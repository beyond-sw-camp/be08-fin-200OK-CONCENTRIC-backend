package ok.backend.notification.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.persistence.Column;
import lombok.Getter;
import ok.backend.notification.domain.entity.Notification;
import ok.backend.notification.domain.enums.NotificationType;

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

    @JsonProperty
    private NotificationType notificationType;

    @JsonProperty
    private String image;

    public NotificationResponseDto(Notification notification) {
        this.id = notification.getId();
        this.message = notification.getMessage();
        this.createDate = notification.getCreateDate();
        this.isRead = notification.getIsRead();
        this.notificationType = notification.getNotificationType();
        this.image = notification.getImage();
    }
}
