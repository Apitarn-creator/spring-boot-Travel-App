package com.techup.spring_demo.controller;

import com.techup.spring_demo.entity.NotificationEntity;
import com.techup.spring_demo.entity.UserEntity;
import com.techup.spring_demo.repository.NotificationRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
@CrossOrigin(origins = {"http://localhost:5173", "https://localhost:5173"})
public class NotificationController {

    @Autowired
    private NotificationRepository notificationRepository;

    // ดึงการแจ้งเตือนทั้งหมดของตัวเอง
    @GetMapping
    public ResponseEntity<List<NotificationEntity>> getMyNotifications(
            org.springframework.security.core.Authentication authentication) {
        UserEntity me = (UserEntity) authentication.getPrincipal();
        List<NotificationEntity> list =
            notificationRepository.findByRecipientIdOrderByCreatedAtDesc(me.getId());
        // ซ่อน password ของ actor
        list.forEach(n -> { if (n.getActor() != null) n.getActor().setPassword(null); });
        return ResponseEntity.ok(list);
    }

    // นับ unread
    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(
            org.springframework.security.core.Authentication authentication) {
        UserEntity me = (UserEntity) authentication.getPrincipal();
        long count = notificationRepository.countByRecipientIdAndIsReadFalse(me.getId());
        return ResponseEntity.ok(Map.of("count", count));
    }

    // Mark all as read
    @PostMapping("/read-all")
    public ResponseEntity<?> markAllRead(
            org.springframework.security.core.Authentication authentication) {
        UserEntity me = (UserEntity) authentication.getPrincipal();
        notificationRepository.markAllAsRead(me.getId());
        return ResponseEntity.ok(Map.of("success", true));
    }

    // Mark single as read
    @PostMapping("/{id}/read")
    public ResponseEntity<?> markRead(
            @PathVariable Long id,
            org.springframework.security.core.Authentication authentication) {
        UserEntity me = (UserEntity) authentication.getPrincipal();
        notificationRepository.findById(id).ifPresent(n -> {
            if (n.getRecipientId().equals(me.getId())) {
                n.setIsRead(true);
                notificationRepository.save(n);
            }
        });
        return ResponseEntity.ok(Map.of("success", true));
    }
}
