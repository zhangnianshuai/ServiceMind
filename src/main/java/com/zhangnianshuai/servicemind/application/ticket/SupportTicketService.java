package com.zhangnianshuai.servicemind.application.ticket;

import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.function.Consumer;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import com.zhangnianshuai.servicemind.api.ticket.dto.CreateSupportTicketRequest;
import com.zhangnianshuai.servicemind.api.ticket.dto.SupportTicketPageResponse;
import com.zhangnianshuai.servicemind.api.ticket.dto.SupportTicketResponse;
import com.zhangnianshuai.servicemind.domain.aftersales.AfterSalesApplication;
import com.zhangnianshuai.servicemind.domain.aftersales.AfterSalesApplicationRepository;
import com.zhangnianshuai.servicemind.domain.customer.Customer;
import com.zhangnianshuai.servicemind.domain.customer.CustomerRepository;
import com.zhangnianshuai.servicemind.domain.order.CustomerOrder;
import com.zhangnianshuai.servicemind.domain.order.CustomerOrderRepository;
import com.zhangnianshuai.servicemind.domain.ticket.SupportTicket;
import com.zhangnianshuai.servicemind.domain.ticket.SupportTicketRepository;
import com.zhangnianshuai.servicemind.domain.ticket.TicketCategory;
import com.zhangnianshuai.servicemind.domain.ticket.TicketPriority;
import com.zhangnianshuai.servicemind.domain.ticket.TicketStatus;

@Service
@Transactional(readOnly = true)
public class SupportTicketService {

    private final CustomerRepository customerRepository;
    private final CustomerOrderRepository orderRepository;
    private final AfterSalesApplicationRepository afterSalesRepository;
    private final SupportTicketRepository ticketRepository;

    public SupportTicketService(
            CustomerRepository customerRepository,
            CustomerOrderRepository orderRepository,
            AfterSalesApplicationRepository afterSalesRepository,
            SupportTicketRepository ticketRepository) {
        this.customerRepository = customerRepository;
        this.orderRepository = orderRepository;
        this.afterSalesRepository = afterSalesRepository;
        this.ticketRepository = ticketRepository;
    }

    @Transactional
    public SupportTicketResponse create(
            CreateSupportTicketRequest request) {
        String customerNo = requireText(
            request.customerNo(),
            "Customer number is required."
        );

        Customer customer = customerRepository
            .findByCustomerNoIgnoreCase(customerNo)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Customer was not found: " + customerNo
            ));

        CustomerOrder order = findOptionalOrder(
            request.orderNo()
        );

        if (order != null
                && !Objects.equals(
                    order.getCustomer().getId(),
                    customer.getId()
                )) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "The order does not belong to this customer."
            );
        }

        AfterSalesApplication afterSalesApplication =
            findOptionalAfterSalesApplication(
                request.afterSalesApplicationNo()
            );

        if (afterSalesApplication != null) {
            CustomerOrder afterSalesOrder =
                afterSalesApplication.getOrder();

            if (!Objects.equals(
                    afterSalesOrder.getCustomer().getId(),
                    customer.getId()
                )) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The after-sales application does not "
                        + "belong to this customer."
                );
            }

            if (order == null) {
                order = afterSalesOrder;
            }
            else if (!Objects.equals(
                    order.getId(),
                    afterSalesOrder.getId()
                )) {
                throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "The after-sales application does not "
                        + "belong to the supplied order."
                );
            }
        }

        TicketPriority priority =
            request.priority() == null
                ? TicketPriority.NORMAL
                : request.priority();

        SupportTicket ticket = new SupportTicket(
            generateTicketNo(),
            customer,
            order,
            afterSalesApplication,
            request.category(),
            priority,
            request.source(),
            requireText(
                request.subject(),
                "Ticket subject is required."
            ),
            requireText(
                request.description(),
                "Ticket description is required."
            )
        );

        try {
            SupportTicket saved =
                ticketRepository.saveAndFlush(ticket);

            return toResponse(saved);
        }
        catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "The ticket conflicts with an existing record.",
                exception
            );
        }
    }

    public SupportTicketResponse findByTicketNo(
            String ticketNo) {
        return toResponse(findRequired(ticketNo));
    }

    public List<SupportTicketResponse> findByCustomerNo(
            String customerNo) {
        String normalizedCustomerNo = requireText(
            customerNo,
            "Customer number is required."
        );

        if (!customerRepository.existsByCustomerNoIgnoreCase(
                normalizedCustomerNo
            )) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Customer was not found: "
                    + normalizedCustomerNo
            );
        }

        return ticketRepository
            .findByCustomer_CustomerNoIgnoreCaseOrderByOpenedAtDesc(
                normalizedCustomerNo
            )
            .stream()
            .map(this::toResponse)
            .toList();
    }

    public SupportTicketPageResponse searchTickets(
            String ticketNo,
            String customerNo,
            String assignedTo,
            TicketCategory category,
            TicketPriority priority,
            TicketStatus status,
            int page,
            int size) {

        PageRequest pageable =
            PageRequest.of(
                page,
                size,
                Sort.by(
                    Sort.Direction.DESC,
                    "openedAt"
                )
            );

        Page<SupportTicketResponse> result =
            ticketRepository
                .searchTickets(
                    toLikePattern(ticketNo),
                    toLikePattern(customerNo),
                    toLikePattern(assignedTo),
                    category,
                    priority,
                    status,
                    pageable
                )
                .map(this::toResponse);

        return SupportTicketPageResponse.from(result);
    }

    @Transactional
    public SupportTicketResponse assign(
            String ticketNo,
            String assignedTo) {
        String normalizedAssignee = requireText(
            assignedTo,
            "Assignee is required."
        );

        return changeState(
            ticketNo,
            ticket -> ticket.assignTo(normalizedAssignee)
        );
    }

    @Transactional
    public SupportTicketResponse changePriority(
            String ticketNo,
            TicketPriority priority) {
        if (priority == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Ticket priority is required."
            );
        }

        return changeState(
            ticketNo,
            ticket -> ticket.changePriority(priority)
        );
    }

    @Transactional
    public SupportTicketResponse startProgress(
            String ticketNo) {
        return changeState(
            ticketNo,
            SupportTicket::startProgress
        );
    }

    @Transactional
    public SupportTicketResponse waitForCustomer(
            String ticketNo) {
        return changeState(
            ticketNo,
            SupportTicket::waitForCustomer
        );
    }

    @Transactional
    public SupportTicketResponse resolve(
            String ticketNo,
            String resolution) {
        String normalizedResolution = requireText(
            resolution,
            "Resolution is required."
        );

        return changeState(
            ticketNo,
            ticket -> ticket.resolve(normalizedResolution)
        );
    }

    @Transactional
    public SupportTicketResponse reopen(
            String ticketNo) {
        SupportTicket ticket = findRequired(ticketNo);

        if (ticket.getStatus() != TicketStatus.RESOLVED) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Only resolved tickets can be reopened."
            );
        }

        return applyStateChange(
            ticket,
            SupportTicket::startProgress
        );
    }

    @Transactional
    public SupportTicketResponse close(
            String ticketNo) {
        return changeState(
            ticketNo,
            SupportTicket::close
        );
    }

    private SupportTicketResponse changeState(
            String ticketNo,
            Consumer<SupportTicket> stateChange) {
        SupportTicket ticket = findRequired(ticketNo);

        return applyStateChange(ticket, stateChange);
    }

    private SupportTicketResponse applyStateChange(
            SupportTicket ticket,
            Consumer<SupportTicket> stateChange) {
        try {
            stateChange.accept(ticket);

            SupportTicket saved =
                ticketRepository.saveAndFlush(ticket);

            return toResponse(saved);
        }
        catch (IllegalStateException exception) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                exception.getMessage(),
                exception
            );
        }
        catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                exception.getMessage(),
                exception
            );
        }
    }

    private SupportTicket findRequired(String ticketNo) {
        String normalizedTicketNo = requireText(
            ticketNo,
            "Ticket number is required."
        );

        return ticketRepository
            .findByTicketNoIgnoreCase(normalizedTicketNo)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Support ticket was not found: "
                    + normalizedTicketNo
            ));
    }

    private CustomerOrder findOptionalOrder(
            String orderNo) {
        if (!StringUtils.hasText(orderNo)) {
            return null;
        }

        String normalizedOrderNo = orderNo.trim();

        return orderRepository
            .findByOrderNoIgnoreCase(normalizedOrderNo)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Order was not found: " + normalizedOrderNo
            ));
    }

    private AfterSalesApplication
            findOptionalAfterSalesApplication(
                String applicationNo) {
        if (!StringUtils.hasText(applicationNo)) {
            return null;
        }

        String normalizedApplicationNo =
            applicationNo.trim();

        return afterSalesRepository
            .findByApplicationNoIgnoreCase(
                normalizedApplicationNo
            )
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "After-sales application was not found: "
                    + normalizedApplicationNo
            ));
    }

    private SupportTicketResponse toResponse(
            SupportTicket ticket) {
        String orderNo = ticket.getOrder() == null
            ? null
            : ticket.getOrder().getOrderNo();

        String afterSalesApplicationNo =
            ticket.getAfterSalesApplication() == null
                ? null
                : ticket
                    .getAfterSalesApplication()
                    .getApplicationNo();

        return new SupportTicketResponse(
            ticket.getTicketNo(),
            ticket.getCustomer().getCustomerNo(),
            ticket.getCustomer().getFullName(),
            orderNo,
            afterSalesApplicationNo,
            ticket.getCategory(),
            ticket.getPriority(),
            ticket.getStatus(),
            ticket.getSource(),
            ticket.getSubject(),
            ticket.getDescription(),
            ticket.getAssignedTo(),
            ticket.getResolution(),
            ticket.getOpenedAt(),
            ticket.getFirstRespondedAt(),
            ticket.getAssignedAt(),
            ticket.getResolvedAt(),
            ticket.getClosedAt(),
            ticket.getCreatedAt(),
            ticket.getUpdatedAt()
        );
    }

    private String generateTicketNo() {
        String randomPart = UUID.randomUUID()
            .toString()
            .replace("-", "")
            .substring(0, 20)
            .toUpperCase();

        return "TK" + randomPart;
    }

    private String requireText(
            String value,
            String errorMessage) {
        if (!StringUtils.hasText(value)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                errorMessage
            );
        }

        return value.trim();
    }


    private String toLikePattern(String value) {
        if (!StringUtils.hasText(value)) {
            return "%";
        }

        return "%"
            + value.trim()
                .toLowerCase(java.util.Locale.ROOT)
            + "%";
    }
    private String normalizeOptional(
            String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        return value.trim();
    }
}