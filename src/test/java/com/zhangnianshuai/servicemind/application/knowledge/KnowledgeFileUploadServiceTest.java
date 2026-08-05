package com.zhangnianshuai.servicemind.application.knowledge;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.zhangnianshuai.servicemind.api.knowledge.dto.AddKnowledgeVersionRequest;
import com.zhangnianshuai.servicemind.api.knowledge.dto.KnowledgeVersionDetailResponse;
import com.zhangnianshuai.servicemind.domain.knowledge.KnowledgeSourceType;

@ExtendWith(MockitoExtension.class)
class KnowledgeFileUploadServiceTest {

    private static final long MAX_FILE_SIZE_BYTES =
        2_097_152L;

    @Mock
    private KnowledgeDocumentService documentService;

    private KnowledgeFileUploadService service;

    @BeforeEach
    void setUp() {
        service = new KnowledgeFileUploadService(
            documentService,
            MAX_FILE_SIZE_BYTES
        );
    }

    @Test
    void shouldCreateMarkdownVersionAndRemoveBom() {
        String text =
            "\uFEFF# Repair policy\n\n"
                + "Escalate delayed repairs.";

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "repair-policy.markdown",
            "text/markdown",
            text.getBytes(StandardCharsets.UTF_8)
        );

        KnowledgeVersionDetailResponse response =
            mock(KnowledgeVersionDetailResponse.class);

        when(documentService.addVersion(
            eq("KB-001"),
            any(AddKnowledgeVersionRequest.class)
        )).thenReturn(response);

        KnowledgeVersionDetailResponse actual =
            service.uploadVersion("KB-001", file);

        assertThat(actual).isSameAs(response);

        ArgumentCaptor<AddKnowledgeVersionRequest> captor =
            ArgumentCaptor.forClass(
                AddKnowledgeVersionRequest.class
            );

        verify(documentService).addVersion(
            eq("KB-001"),
            captor.capture()
        );

        AddKnowledgeVersionRequest request =
            captor.getValue();

        assertThat(request.sourceType())
            .isEqualTo(KnowledgeSourceType.MARKDOWN);
        assertThat(request.originalFilename())
            .isEqualTo("repair-policy.markdown");
        assertThat(request.mimeType())
            .isEqualTo("text/markdown");
        assertThat(request.rawContent())
            .isEqualTo(
                "# Repair policy\n\n"
                    + "Escalate delayed repairs."
            );
    }

    @Test
    void shouldCreateTextVersionAndNormalizeMimeType() {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "repair-policy.txt",
            "text/plain; charset=UTF-8",
            "Plain repair policy."
                .getBytes(StandardCharsets.UTF_8)
        );

        KnowledgeVersionDetailResponse response =
            mock(KnowledgeVersionDetailResponse.class);

        when(documentService.addVersion(
            eq("KB-002"),
            any(AddKnowledgeVersionRequest.class)
        )).thenReturn(response);

        service.uploadVersion("KB-002", file);

        ArgumentCaptor<AddKnowledgeVersionRequest> captor =
            ArgumentCaptor.forClass(
                AddKnowledgeVersionRequest.class
            );

        verify(documentService).addVersion(
            eq("KB-002"),
            captor.capture()
        );

        AddKnowledgeVersionRequest request =
            captor.getValue();

        assertThat(request.sourceType())
            .isEqualTo(KnowledgeSourceType.TEXT);
        assertThat(request.mimeType())
            .isEqualTo("text/plain");
        assertThat(request.rawContent())
            .isEqualTo("Plain repair policy.");
    }

    @Test
    void shouldRejectNullFile() {
        assertThatThrownBy(() ->
            service.uploadVersion("KB-001", null)
        )
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining(
                "upload file is required"
            )
            .satisfies(exception ->
                assertThat(
                    ((ResponseStatusException) exception)
                        .getStatusCode()
                ).isEqualTo(HttpStatus.BAD_REQUEST)
            );

        verifyNoInteractions(documentService);
    }

    @Test
    void shouldRejectEmptyFile() {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "empty.txt",
            "text/plain",
            new byte[0]
        );

        assertBadRequest(
            file,
            "must not be empty"
        );
    }

    @Test
    void shouldRejectOversizedFile() {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "large.txt",
            "text/plain",
            new byte[(int) MAX_FILE_SIZE_BYTES + 1]
        );

        assertBadRequest(
            file,
            "exceeds the maximum size"
        );
    }

    @Test
    void shouldRejectPathStyleFilename() {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "C:\\fakepath\\repair.md",
            "text/markdown",
            "# Repair".getBytes(StandardCharsets.UTF_8)
        );

        assertBadRequest(
            file,
            "Path-style filenames"
        );
    }

    @Test
    void shouldRejectUnsupportedExtension() {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "repair.pdf",
            "text/plain",
            "Not really a PDF."
                .getBytes(StandardCharsets.UTF_8)
        );

        assertBadRequest(
            file,
            "Unsupported file extension"
        );
    }

    @Test
    void shouldRejectMissingMimeType() {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "repair.txt",
            null,
            "Repair policy."
                .getBytes(StandardCharsets.UTF_8)
        );

        assertBadRequest(
            file,
            "MIME type is required"
        );
    }

    @Test
    void shouldRejectExtensionMimeMismatch() {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "repair.txt",
            "text/markdown",
            "Repair policy."
                .getBytes(StandardCharsets.UTF_8)
        );

        assertBadRequest(
            file,
            "not allowed for TXT"
        );
    }

    @Test
    void shouldRejectInvalidUtf8() {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "invalid.txt",
            "text/plain",
            new byte[] {
                (byte) 0xC3,
                (byte) 0x28
            }
        );

        assertBadRequest(
            file,
            "valid UTF-8"
        );
    }

    @Test
    void shouldRejectNulCharacter() {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "binary.txt",
            "text/plain",
            "abc\u0000def"
                .getBytes(StandardCharsets.UTF_8)
        );

        assertBadRequest(
            file,
            "NUL characters"
        );
    }

    @Test
    void shouldRejectBinaryControlCharacters() {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "binary.txt",
            "text/plain",
            new byte[] {
                0x01,
                0x02,
                0x03,
                0x0A
            }
        );

        assertBadRequest(
            file,
            "binary control characters"
        );
    }

    @Test
    void shouldRejectBlankTextAfterBomRemoval() {
        String text = "\uFEFF   \r\n\t";

        MockMultipartFile file = new MockMultipartFile(
            "file",
            "blank.md",
            "text/markdown",
            text.getBytes(StandardCharsets.UTF_8)
        );

        assertBadRequest(
            file,
            "must not be blank"
        );
    }

    private void assertBadRequest(
            MultipartFile file,
            String expectedMessage) {
        assertThatThrownBy(() ->
            service.uploadVersion("KB-001", file)
        )
            .isInstanceOf(ResponseStatusException.class)
            .hasMessageContaining(expectedMessage)
            .satisfies(exception ->
                assertThat(
                    ((ResponseStatusException) exception)
                        .getStatusCode()
                ).isEqualTo(HttpStatus.BAD_REQUEST)
            );

        verifyNoInteractions(documentService);
    }
}