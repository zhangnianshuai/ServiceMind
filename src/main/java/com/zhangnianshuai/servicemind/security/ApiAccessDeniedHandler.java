package com.zhangnianshuai.servicemind.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
public class ApiAccessDeniedHandler
        implements AccessDeniedHandler {

    private final SecurityProblemWriter problemWriter;

    public ApiAccessDeniedHandler(
            SecurityProblemWriter problemWriter) {
        this.problemWriter =
            problemWriter;
    }

    @Override
    public void handle(
            HttpServletRequest request,
            HttpServletResponse response,
            AccessDeniedException exception)
            throws IOException, ServletException {

        problemWriter.write(
            response,
            HttpStatus.FORBIDDEN,
            "Access denied",
            "You do not have permission to perform this operation."
        );
    }
}