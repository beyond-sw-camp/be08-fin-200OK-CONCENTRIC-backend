package ok.backend.notification.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import ok.backend.notification.dto.NotificationResponseDto;
import ok.backend.notification.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Notification", description = "알림 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("v1/api/notification")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "알림 조회 API", description = "회원이 받은 알림을 전부 조회한다.")
    @GetMapping("/list")
    public ResponseEntity<List<NotificationResponseDto>> getAllNotifications() {
        List<NotificationResponseDto> notifications = notificationService.getAllNotifications();

        return ResponseEntity.ok(notifications);
    }
}
