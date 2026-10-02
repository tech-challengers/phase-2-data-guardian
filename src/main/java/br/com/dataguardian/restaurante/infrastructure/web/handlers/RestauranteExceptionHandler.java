package br.com.dataguardian.restaurante.infrastructure.web.handlers;

import br.com.dataguardian.restaurante.infrastructure.web.controllers.RestauranteController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

@RestControllerAdvice(assignableTypes = RestauranteController.class)
public class RestauranteExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail tratarDadosInvalidos(IllegalArgumentException exception) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problema.setTitle("Dados do restaurante inválidos");
        return problema;
    }
}
