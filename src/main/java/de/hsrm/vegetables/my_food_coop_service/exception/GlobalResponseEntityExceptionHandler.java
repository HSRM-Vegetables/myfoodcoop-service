package de.hsrm.vegetables.my_food_coop_service.exception;

import com.auth0.jwt.exceptions.JWTDecodeException;
import com.auth0.jwt.exceptions.SignatureVerificationException;
import com.auth0.jwt.exceptions.TokenExpiredException;
import com.fasterxml.jackson.databind.exc.InvalidFormatException;
import de.hsrm.vegetables.my_food_coop_service.exception.errors.BaseError;
import de.hsrm.vegetables.my_food_coop_service.exception.errors.MaskedError;
import de.hsrm.vegetables.my_food_coop_service.exception.errors.http.BadRequestError;
import de.hsrm.vegetables.my_food_coop_service.exception.errors.http.NotFoundError;
import de.hsrm.vegetables.my_food_coop_service.exception.errors.http.UnauthorizedError;
import de.hsrm.vegetables.my_food_coop_service.model.ErrorDetail;
import de.hsrm.vegetables.my_food_coop_service.model.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.BindException;
import org.springframework.web.HttpMediaTypeNotAcceptableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingPathVariableException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.context.request.async.AsyncRequestTimeoutException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;
import org.springframework.web.servlet.NoHandlerFoundException;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.List;
import java.util.stream.Collectors;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    public static final String APPLICATION_PROBLEM_JSON = "application/problem+json";

    final Logger errorLogger = LoggerFactory.getLogger(GlobalResponseEntityExceptionHandler.class);

    /*
     Handler for own errors
     */
    @ExceptionHandler(BadRequestError.class)
    public ResponseEntity<Object> handleBadRequestError(BadRequestError error) {
        return createException(error, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(NotFoundError.class)
    public ResponseEntity<Object> handleNotFoundError(NotFoundError error) {
        return createException(error, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(UnauthorizedError.class)
    public ResponseEntity<Object> handleUnauthorizedError(UnauthorizedError error) {
        return createException(error, HttpStatus.UNAUTHORIZED);
    }

    // Handles all BaseErrors that were not specifically mapped here
    @ExceptionHandler(BaseError.class)
    public ResponseEntity<Object> handleBaseError(BaseError error) {
        return createException(new MaskedError(error.getErrorCode()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    /*
     Handler for security errors
     */

    @ExceptionHandler(JWTDecodeException.class)
    public ResponseEntity<Object> handleJwtDecodeException(JWTDecodeException error) {
        return createException("Invalid Authorization token", HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_JWT);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Object> handleAccessDenied(AccessDeniedException error) {
        return createException("Access denied", HttpStatus.UNAUTHORIZED, ErrorCode.ACCESS_DENIED);
    }

    @ExceptionHandler(SignatureVerificationException.class)
    public ResponseEntity<Object> handleSignatureVerificationException(SignatureVerificationException error) {
        return createException("Signature for token in Authorization header is invalid", HttpStatus.UNAUTHORIZED, ErrorCode.INVALID_TOKEN_SIGNATURE);
    }

    @ExceptionHandler(TokenExpiredException.class)
    public ResponseEntity<Object> handleTokenExpiredException(TokenExpiredException error) {
        return createException("The token is expired", HttpStatus.UNAUTHORIZED, ErrorCode.TOKEN_EXPIRED);
    }

    /*
     Handler for Spring-Related errors
     */

    // All errors not specifically mapped
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleException(Exception exception) {
        exception.printStackTrace();
        return createException("An Internal Server Error occurred", HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_EXCEPTION);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, @NonNull HttpHeaders headers, @NonNull HttpStatus status, @NonNull WebRequest request) {
        return createException(ex.getMessage(), status, ErrorCode.METHOD_NOT_ALLOWED);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, @NonNull HttpHeaders headers, @NonNull HttpStatus status, @NonNull WebRequest request) {
        return createException(ex.getMessage(), status, ErrorCode.UNSUPPORTED_MEDIA_TYPE);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex, @NonNull HttpHeaders headers, @NonNull HttpStatus status, @NonNull WebRequest request) {
        return createException(ex.getMessage(), status, ErrorCode.NOT_ACCEPTABLE);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleMissingPathVariable(MissingPathVariableException ex, @NonNull HttpHeaders headers, @NonNull HttpStatus status, @NonNull WebRequest request) {
        return createException(ex.getMessage(), status, ErrorCode.MISSING_PATH_VARIABLE);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, @NonNull HttpHeaders headers, @NonNull HttpStatus status, @NonNull WebRequest request) {
        return createException(ex.getMessage(), status, ErrorCode.MISSING_SERVLET_REQUEST_PARAMETER);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleServletRequestBindingException(ServletRequestBindingException ex, @NonNull HttpHeaders headers, @NonNull HttpStatus status, @NonNull WebRequest request) {
        return createException(ex.getMessage(), status, ErrorCode.SERVLET_REQUEST_BINDING_EXCEPTION);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleConversionNotSupported(ConversionNotSupportedException ex, @NonNull HttpHeaders headers, @NonNull HttpStatus status, @NonNull WebRequest request) {
        return createException(ex.getMessage(), status, ErrorCode.CONVERSION_NOT_SUPPORTED);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, @NonNull HttpHeaders headers, @NonNull HttpStatus status, @NonNull WebRequest request) {
        return createException(ex.getMessage(), status, ErrorCode.TYPE_MISMATCH);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, @NonNull HttpHeaders headers, @NonNull HttpStatus status, @NonNull WebRequest request) {
        Throwable specificException = ex.getMostSpecificCause();

        if (specificException instanceof InvalidFormatException) {
            return createException("Invalid JSON: " + specificException.getMessage()
                    .split("\n")[0], status, ErrorCode.MESSAGE_NOT_READABLE);
        }

        ex.printStackTrace();

        return createException("Invalid Body, check Specification", status, ErrorCode.MESSAGE_NOT_READABLE);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleHttpMessageNotWritable(HttpMessageNotWritableException ex, @NonNull HttpHeaders headers, @NonNull HttpStatus status, @NonNull WebRequest request) {
        return createException(ex.getMessage(), status, ErrorCode.MESSAGE_NOT_WRITABLE);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleMethodArgumentNotValid(@NonNull MethodArgumentNotValidException ex, @NonNull HttpHeaders headers, @NonNull HttpStatus status, @NonNull WebRequest request) {
        return createException(ex, status);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleMissingServletRequestPart(MissingServletRequestPartException ex, @NonNull HttpHeaders headers, @NonNull HttpStatus status, @NonNull WebRequest request) {
        return createException(ex.getMessage(), status, ErrorCode.MISSING_SERVLET_REQUEST_PART);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleBindException(BindException ex, @NonNull HttpHeaders headers, @NonNull HttpStatus status, @NonNull WebRequest request) {
        return createException(ex.getMessage(), status, ErrorCode.BIND_EXCEPTION);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, @NonNull HttpHeaders headers, @NonNull HttpStatus status, @NonNull WebRequest request) {
        return createException(ex.getMessage(), status, ErrorCode.NO_HANDLER_FOUND);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleAsyncRequestTimeoutException(AsyncRequestTimeoutException ex, @NonNull HttpHeaders headers, @NonNull HttpStatus status, @NonNull WebRequest webRequest) {
        return createException(ex.getMessage(), status, ErrorCode.ASYNC_REQUEST_TIMEOUT);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body, @NonNull HttpHeaders headers, @NonNull HttpStatus status, @NonNull WebRequest request) {
        // print stack trace on internal errors for debugging
        ex.printStackTrace();
        // Do not respond with any message concerning internal errors
        return createException("An Internal Server Error occurred", status, ErrorCode.INTERNAL_EXCEPTION);
    }


    /*
     Helper functions to create the proper responses
     */


    /**
     * Returns a ResponseEntity containing an Error Object
     * Logs the error
     *
     * @param errorMessage The message of the error
     * @param status       The corresponding HTTP Status of the error
     * @param errorCode    The specific ErrorCode for this error
     */
    private ResponseEntity<Object> createException(String errorMessage, HttpStatus status, ErrorCode errorCode) {
        if (errorMessage == null) {
            errorMessage = "";
        }

        errorLogger.error("{} \t\t|| {} \t\t|| {}", status, errorCode.getValue(), errorMessage);

        // Create response object
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorCode(errorCode.getValue());
        errorResponse.setStatus(status.value());
        errorResponse.setMessage(errorMessage);

        // Set response headers
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.CONTENT_TYPE, APPLICATION_PROBLEM_JSON);

        return new ResponseEntity<>(errorResponse, httpHeaders, status);
    }

    private ResponseEntity<Object> createException(BaseError error, HttpStatus status) {
        errorLogger.error("{} \t\t|| {} \t\t|| {}", status, error.getErrorCode()
                .getValue(), error.getMessage());

        // Create response object
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorCode(error.getErrorCode()
                .getValue());
        errorResponse.setStatus(status.value());
        errorResponse.setMessage(error.getMessage());

        // Set response headers
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.CONTENT_TYPE, APPLICATION_PROBLEM_JSON);

        return new ResponseEntity<>(errorResponse, httpHeaders, status);
    }

    private ResponseEntity<Object> createException(MethodArgumentNotValidException exception, HttpStatus status) {
        errorLogger.error("{} \t\t|| {} \t\t|| Validation failed for: {}", status, ErrorCode.METHOD_ARGUMENT_NOT_VALID.getValue(), exception.getParameter());

        List<ErrorDetail> details = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError -> {
                    ErrorDetail detail = new ErrorDetail();
                    detail.setMessage("Invalid parameter or body member: " + fieldError.getField());
                    detail.setDetail("The value " + fieldError.getRejectedValue() + (fieldError.isBindingFailure() ? " was rejected. Please check the specification" : " does not match the specification"));
                    return detail;
                })
                .collect(Collectors.toList());

        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorCode(ErrorCode.METHOD_ARGUMENT_NOT_VALID.getValue());
        errorResponse.setStatus(status.value());
        errorResponse.setMessage("Validation failed for: " + exception.getParameter());
        errorResponse.setDetails(details);

        // Set response headers
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.CONTENT_TYPE, APPLICATION_PROBLEM_JSON);

        return new ResponseEntity<>(errorResponse, httpHeaders, status);
    }
}
