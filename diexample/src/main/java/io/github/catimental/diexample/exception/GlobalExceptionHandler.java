package io.github.catimental.diexample.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;



@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ErrorResponse> handleApi(ApiException e){
        var body = new ErrorResponse(e.getErrorCode().name(), e.getMessage());
       
        if(e.getErrorCode() == ErrorCode.RATE_LIMITED){
            return ResponseEntity.status(429).body(body);
        }
        if(e.getErrorCode() == ErrorCode.ALREADY_WACHLISTED){
            return ResponseEntity.status(409).body(body);
        }
        if(e.getErrorCode() == ErrorCode.WATCHLIST_NOT_FOUND){{
            return ResponseEntity.status(404).body(body);
        }


        return ResponseEntity.badRequest().body(body);
    }













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
