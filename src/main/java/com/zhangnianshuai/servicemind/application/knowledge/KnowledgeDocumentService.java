package com.zhangnianshuai.servicemind.application.knowledge;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.List;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ResponseStatusException;

import com.zhangnianshuai.servicemind.api.knowledge.dto.AddKnowledgeVersionRequest;
import com.zhangnianshuai.servicemind.api.knowledge.dto.CreateKnowledgeDocumentRequest;
import com.zhangnianshuai.servicemind.api.knowledge.dto.KnowledgeDocumentDetailResponse;
import com.zhangnianshuai.servicemind.api.knowledge.dto.KnowledgeDocumentSummaryResponse;
import com.zhangnianshuai.servicemind.api.knowledge.dto.KnowledgeVersionDetailResponse;
import com.zhangnianshuai.servicemind.api.knowledge.dto.KnowledgeVersionSummaryResponse;
import com.zhangnianshuai.servicemind.api.knowledge.dto.UpdateKnowledgeDocumentRequest;
import com.zhangnianshuai.servicemind.domain.knowledge.KnowledgeDocument;
import com.zhangnianshuai.servicemind.domain.knowledge.KnowledgeDocumentCategory;
import com.zhangnianshuai.servicemind.domain.knowledge.KnowledgeDocumentRepository;
import com.zhangnianshuai.servicemind.domain.knowledge.KnowledgeDocumentStatus;
import com.zhangnianshuai.servicemind.domain.knowledge.KnowledgeDocumentVersion;
import com.zhangnianshuai.servicemind.domain.knowledge.KnowledgeDocumentVersionRepository;
import com.zhangnianshuai.servicemind.domain.knowledge.KnowledgeSourceType;

@Service
@Transactional(readOnly = true)
public class KnowledgeDocumentService {

    private static final int MAX_CONTENT_LENGTH = 1_000_000;

    private final KnowledgeDocumentRepository documentRepository;
    private final KnowledgeDocumentVersionRepository versionRepository;

    public KnowledgeDocumentService(
            KnowledgeDocumentRepository documentRepository,
            KnowledgeDocumentVersionRepository versionRepository) {
        this.documentRepository = documentRepository;
        this.versionRepository = versionRepository;
    }

    @Transactional
    public KnowledgeDocumentDetailResponse create(
            CreateKnowledgeDocumentRequest request) {
        validateSupportedSourceType(request.sourceType());

        String content = normalizeContent(request.rawContent());
        String contentHash = sha256(content);

        KnowledgeDocument document =
            new KnowledgeDocument(
                generateDocumentNo(),
                requireText(
                    request.title(),
                    "Document title is required."
                ),
                request.category(),
                normalizeOptional(request.createdBy())
            );

        try {
            document = documentRepository.saveAndFlush(document);

            int versionNo = document.beginNewVersion();

            KnowledgeDocumentVersion version =
                new KnowledgeDocumentVersion(
                    document,
                    versionNo,
                    request.sourceType(),
                    normalizeOptional(request.originalFilename()),
                    normalizeOptional(request.mimeType()),
                    contentHash,
                    content
                );

            versionRepository.saveAndFlush(version);
            documentRepository.saveAndFlush(document);

            return toDetail(document);
        }
        catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "The knowledge document conflicts with "
                    + "an existing record.",
                exception
            );
        }
    }

    @Transactional
    public KnowledgeVersionDetailResponse addVersion(
            String documentNo,
            AddKnowledgeVersionRequest request) {
        KnowledgeDocument document =
            findRequiredDocument(documentNo);

        if (document.getStatus()
                == KnowledgeDocumentStatus.ARCHIVED) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Archived knowledge documents cannot "
                    + "accept new versions."
            );
        }

        validateSupportedSourceType(request.sourceType());

        String content = normalizeContent(request.rawContent());
        String contentHash = sha256(content);

        if (versionRepository
                .existsByDocument_IdAndContentHash(
                    document.getId(),
                    contentHash
                )) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "An identical version already exists "
                    + "for this document."
            );
        }

        try {
            int versionNo = document.beginNewVersion();

            KnowledgeDocumentVersion version =
                new KnowledgeDocumentVersion(
                    document,
                    versionNo,
                    request.sourceType(),
                    normalizeOptional(request.originalFilename()),
                    normalizeOptional(request.mimeType()),
                    contentHash,
                    content
                );

            version = versionRepository.saveAndFlush(version);
            documentRepository.saveAndFlush(document);

            return toVersionDetail(version);
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
        catch (DataIntegrityViolationException exception) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "The knowledge version conflicts with "
                    + "an existing record.",
                exception
            );
        }
    }

    public List<KnowledgeDocumentSummaryResponse> list(
            KnowledgeDocumentCategory category,
            KnowledgeDocumentStatus status) {
        List<KnowledgeDocument> documents;

        if (category != null && status != null) {
            documents = documentRepository
                .findByCategoryAndStatusOrderByUpdatedAtDesc(
                    category,
                    status
                );
        }
        else if (category != null) {
            documents = documentRepository
                .findByCategoryOrderByUpdatedAtDesc(category);
        }
        else if (status != null) {
            documents = documentRepository
                .findByStatusOrderByUpdatedAtDesc(status);
        }
        else {
            documents = documentRepository
                .findAllByOrderByUpdatedAtDesc();
        }

        return documents.stream()
            .map(this::toSummary)
            .toList();
    }

    public KnowledgeDocumentDetailResponse findDocument(
            String documentNo) {
        return toDetail(findRequiredDocument(documentNo));
    }

    public List<KnowledgeVersionSummaryResponse> listVersions(
            String documentNo) {
        KnowledgeDocument document =
            findRequiredDocument(documentNo);

        return versionRepository
            .findByDocument_DocumentNoIgnoreCaseOrderByVersionNoDesc(
                document.getDocumentNo()
            )
            .stream()
            .map(this::toVersionSummary)
            .toList();
    }

    public KnowledgeVersionDetailResponse findVersion(
            String documentNo,
            int versionNo) {
        KnowledgeDocument document =
            findRequiredDocument(documentNo);

        if (versionNo <= 0) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Version number must be greater than zero."
            );
        }

        KnowledgeDocumentVersion version =
            versionRepository
                .findByDocument_IdAndVersionNo(
                    document.getId(),
                    versionNo
                )
                .orElseThrow(() -> new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Knowledge version was not found: "
                        + versionNo
                ));

        return toVersionDetail(version);
    }

    @Transactional
    public KnowledgeDocumentDetailResponse updateMetadata(
            String documentNo,
            UpdateKnowledgeDocumentRequest request) {
        KnowledgeDocument document =
            findRequiredDocument(documentNo);

        try {
            document.changeMetadata(
                requireText(
                    request.title(),
                    "Document title is required."
                ),
                request.category()
            );

            KnowledgeDocument saved =
                documentRepository.saveAndFlush(document);

            return toDetail(saved);
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

    @Transactional
    public KnowledgeDocumentDetailResponse archive(
            String documentNo) {
        KnowledgeDocument document =
            findRequiredDocument(documentNo);

        document.archive();

        KnowledgeDocument saved =
            documentRepository.saveAndFlush(document);

        return toDetail(saved);
    }

    private KnowledgeDocument findRequiredDocument(
            String documentNo) {
        String normalizedDocumentNo = requireText(
            documentNo,
            "Document number is required."
        );

        return documentRepository
            .findByDocumentNoIgnoreCase(normalizedDocumentNo)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Knowledge document was not found: "
                    + normalizedDocumentNo
            ));
    }

    private KnowledgeDocumentDetailResponse toDetail(
            KnowledgeDocument document) {
        List<KnowledgeVersionSummaryResponse> versions =
            versionRepository
                .findByDocument_DocumentNoIgnoreCaseOrderByVersionNoDesc(
                    document.getDocumentNo()
                )
                .stream()
                .map(this::toVersionSummary)
                .toList();

        return new KnowledgeDocumentDetailResponse(
            document.getDocumentNo(),
            document.getTitle(),
            document.getCategory(),
            document.getStatus(),
            document.getLatestVersionNo(),
            document.getActiveVersionNo(),
            document.getCreatedBy(),
            document.getCreatedAt(),
            document.getUpdatedAt(),
            versions
        );
    }

    private KnowledgeDocumentSummaryResponse toSummary(
            KnowledgeDocument document) {
        return new KnowledgeDocumentSummaryResponse(
            document.getDocumentNo(),
            document.getTitle(),
            document.getCategory(),
            document.getStatus(),
            document.getLatestVersionNo(),
            document.getActiveVersionNo(),
            document.getCreatedBy(),
            document.getCreatedAt(),
            document.getUpdatedAt()
        );
    }

    private KnowledgeVersionSummaryResponse toVersionSummary(
            KnowledgeDocumentVersion version) {
        return new KnowledgeVersionSummaryResponse(
            version.getVersionNo(),
            version.getSourceType(),
            version.getOriginalFilename(),
            version.getMimeType(),
            version.getContentHash(),
            version.getStatus(),
            version.getChunkCount(),
            version.getEmbeddingModel(),
            version.getEmbeddingDimensions(),
            version.getErrorMessage(),
            version.getCreatedAt(),
            version.getUpdatedAt(),
            version.getProcessedAt(),
            version.getPublishedAt()
        );
    }

    private KnowledgeVersionDetailResponse toVersionDetail(
            KnowledgeDocumentVersion version) {
        return new KnowledgeVersionDetailResponse(
            version.getDocument().getDocumentNo(),
            version.getVersionNo(),
            version.getSourceType(),
            version.getOriginalFilename(),
            version.getMimeType(),
            version.getContentHash(),
            version.getRawContent(),
            version.getStatus(),
            version.getChunkCount(),
            version.getEmbeddingModel(),
            version.getEmbeddingDimensions(),
            version.getErrorMessage(),
            version.getCreatedAt(),
            version.getUpdatedAt(),
            version.getProcessedAt(),
            version.getPublishedAt()
        );
    }

    private void validateSupportedSourceType(
            KnowledgeSourceType sourceType) {
        if (sourceType == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Knowledge source type is required."
            );
        }

        if (sourceType != KnowledgeSourceType.TEXT
                && sourceType != KnowledgeSourceType.MARKDOWN) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Only TEXT and MARKDOWN sources are supported "
                    + "in the current stage."
            );
        }
    }

    private String normalizeContent(String rawContent) {
        if (!StringUtils.hasText(rawContent)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Knowledge content is required."
            );
        }

        String content = rawContent.strip();

        if (content.length() > MAX_CONTENT_LENGTH) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Knowledge content exceeds the maximum "
                    + "length of " + MAX_CONTENT_LENGTH
                    + " characters."
            );
        }

        return content;
    }

    private String sha256(String content) {
        try {
            MessageDigest digest =
                MessageDigest.getInstance("SHA-256");

            byte[] bytes = digest.digest(
                content.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(bytes);
        }
        catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException(
                "SHA-256 is not available.",
                exception
            );
        }
    }

    private String generateDocumentNo() {
        String randomPart = UUID.randomUUID()
            .toString()
            .replace("-", "")
            .substring(0, 20)
            .toUpperCase();

        return "KB" + randomPart;
    }

    private String requireText(
            String value,
            String message) {
        if (!StringUtils.hasText(value)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                message
            );
        }

        return value.trim();
    }

    private String normalizeOptional(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }

        return value.trim();
    }
}