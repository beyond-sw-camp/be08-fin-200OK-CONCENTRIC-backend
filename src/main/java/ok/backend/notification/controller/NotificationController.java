package ok.backend.notification.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import ok.backend.notification.service.NotificationService;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Tag(name = "Notification", description = "알림 기능")
@RestController
@RequiredArgsConstructor
@RequestMapping("v1/api/notification")
public class NotificationController {

    private final NotificationService notificationService;


}
