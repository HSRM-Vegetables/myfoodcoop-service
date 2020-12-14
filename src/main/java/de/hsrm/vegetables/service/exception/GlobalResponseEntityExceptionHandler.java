package de.hsrm.vegetables.service.exception;

import de.hsrm.vegetables.Stadtgemuese_Backend.model.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.ConversionNotSupportedException;
import org.springframework.beans.TypeMismatchException;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.http.converter.HttpMessageNotWritableException;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
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

@ControllerAdvice
//@Component
//@Order(Ordered.HIGHEST_PRECEDENCE)
public class GlobalResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

    Logger logger = LoggerFactory.getLogger(GlobalResponseEntityExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    protected ResponseEntity<Object> handleException(Exception exception) {
        return this.createException(exception.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_EXCEPTION);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleHttpRequestMethodNotSupported(HttpRequestMethodNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return this.createException(ex.getMessage(), status, ErrorCode.METHOD_NOT_ALLOWED);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleHttpMediaTypeNotSupported(HttpMediaTypeNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return this.createException(ex.getMessage(), status, ErrorCode.UNSUPPORTED_MEDIA_TYPE);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleHttpMediaTypeNotAcceptable(HttpMediaTypeNotAcceptableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return this.createException(ex.getMessage(), status, ErrorCode.NOT_ACCEPTABLE);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleMissingPathVariable(MissingPathVariableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return this.createException(ex.getMessage(), status, ErrorCode.MISSING_PATH_VARIABLE);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleMissingServletRequestParameter(MissingServletRequestParameterException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return this.createException(ex.getMessage(), status, ErrorCode.MISSING_SERVLET_REQUEST_PARAMETER);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleServletRequestBindingException(ServletRequestBindingException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return this.createException(ex.getMessage(), status, ErrorCode.SERVLET_REQUEST_BINDING_EXCEPTION);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleConversionNotSupported(ConversionNotSupportedException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return this.createException(ex.getMessage(), status, ErrorCode.CONVERSION_NOT_SUPPORTED);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleTypeMismatch(TypeMismatchException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return this.createException(ex.getMessage(), status, ErrorCode.TYPE_MISMATCH);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleHttpMessageNotReadable(HttpMessageNotReadableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return this.createException(ex.getMessage(), status, ErrorCode.MESSAGE_NOT_READABLE);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleHttpMessageNotWritable(HttpMessageNotWritableException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return this.createException(ex.getMessage(), status, ErrorCode.MESSAGE_NOT_WRITABLE);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return this.createException(ex.getMessage(), status, ErrorCode.METHOD_ARGUMENT_NOT_VALID);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleMissingServletRequestPart(MissingServletRequestPartException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return this.createException(ex.getMessage(), status, ErrorCode.MISSING_SERVLET_REQUEST_PART);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleBindException(BindException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return this.createException(ex.getMessage(), status, ErrorCode.BIND_EXCEPTION);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleNoHandlerFoundException(NoHandlerFoundException ex, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return this.createException(ex.getMessage(), status, ErrorCode.NO_HANDLER_FOUND);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleAsyncRequestTimeoutException(AsyncRequestTimeoutException ex, HttpHeaders headers, HttpStatus status, WebRequest webRequest) {
        return this.createException(ex.getMessage(), status, ErrorCode.ASYNC_REQUEST_TIMEOUT);
    }

    @Override
    @NonNull
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, @Nullable Object body, HttpHeaders headers, HttpStatus status, WebRequest request) {
        return this.createException(ex.getMessage(), status, ErrorCode.INTERNAL_EXCEPTION);
    }

    /**
     * Returns a ResponseEntity containing an Error Object
     * Logs the error
     *
     * @param errorMessage The message of the error
     * @param status The corresponding HTTP Status of the error
     * @param errorCode The specific ErrorCode for this error
     */
    private ResponseEntity<Object> createException(String errorMessage, HttpStatus status, ErrorCode errorCode) {
        if (errorMessage == null) {
            errorMessage = "";
        }

        logger.error(status + " || " + errorCode.getValue() + " || " + errorMessage);

        // Create response object
        ErrorResponse errorResponse = new ErrorResponse();
        errorResponse.setErrorCode(errorCode.getValue());
        errorResponse.setStatus(status.value());
        errorResponse.setErrorMessage(errorMessage);

        // Set response headers
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.set(HttpHeaders.CONTENT_TYPE, "application/problem+json");

        return new ResponseEntity<Object>(errorResponse, httpHeaders, status);
    }
}
