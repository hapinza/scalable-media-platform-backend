package io.github.catimental.diexample.exception;

// import org.springframework.http.ResponseEntity;
// import org.springframework.web.bind.annotation.ExceptionHandler;

public class ApiException extends RuntimeException {
    private final ErrorCode errorCode;

    public ApiException(ErrorCode errorCode, String message){
        super(message);
        this.errorCode = errorCode;
    }

    public ErrorCode getErrorCode(){
        return errorCode;
    }


    // @ExceptionHandler(ApiException.class)
    // public ResponseEntity<ErrorResponse> handleApi(ApiException e){
    //     var body = new ErrorResponse(e.getErrorCode().name(), e.getMessage());

    //     if(e.getErrorCode() == ErrorCode.ALREADY_WACHLISTED){
    //         return ResponseEntity.status(409).body(body);
    //     }

    //     if(e.getErrorCode() == ErrorCode.WATCHLIST_NOT_FOUND){
    //         return ResponseEntity.status(404).body(body);
    //     }


    //     return ReseponseEntity.badRequest().body(body);
        



    }



