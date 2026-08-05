package com.zhangnianshuai.servicemind.application.knowledge;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.CodingErrorAction;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Set;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.zhangnianshuai.servicemind.api.knowledge.dto.AddKnowledgeVersionRequest;
import com.zhangnianshuai.servicemind.api.knowledge.dto.KnowledgeVersionDetailResponse;
import com.zhangnianshuai.servicemind.domain.knowledge.KnowledgeSourceType;

@Service
public class KnowledgeFileUploadService {

    private static final int MAX_FILENAME_LENGTH = 255;

    private final KnowledgeDocumentService documentService;
    private final long maxFileSizeBytes;

    public KnowledgeFileUploadService(
            KnowledgeDocumentService documentService,
            @Value(
                "${app.knowledge.upload.max-file-size-bytes:"
                    + "2097152}"
            )
            long maxFileSizeBytes) {
        if (maxFileSizeBytes <= 0) {
            throw new IllegalArgumentException(
                "Knowledge upload maximum file size "
                    + "must be greater than zero."
            );
        }

        this.documentService = documentService;
        this.maxFileSizeBytes = maxFileSizeBytes;
    }

    public KnowledgeVersionDetailResponse uploadVersion(
            String documentNo,
            MultipartFile file) {
        validateFilePresence(file);
        validateDeclaredSize(file);

        String originalFilename =
            validateOriginalFilename(
                file.getOriginalFilename()
            );

        UploadType uploadType =
            resolveUploadType(originalFilename);

        String mimeType =
            normalizeMimeType(file.getContentType());

        validateMimeType(uploadType, mimeType);

        byte[] bytes = readBytes(file);
        String content = decodeStrictUtf8(bytes);
        content = removeUtf8Bom(content);

        validateDecodedContent(content);

        AddKnowledgeVersionRequest request =
            new AddKnowledgeVersionRequest(
                uploadType.sourceType(),
                originalFilename,
                mimeType,
                content
            );

        return documentService.addVersion(
            documentNo,
            request
        );
    }

    private void validateFilePresence(
            MultipartFile file) {
        if (file == null) {
            throw badRequest(
                "Knowledge upload file is required."
            );
        }

        if (file.isEmpty()) {
            throw badRequest(
                "Uploaded file must not be empty."
            );
        }
    }

    private void validateDeclaredSize(
            MultipartFile file) {
        if (file.getSize() <= 0) {
            throw badRequest(
                "Uploaded file must not be empty."
            );
        }

        if (file.getSize() > maxFileSizeBytes) {
            throw badRequest(
                "Uploaded file exceeds the maximum size of "
                    + maxFileSizeBytes
                    + " bytes."
            );
        }
    }

    private String validateOriginalFilename(
            String originalFilename) {
        if (!StringUtils.hasText(originalFilename)) {
            throw badRequest(
                "Original filename is required."
            );
        }

        String filename = originalFilename.trim();

        if (filename.length() > MAX_FILENAME_LENGTH) {
            throw badRequest(
                "Original filename exceeds "
                    + MAX_FILENAME_LENGTH
                    + " characters."
            );
        }

        if (filename.indexOf('/') >= 0
                || filename.indexOf('\\') >= 0
                || filename.indexOf(':') >= 0) {
            throw badRequest(
                "Path-style filenames are not allowed."
            );
        }

        if (filename.equals(".")
                || filename.equals("..")
                || filename.contains("../")
                || filename.contains("..\\")) {
            throw badRequest(
                "Path traversal filenames are not allowed."
            );
        }

        for (int index = 0;
                index < filename.length();
                index++) {
            if (Character.isISOControl(
                    filename.charAt(index))) {
                throw badRequest(
                    "Original filename contains "
                        + "invalid control characters."
                );
            }
        }

        return filename;
    }

    private UploadType resolveUploadType(
            String originalFilename) {
        String lowerFilename =
            originalFilename.toLowerCase(Locale.ROOT);

        if (lowerFilename.endsWith(".txt")) {
            return UploadType.TEXT;
        }

        if (lowerFilename.endsWith(".md")
                || lowerFilename.endsWith(".markdown")) {
            return UploadType.MARKDOWN;
        }

        throw badRequest(
            "Unsupported file extension. Only .txt, .md, "
                + "and .markdown are supported."
        );
    }

    private String normalizeMimeType(
            String contentType) {
        if (!StringUtils.hasText(contentType)) {
            throw badRequest(
                "Uploaded file MIME type is required."
            );
        }

        String normalized = contentType
            .split(";", 2)[0]
            .trim()
            .toLowerCase(Locale.ROOT);

        if (!StringUtils.hasText(normalized)) {
            throw badRequest(
                "Uploaded file MIME type is required."
            );
        }

        return normalized;
    }

    private void validateMimeType(
            UploadType uploadType,
            String mimeType) {
        if (!uploadType.allowedMimeTypes()
                .contains(mimeType)) {
            throw badRequest(
                "MIME type " + mimeType
                    + " is not allowed for "
                    + uploadType.description()
                    + " files."
            );
        }
    }

    private byte[] readBytes(
            MultipartFile file) {
        try {
            byte[] bytes = file.getBytes();

            if (bytes.length == 0) {
                throw badRequest(
                    "Uploaded file must not be empty."
                );
            }

            if (bytes.length > maxFileSizeBytes) {
                throw badRequest(
                    "Uploaded file exceeds the maximum size of "
                        + maxFileSizeBytes
                        + " bytes."
                );
            }

            return bytes;
        }
        catch (IOException exception) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Uploaded file could not be read.",
                exception
            );
        }
    }

    private String decodeStrictUtf8(
            byte[] bytes) {
        try {
            return StandardCharsets.UTF_8
                .newDecoder()
                .onMalformedInput(
                    CodingErrorAction.REPORT
                )
                .onUnmappableCharacter(
                    CodingErrorAction.REPORT
                )
                .decode(ByteBuffer.wrap(bytes))
                .toString();
        }
        catch (CharacterCodingException exception) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Uploaded file must contain valid UTF-8 text.",
                exception
            );
        }
    }

    private String removeUtf8Bom(
            String content) {
        if (!content.isEmpty()
                && content.charAt(0) == '\uFEFF') {
            return content.substring(1);
        }

        return content;
    }

    private void validateDecodedContent(
            String content) {
        if (!StringUtils.hasText(content)) {
            throw badRequest(
                "Uploaded text content must not be blank."
            );
        }

        for (int index = 0;
                index < content.length();
                index++) {
            char character = content.charAt(index);

            if (character == '\u0000') {
                throw badRequest(
                    "Uploaded file contains NUL characters "
                        + "and appears to be binary."
                );
            }

            if (Character.isISOControl(character)
                    && character != '\n'
                    && character != '\r'
                    && character != '\t') {
                throw badRequest(
                    "Uploaded file contains binary "
                        + "control characters."
                );
            }
        }
    }

    private ResponseStatusException badRequest(
            String message) {
        return new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            message
        );
    }

    private enum UploadType {

        TEXT(
            KnowledgeSourceType.TEXT,
            Set.of("text/plain"),
            "TXT"
        ),

        MARKDOWN(
            KnowledgeSourceType.MARKDOWN,
            Set.of(
                "text/markdown",
                "text/x-markdown",
                "text/plain"
            ),
            "Markdown"
        );

        private final KnowledgeSourceType sourceType;
        private final Set<String> allowedMimeTypes;
        private final String description;

        UploadType(
                KnowledgeSourceType sourceType,
                Set<String> allowedMimeTypes,
                String description) {
            this.sourceType = sourceType;
            this.allowedMimeTypes = allowedMimeTypes;
            this.description = description;
        }

        private KnowledgeSourceType sourceType() {
            return sourceType;
        }

        private Set<String> allowedMimeTypes() {
            return allowedMimeTypes;
        }

        private String description() {
            return description;
        }
    }
}