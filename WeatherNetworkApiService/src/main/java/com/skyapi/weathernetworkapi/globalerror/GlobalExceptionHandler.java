package com.skyapi.weathernetworkapi.globalerror;

import com.skyapi.weathernetworkapi.location.LocationNotFoundException;
import com.skyapi.weathernetworkapi.realtimeweather.RealtimeWeatherNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.time.LocalDateTime;
import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler extends ResponseEntityExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(Exception.class)
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ResponseBody
    public ErrorDTO handlerGenericException(HttpServletRequest servletRequest, Exception ex) {
        ErrorDTO error = new ErrorDTO();
        error.setTimestamp(LocalDateTime.now());
        error.setStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
        error.addError(HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        error.setPath(servletRequest.getServletPath());

        LOGGER.error(ex.getMessage(), ex);

        return error;
    }

    @ExceptionHandler(BadRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorDTO handleBadRequestException(HttpServletRequest servletRequest, Exception ex) {
        ErrorDTO error = new ErrorDTO();
        error.setTimestamp(LocalDateTime.now());
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.addError(ex.getMessage());
        error.setPath(servletRequest.getServletPath());

        LOGGER.error(ex.getMessage(), ex);

        return error;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorDTO handleConstrainViolationException(HttpServletRequest servletRequest, Exception ex) {
        ErrorDTO error = new ErrorDTO();

        ConstraintViolationException violationException = (ConstraintViolationException) ex;

        error.setTimestamp(LocalDateTime.now());
        error.setStatus(HttpStatus.BAD_REQUEST.value());;
        error.setPath(servletRequest.getServletPath());

        var constraintViolations = violationException.getConstraintViolations();

        constraintViolations.forEach(constraint -> {
            error.addError(constraint.getPropertyPath() + " : " + constraint.getMessage());
        });

        LOGGER.error(ex.getMessage(), ex);

        return error;
    }

    @ExceptionHandler(LocationNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorDTO handleLocationNotFoundException (HttpServletRequest servletRequest, Exception ex) {

        ErrorDTO error = new ErrorDTO();

        error.setTimestamp(LocalDateTime.now());
        error.setStatus(HttpStatus.NOT_FOUND.value());;
        error.addError(ex.getMessage());
        error.setPath(servletRequest.getServletPath());

        LOGGER.error(ex.getMessage(), ex);

        return error;
    }

    @ExceptionHandler(RealtimeWeatherNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorDTO handleRealtimeWeatherNotFoundException (HttpServletRequest servletRequest, Exception ex) {
        ErrorDTO error = new ErrorDTO();

        error.setTimestamp(LocalDateTime.now());
        error.setStatus(HttpStatus.NOT_FOUND.value());;
        error.addError(ex.getMessage());
        error.setPath(servletRequest.getServletPath());

        LOGGER.error(ex.getMessage(), ex);

        return error;
    }

    @ExceptionHandler(GeolocatioException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ResponseBody
    public ErrorDTO handleGeolocatioException (HttpServletRequest servletRequest, Exception ex) {
        ErrorDTO error = new ErrorDTO();

        error.setTimestamp(LocalDateTime.now());
        error.setStatus(HttpStatus.NOT_FOUND.value());;
        error.addError(ex.getMessage());
        error.setPath(servletRequest.getServletPath());

        LOGGER.error(ex.getMessage(), ex);

        return error;
    }

    @ExceptionHandler(NumberFormatException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ResponseBody
    public ErrorDTO handleNumberFormatException (HttpServletRequest servletRequest, Exception ex) {
        ErrorDTO error = new ErrorDTO();

        error.setTimestamp(LocalDateTime.now());
        error.setStatus(HttpStatus.BAD_REQUEST.value());;
        error.addError(ex.getMessage());
        error.setPath(servletRequest.getServletPath());

        LOGGER.error(ex.getMessage(), ex);

        return error;
    }

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, HttpHeaders headers, HttpStatusCode status, WebRequest request) {

        ErrorDTO error = new ErrorDTO();
        error.setTimestamp(LocalDateTime.now());
        error.setStatus(HttpStatus.BAD_REQUEST.value());
        error.setPath(((ServletWebRequest)request).getRequest().getServletPath());

        List<FieldError> fieldErrors = ex.getBindingResult().getFieldErrors();

        fieldErrors.forEach(fieldError -> {
            error.addError(fieldError.getDefaultMessage());
        });

        LOGGER.error(ex.getMessage(), ex);

        return new ResponseEntity<>(error, headers, status);
    }
}
