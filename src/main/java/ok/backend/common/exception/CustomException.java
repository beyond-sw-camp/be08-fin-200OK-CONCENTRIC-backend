package ok.backend.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class CustomException extends RuntimeException {
    // 전역으로 사용하는 exception -> unchecked exception
    private final ErrorCode errorCode;
}
