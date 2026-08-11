package com.zhangnianshuai.servicemind.api.ticket;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zhangnianshuai.servicemind.api.ticket.dto.AssignTicketRequest;
import com.zhangnianshuai.servicemind.api.ticket.dto.ChangeTicketPriorityRequest;
import com.zhangnianshuai.servicemind.api.ticket.dto.CreateSupportTicketRequest;
import com.zhangnianshuai.servicemind.api.ticket.dto.ResolveTicketRequest;
import com.zhangnianshuai.servicemind.api.ticket.dto.SupportTicketPageResponse;
import com.zhangnianshuai.servicemind.api.ticket.dto.SupportTicketResponse;
import com.zhangnianshuai.servicemind.application.ticket.SupportTicketService;
import com.zhangnianshuai.servicemind.domain.ticket.TicketCategory;
import com.zhangnianshuai.servicemind.domain.ticket.TicketPriority;
import com.zhangnianshuai.servicemind.domain.ticket.TicketStatus;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Validated
@RestController
@RequestMapping("/api/tickets")
public class SupportTicketController {

    private final SupportTicketService service;

    public SupportTicketController(
            SupportTicketService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<SupportTicketResponse> create(
            @Valid
            @RequestBody
            CreateSupportTicketRequest request) {
        SupportTicketResponse response =
            service.create(request);

        URI location = URI.create(
            "/api/tickets/" + response.ticketNo()
        );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .location(location)
            .body(response);
    }

    @GetMapping("/search")
    public SupportTicketPageResponse search(
            @RequestParam(required = false)
            @Size(max = 32)
            String ticketNo,

            @RequestParam(required = false)
            @Size(max = 32)
            String customerNo,

            @RequestParam(required = false)
            @Size(max = 100)
            String assignedTo,

            @RequestParam(required = false)
            TicketCategory category,

            @RequestParam(required = false)
            TicketPriority priority,

            @RequestParam(required = false)
            TicketStatus status,

            @RequestParam(defaultValue = "0")
            @Min(0)
            int page,

            @RequestParam(defaultValue = "20")
            @Min(1)
            @Max(100)
            int size) {

        return service.searchTickets(
            ticketNo,
            customerNo,
            assignedTo,
            category,
            priority,
            status,
            page,
            size
        );
    }

    @GetMapping("/{ticketNo}")
    public SupportTicketResponse findByTicketNo(
            @PathVariable
            String ticketNo) {
        return service.findByTicketNo(ticketNo);
    }

    @GetMapping(params = "customerNo")
    public List<SupportTicketResponse> findByCustomerNo(
            @RequestParam
            @NotBlank
            String customerNo) {
        return service.findByCustomerNo(customerNo);
    }

    @PostMapping("/{ticketNo}/assign")
    public SupportTicketResponse assign(
            @PathVariable
            String ticketNo,
            @Valid
            @RequestBody
            AssignTicketRequest request) {
        return service.assign(
            ticketNo,
            request.assignedTo()
        );
    }

    @PostMapping("/{ticketNo}/priority")
    public SupportTicketResponse changePriority(
            @PathVariable
            String ticketNo,
            @Valid
            @RequestBody
            ChangeTicketPriorityRequest request) {
        return service.changePriority(
            ticketNo,
            request.priority()
        );
    }

    @PostMapping("/{ticketNo}/start-progress")
    public SupportTicketResponse startProgress(
            @PathVariable
            String ticketNo) {
        return service.startProgress(ticketNo);
    }

    @PostMapping("/{ticketNo}/wait-customer")
    public SupportTicketResponse waitForCustomer(
            @PathVariable
            String ticketNo) {
        return service.waitForCustomer(ticketNo);
    }

    @PostMapping("/{ticketNo}/resolve")
    public SupportTicketResponse resolve(
            @PathVariable
            String ticketNo,
            @Valid
            @RequestBody
            ResolveTicketRequest request) {
        return service.resolve(
            ticketNo,
            request.resolution()
        );
    }

    @PostMapping("/{ticketNo}/reopen")
    public SupportTicketResponse reopen(
            @PathVariable
            String ticketNo) {
        return service.reopen(ticketNo);
    }

    @PostMapping("/{ticketNo}/close")
    public SupportTicketResponse close(
            @PathVariable
            String ticketNo) {
        return service.close(ticketNo);
    }
}