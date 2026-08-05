package com.zhangnianshuai.servicemind.api.knowledge;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartFile;

import com.zhangnianshuai.servicemind.api.knowledge.dto.KnowledgeVersionDetailResponse;
import com.zhangnianshuai.servicemind.application.knowledge.KnowledgeDocumentService;
import com.zhangnianshuai.servicemind.application.knowledge.KnowledgeFileUploadService;
import com.zhangnianshuai.servicemind.application.knowledge.KnowledgePublishingService;

@ExtendWith(MockitoExtension.class)
class KnowledgeDocumentUploadControllerTest {

    @Mock
    private KnowledgeDocumentService documentService;

    @Mock
    private KnowledgePublishingService publishingService;

    @Mock
    private KnowledgeFileUploadService fileUploadService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        KnowledgeDocumentController controller =
            new KnowledgeDocumentController(
                documentService,
                publishingService,
                fileUploadService
            );

        mockMvc = MockMvcBuilders
            .standaloneSetup(controller)
            .setControllerAdvice(
                new KnowledgeUploadExceptionHandler()
            )
            .build();
    }

    @Test
    void shouldUploadMultipartKnowledgeVersion()
            throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "repair-v3.md",
            "text/markdown",
            "# Repair version 3"
                .getBytes(StandardCharsets.UTF_8)
        );

        KnowledgeVersionDetailResponse response =
            mock(KnowledgeVersionDetailResponse.class);

        when(response.versionNo()).thenReturn(3);

        when(fileUploadService.uploadVersion(
            eq("KB-001"),
            any(MultipartFile.class)
        )).thenReturn(response);

        mockMvc.perform(
                multipart(
                    "/api/knowledge/documents/"
                        + "KB-001/versions/upload"
                )
                    .file(file)
            )
            .andExpect(status().isCreated())
            .andExpect(header().string(
                "Location",
                "/api/knowledge/documents/"
                    + "KB-001/versions/3"
            ));

        verify(fileUploadService).uploadVersion(
            eq("KB-001"),
            any(MultipartFile.class)
        );
    }

    @Test
    void shouldRejectMissingMultipartFile()
            throws Exception {
        mockMvc.perform(
                multipart(
                    "/api/knowledge/documents/"
                        + "KB-001/versions/upload"
                )
            )
            .andExpect(status().isBadRequest());

        verifyNoInteractions(fileUploadService);
    }

    @Test
    void shouldReturnPayloadTooLargeForMultipartLimit()
            throws Exception {
        MockMultipartFile file = new MockMultipartFile(
            "file",
            "large.md",
            "text/markdown",
            "# Large".getBytes(StandardCharsets.UTF_8)
        );

        when(fileUploadService.uploadVersion(
            eq("KB-001"),
            any(MultipartFile.class)
        )).thenThrow(
            new MaxUploadSizeExceededException(
                2_097_152L
            )
        );

        mockMvc.perform(
                multipart(
                    "/api/knowledge/documents/"
                        + "KB-001/versions/upload"
                )
                    .file(file)
            )
            .andExpect(status().is(413))
            .andExpect(jsonPath("$.status").value(413))
            .andExpect(jsonPath("$.title").value(
                "Knowledge upload rejected"
            ))
            .andExpect(jsonPath("$.detail").value(
                "Uploaded file exceeds the configured "
                    + "multipart size limit."
            ));
    }
}