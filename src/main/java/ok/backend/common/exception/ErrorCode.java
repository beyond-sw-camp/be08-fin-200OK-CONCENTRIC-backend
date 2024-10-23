package ok.backend.common.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@AllArgsConstructor
@Getter
public enum ErrorCode {

    // 공통
    BAD_REQUEST(HttpStatus.BAD_REQUEST, "SYSTEM_001", "잘못된 요청입니다."),
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYSTEM_002","서버에서 요청을 처리할 수 없습니다."),
    DATABASE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "SYSTEM_003","데이터베이스 오류가 발생했습니다."),
    RATE_LIMIT_EXCEEDED(HttpStatus.TOO_MANY_REQUESTS, "SYSTEM_004","요청 제한을 초과했습니다."),

    // 계정
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "USER_001", "사용자 인증에 실패 하였습니다."),
    TOKEN_NOT_EXIST(HttpStatus.UNAUTHORIZED, "USER_002", "토큰이 존재하지 않습니다."),
    INVALID_VERIFICATION_TOKEN(HttpStatus.UNAUTHORIZED, "USER_003", "토큰이 유효하지 않습니다."),
    EXPIRED_VERIFICATION_TOKEN(HttpStatus.UNAUTHORIZED, "USER_004", "토큰의 유효기간이 만료 되었습니다."),
    REFRESH_TOKEN_NOT_EXIST(HttpStatus.UNAUTHORIZED, "USER_010", "리프레시 토큰이 존재하지 않습니다."),

    // 회원
    DUPLICATE_SIGNUP_ID(HttpStatus.CONFLICT, "USER_005", "사용자 이메일이 중복 되었습니다."),
    MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "USER_006", "사용자를 찾을 수 없습니다."),
    INVALID_PASSWORD(HttpStatus.BAD_REQUEST, "USER_007", "사용자의 패스워드가 일치하지 않습니다."),
    EMPTY_INPUT_MEMBER(HttpStatus.BAD_REQUEST, "USER_008", "공백을 사용할 수 없습니다."),
    MEMBER_DELETED(HttpStatus.FORBIDDEN, "USER_009", "탈퇴한 사용자입니다."),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "USER_010", "중복된 닉네임입니다."),

    DUPLICATE_SOCIAL(HttpStatus.CONFLICT, "SOCIAL_001", "이미 등록된 사용자입니다."),
    DUPLICATE_SOCIAL_REQUEST(HttpStatus.BAD_REQUEST, "SOCIAL_002", "이미 요청한 내역입니다."),
    INVALID_SOCIAL_REQUEST(HttpStatus.BAD_REQUEST, "SOCIAL_003", "자기 자신은 친구로 등록할 수 없습니다."),
    FRIENDSHIP_REQUEST_NOT_FOUND(HttpStatus.NOT_FOUND, "SOCIAL_004", "존재하지 않는 요청입니다."),

    // 팀
    DUPLICATE_TEAM_REQUEST(HttpStatus.CONFLICT, "TEAM_001", "이미 존재하는 그룹입니다."),
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "TEAM_002", "그룹을 찾을 수 없습니다."),
    DUPLICATE_TEAM(HttpStatus.CONFLICT, "TEAM_003", "이미 가입된 그룹입니다."),
    NOT_ACCESS_TEAM(HttpStatus.FORBIDDEN, "TEAM_004", "권한이 없는 사용자입니다."),

    // 일정
    SCHEDULE_NOT_FOUND(HttpStatus.NOT_FOUND, "SCHEDULE_001", "일정을 찾을 수 없습니다."),
    DUPLICATE_SCHEDULE(HttpStatus.CONFLICT, "SCHEDULE_002", "이미 존재하는 일정입니다."),
    NOT_ACCESS_SCHEDULE(HttpStatus.FORBIDDEN, "SCHEDULE_003", "권한이 없는 사용자입니다."),
    EMPTY_INPUT_SCHEDULE(HttpStatus.BAD_REQUEST, "SCHEDULE_004", "필수 입력값이 입력되지 않았습니다."),
    INVALID_SCHEDULE_REQUEST(HttpStatus.BAD_REQUEST, "SCHEDULE_005", "유효하지 않은 날짜입니다."),

    // 채팅
    CHAT_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_001", "채팅방을 찾을 수 없습니다."),
    DUPLICATE_CHAT(HttpStatus.CONFLICT, "CHAT_002", "이미 참여 중인 채팅방입니다."),
    INVALID_CHAT_REQUEST(HttpStatus.BAD_REQUEST, "CHAT_003", "자기 자신에게 요청을 보낼 수 없습니다."),
    NOT_ACCESS_CHAT(HttpStatus.FORBIDDEN, "CHAT_004", "권한이 없는 사용자입니다."),
    EMPTY_INPUT_CHAT(HttpStatus.BAD_REQUEST, "CHAT_005", "메세지가 입력되지 않았습니다."),
    UNSUPPORTED_MEDIA_TYPE_CHAT(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "CHAT_006", "지원하지 않는 미디어 유형입니다."),
    MESSAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_007", "메세지를 찾을 수 없습니다."),
    INVALID_DELETE_REQUEST(HttpStatus.BAD_REQUEST, "CHAT_009", "그룹 생성자는 채팅방을 탈퇴할 수 없습니다."),
    CONNECTION_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT_010", "마지막 연결 내역을 찾을 수 없습니다."),

    // 알림
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "NOTIFICATION_001", "알림을 찾을 수 없습니다."),

    // 파일
    STORAGE_NOT_FOUND(HttpStatus.NOT_FOUND, "STORAGE_001", "파일함을 찾을 수 없습니다."),
    STORAGE_CAPACITY_EXCEED(HttpStatus.BAD_REQUEST, "STORAGE_002", "파일함 용량이 부족합니다."),
    STORAGE_FILE_SIZE_EXCEED(HttpStatus.BAD_REQUEST, "STORAGE_003", "업로드 가능한 파일의 크기는 10MB 이하입니다."),
    STORAGE_REQUEST_SIZE_EXCEED(HttpStatus.BAD_REQUEST, "STORAGE_004", "한번에 업로드 가능한 크기는 50MB 이하입니다"),
    STORAGE_FILE_NOT_MATCHED(HttpStatus.NOT_FOUND, "STORAGE_005", "다른 파일함의 파일에 접근할 수 없습니다."),
    STORAGE_FILE_NOT_FOUND(HttpStatus.NOT_FOUND, "STORAGE_006", "파일을 찾을 수 없습니다.");

    private final HttpStatus httpStatus;
    private final String code;
    private final String message;

}
