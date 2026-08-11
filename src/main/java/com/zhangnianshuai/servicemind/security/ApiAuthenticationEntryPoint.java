package com.zhangnianshuai.servicemind.security;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
public class ApiAuthenticationEntryPoint
        implements AuthenticationEntryPoint {

    private final SecurityProblemWriter problemWriter;

    public ApiAuthenticationEntryPoint(
            SecurityProblemWriter problemWriter) {
        this.problemWriter =
            problemWriter;
    }

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException exception)
            throws IOException, ServletException {

        problemWriter.write(
            response,
            HttpStatus.UNAUTHORIZED,
            "Authentication required",
            "Authentication is required to access this resource."
        );
    }
}