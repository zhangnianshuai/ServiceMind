package com.zhangnianshuai.servicemind.api.aftersales;

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

import com.zhangnianshuai.servicemind.api.aftersales.dto.AfterSalesPageResponse;
import com.zhangnianshuai.servicemind.api.aftersales.dto.AfterSalesResponse;
import com.zhangnianshuai.servicemind.api.aftersales.dto.CreateAfterSalesRequest;
import com.zhangnianshuai.servicemind.api.aftersales.dto.ReviewAfterSalesRequest;
import com.zhangnianshuai.servicemind.application.aftersales.AfterSalesApplicationService;
import com.zhangnianshuai.servicemind.domain.aftersales.AfterSalesStatus;
import com.zhangnianshuai.servicemind.domain.aftersales.AfterSalesType;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Validated
@RestController
@RequestMapping("/api/after-sales")
public class AfterSalesController {

    private final AfterSalesApplicationService service;

    public AfterSalesController(
            AfterSalesApplicationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<AfterSalesResponse> create(
            @Valid
            @RequestBody
            CreateAfterSalesRequest request) {
        AfterSalesResponse response = service.create(request);

        URI location = URI.create(
            "/api/after-sales/" + response.applicationNo()
        );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .location(location)
            .body(response);
    }

    @GetMapping("/search")
    public AfterSalesPageResponse search(
            @RequestParam(required = false)
            @Size(max = 32)
            String applicationNo,

            @RequestParam(required = false)
            @Size(max = 32)
            String orderNo,

            @RequestParam(required = false)
            @Size(max = 32)
            String customerNo,

            @RequestParam(required = false)
            AfterSalesType type,

            @RequestParam(required = false)
            AfterSalesStatus status,

            @RequestParam(defaultValue = "0")
            @Min(0)
            int page,

            @RequestParam(defaultValue = "20")
            @Min(1)
            @Max(100)
            int size) {

        return service.searchApplications(
            applicationNo,
            orderNo,
            customerNo,
            type,
            status,
            page,
            size
        );
    }

    @GetMapping("/{applicationNo}")
    public AfterSalesResponse findByApplicationNo(
            @PathVariable
            String applicationNo) {
        return service.findByApplicationNo(applicationNo);
    }

    @GetMapping(params = "customerNo")
    public List<AfterSalesResponse> findByCustomerNo(
            @RequestParam
            @NotBlank
            String customerNo) {
        return service.findByCustomerNo(customerNo);
    }

    @PostMapping("/{applicationNo}/start-review")
    public AfterSalesResponse startReview(
            @PathVariable
            String applicationNo) {
        return service.startReview(applicationNo);
    }

    @PostMapping("/{applicationNo}/approve")
    public AfterSalesResponse approve(
            @PathVariable
            String applicationNo,
            @Valid
            @RequestBody
            ReviewAfterSalesRequest request) {
        return service.approve(
            applicationNo,
            request.comment()
        );
    }

    @PostMapping("/{applicationNo}/reject")
    public AfterSalesResponse reject(
            @PathVariable
            String applicationNo,
            @Valid
            @RequestBody
            ReviewAfterSalesRequest request) {
        return service.reject(
            applicationNo,
            request.comment()
        );
    }

    @PostMapping("/{applicationNo}/start-processing")
    public AfterSalesResponse startProcessing(
            @PathVariable
            String applicationNo) {
        return service.startProcessing(applicationNo);
    }

    @PostMapping("/{applicationNo}/complete")
    public AfterSalesResponse complete(
            @PathVariable
            String applicationNo) {
        return service.complete(applicationNo);
    }

    @PostMapping("/{applicationNo}/cancel")
    public AfterSalesResponse cancel(
            @PathVariable
            String applicationNo) {
        return service.cancel(applicationNo);
    }
}