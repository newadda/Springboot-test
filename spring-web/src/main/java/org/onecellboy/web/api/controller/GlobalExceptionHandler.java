package org.onecellboy.web.api.controller;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.common.exceptions.UnapprovedClientAuthenticationException;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedClientException;
import org.springframework.security.oauth2.common.exceptions.UnauthorizedUserException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import javax.servlet.http.HttpServletResponse;

@ControllerAdvice
@RestController
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {
    @ResponseStatus(HttpStatus.CONFLICT)  // 409
    @ExceptionHandler({UnauthorizedClientException.class, UnauthorizedUserException.class, UnapprovedClientAuthenticationException.class,Exception.class,RuntimeException.class})
    private ResponseEntity< String > error(final Exception exception, final HttpStatus httpStatus, final String logRef) {

        return new ResponseEntity < > (new String( ""), httpStatus);
    }
}