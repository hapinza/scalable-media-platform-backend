package io.github.catimental.diexample.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.Map;


@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(IllegalStateException.class) 
    public ResponseEntity<?> handleIllegalState(IllegalStateException e){
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(Map.of(
                    "success", false,
                    "error", e.getMessage()
            
            ));

    }

@ExceptionHandler(IllegalArgumentException.class)
public ResponseEntity<?> handleIllegalArgument(IllegalArgumentException e){
    return ResponseEntity
            .status(HttpStatus.BAD_REQUEST)
            .body(Map.of(
                "success", false,
                "error", e.getMessage()
            ));


}

@ExceptionHandler(Exception.class)
public ResponseEntity<?> handleOtherExceptions(Exception e){
    return ResponseEntity
            .status(HttpStatus.INTERNAL_SERVER_ERROR)
            .body(Map.of(
                "success", false,
                "error", "server error",
                "details", e.getMessage()
            ));
}



}
