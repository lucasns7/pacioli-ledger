package com.lucas.pacioli_ledger.security;

import jakarta.servlet.ServletException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class SecurityExceptionHandlerTest {

    private SecurityExceptionHandler handler;
    private MockHttpServletRequest request;
    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        handler = new SecurityExceptionHandler();

        request = new MockHttpServletRequest();
        request.setRequestURI("/api/accounts");

        response = new MockHttpServletResponse();
    }

    @Test
    void shouldReturnUnauthorizedProblemDetail() throws ServletException, IOException {

        var exception = new InsufficientAuthenticationException("Authentication is required.");

        handler.commence(request, response, exception);

        assertEquals(HttpStatus.UNAUTHORIZED.value(), response.getStatus());

        assertEquals(
                "application/problem+json",
                response.getContentType().split(";")[0]
        );

        assertEquals("UTF-8", response.getCharacterEncoding());

        String body = response.getContentAsString();

        assertTrue(body.contains("\"title\": \"Unauthorized\""));
        assertTrue(body.contains("\"status\": 401"));
        assertTrue(body.contains("Authentication is required to access this resource."));
        assertTrue(body.contains("\"instance\": \"/api/accounts\""));
    }

    @Test
    void shouldReturnForbiddenProblemDetail() throws ServletException, IOException {

        var exception = new AccessDeniedException("Access denied.");

        handler.handle(request, response, exception);

        assertEquals(HttpStatus.FORBIDDEN.value(), response.getStatus());

        assertEquals(
                "application/problem+json",
                response.getContentType().split(";")[0]
        );

        assertEquals("UTF-8", response.getCharacterEncoding());

        String body = response.getContentAsString();

        assertTrue(body.contains("\"title\": \"Forbidden\""));
        assertTrue(body.contains("\"status\": 403"));
        assertTrue(body.contains("You do not have permission to access this resource."));
        assertTrue(body.contains("\"instance\": \"/api/accounts\""));
    }

    @Test
    void shouldNotWriteBodyWhenResponseIsAlreadyCommitted() throws ServletException, IOException {

        response.getWriter().write("Existing response");
        response.setCommitted(true);

        var exception = new InsufficientAuthenticationException("Authentication is required.");

        handler.commence(request, response, exception);

        assertEquals("Existing response", response.getContentAsString());
    }

    @Test
    void shouldEscapeSpecialAndControlCharactersInJson() throws ServletException, IOException {

        String requestUri = "/api/accounts/" + (char) 1 + "\"\\segment";

        request.setRequestURI(requestUri);

        var exception = new InsufficientAuthenticationException("Authentication is required.");

        handler.commence(request, response, exception);

        var objectMapper = new ObjectMapper();
        var json = objectMapper.readTree(response.getContentAsString());

        assertEquals(requestUri, json.get("instance").asText());
    }
}
