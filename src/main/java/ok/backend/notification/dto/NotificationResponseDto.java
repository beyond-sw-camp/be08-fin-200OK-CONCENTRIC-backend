package ok.backend.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import ok.backend.notification.domain.entity.Notification;

import java.time.LocalDateTime;

@Getter
public class NotificationResponseDto {

    @JsonProperty
    private Long id;

    @JsonProperty
    private String message;

    @JsonProperty
    private LocalDateTime createDate;

    @JsonProperty
    private Boolean isRead;

    public NotificationResponseDto(Notification notification) {
        this.id = notification.getId();
        this.message = notification.getMessage();
        this.createDate = notification.getCreateDate();
        this.isRead = notification.getIsRead();
    }
}
