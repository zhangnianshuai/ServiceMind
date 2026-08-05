package com.zhangnianshuai.servicemind.api.knowledge;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

@RestControllerAdvice
public class KnowledgeUploadExceptionHandler {

    @ExceptionHandler(
        MaxUploadSizeExceededException.class
    )
    public ResponseEntity<ProblemDetail>
            handleMaxUploadSizeExceeded(
                MaxUploadSizeExceededException exception) {
        ProblemDetail problem = ProblemDetail
            .forStatusAndDetail(
                HttpStatus.PAYLOAD_TOO_LARGE,
                "Uploaded file exceeds the configured "
                    + "multipart size limit."
            );

        problem.setTitle(
            "Knowledge upload rejected"
        );

        return ResponseEntity
            .status(HttpStatus.PAYLOAD_TOO_LARGE)
            .body(problem);
    }
}