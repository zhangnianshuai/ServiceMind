package com.zhangnianshuai.servicemind.api.knowledge;

import java.net.URI;
import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.zhangnianshuai.servicemind.api.knowledge.dto.AddKnowledgeVersionRequest;
import com.zhangnianshuai.servicemind.api.knowledge.dto.CreateKnowledgeDocumentRequest;
import com.zhangnianshuai.servicemind.api.knowledge.dto.KnowledgeDocumentDetailResponse;
import com.zhangnianshuai.servicemind.api.knowledge.dto.KnowledgeDocumentSummaryResponse;
import com.zhangnianshuai.servicemind.api.knowledge.dto.KnowledgeVersionDetailResponse;
import com.zhangnianshuai.servicemind.api.knowledge.dto.KnowledgeVersionSummaryResponse;
import com.zhangnianshuai.servicemind.api.knowledge.dto.UpdateKnowledgeDocumentRequest;
import com.zhangnianshuai.servicemind.application.knowledge.KnowledgeDocumentService;
import com.zhangnianshuai.servicemind.domain.knowledge.KnowledgeDocumentCategory;
import com.zhangnianshuai.servicemind.domain.knowledge.KnowledgeDocumentStatus;

import jakarta.validation.Valid;

@Validated
@RestController
@RequestMapping("/api/knowledge/documents")
public class KnowledgeDocumentController {

    private final KnowledgeDocumentService service;

    public KnowledgeDocumentController(
            KnowledgeDocumentService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<KnowledgeDocumentDetailResponse> create(
            @Valid
            @RequestBody
            CreateKnowledgeDocumentRequest request) {
        KnowledgeDocumentDetailResponse response =
            service.create(request);

        URI location = URI.create(
            "/api/knowledge/documents/"
                + response.documentNo()
        );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .location(location)
            .body(response);
    }

    @GetMapping
    public List<KnowledgeDocumentSummaryResponse> list(
            @RequestParam(required = false)
            KnowledgeDocumentCategory category,
            @RequestParam(required = false)
            KnowledgeDocumentStatus status) {
        return service.list(category, status);
    }

    @GetMapping("/{documentNo}")
    public KnowledgeDocumentDetailResponse findDocument(
            @PathVariable
            String documentNo) {
        return service.findDocument(documentNo);
    }

    @PatchMapping("/{documentNo}")
    public KnowledgeDocumentDetailResponse updateMetadata(
            @PathVariable
            String documentNo,
            @Valid
            @RequestBody
            UpdateKnowledgeDocumentRequest request) {
        return service.updateMetadata(documentNo, request);
    }

    @PostMapping("/{documentNo}/versions")
    public ResponseEntity<KnowledgeVersionDetailResponse>
            addVersion(
                @PathVariable
                String documentNo,
                @Valid
                @RequestBody
                AddKnowledgeVersionRequest request) {
        KnowledgeVersionDetailResponse response =
            service.addVersion(documentNo, request);

        URI location = URI.create(
            "/api/knowledge/documents/"
                + documentNo
                + "/versions/"
                + response.versionNo()
        );

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .location(location)
            .body(response);
    }

    @GetMapping("/{documentNo}/versions")
    public List<KnowledgeVersionSummaryResponse> listVersions(
            @PathVariable
            String documentNo) {
        return service.listVersions(documentNo);
    }

    @GetMapping("/{documentNo}/versions/{versionNo}")
    public KnowledgeVersionDetailResponse findVersion(
            @PathVariable
            String documentNo,
            @PathVariable
            int versionNo) {
        return service.findVersion(documentNo, versionNo);
    }

    @PostMapping("/{documentNo}/archive")
    public KnowledgeDocumentDetailResponse archive(
            @PathVariable
            String documentNo) {
        return service.archive(documentNo);
    }
}