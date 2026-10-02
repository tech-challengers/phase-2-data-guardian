package br.com.dataguardian.restaurante.infrastructure.web.handlers;

import br.com.dataguardian.restaurante.infrastructure.web.controllers.RestauranteController;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import java.util.NoSuchElementException;

@RestControllerAdvice(assignableTypes = RestauranteController.class)
public class RestauranteExceptionHandler extends ResponseEntityExceptionHandler {

    @ExceptionHandler(NoSuchElementException.class)
    public ProblemDetail tratarRestauranteNaoEncontrado(NoSuchElementException exception) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problema.setTitle("Restaurante não encontrado");
        return problema;
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ProblemDetail tratarDadosInvalidos(IllegalArgumentException exception) {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, exception.getMessage());
        problema.setTitle("Dados do restaurante inválidos");
        return problema;
    }
}
