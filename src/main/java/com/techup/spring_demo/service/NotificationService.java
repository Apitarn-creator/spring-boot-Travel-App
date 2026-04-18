package com.techup.spring_demo.service;

import com.techup.spring_demo.entity.NotificationEntity;
import com.techup.spring_demo.entity.UserEntity;
import com.techup.spring_demo.repository.NotificationRepository;
import com.techup.spring_demo.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class NotificationService {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    // สร้าง notification — ใช้ร่วมกันทุก type
    public void create(Long recipientId, UserEntity actor, String type,
                       Long tripId, String tripTitle) {
        // ไม่แจ้งเตือนตัวเอง
        if (recipientId.equals(actor.getId())) return;

        NotificationEntity n = new NotificationEntity();
        n.setRecipientId(recipientId);
        n.setActor(actor);
        n.setType(type);
        n.setTripId(tripId);
        n.setTripTitle(tripTitle);
        notificationRepository.save(n);
    }

    // Shortcut สำหรับ LIKE
    public void notifyLike(Long tripAuthorId, UserEntity liker,
                           Long tripId, String tripTitle) {
        create(tripAuthorId, liker, "LIKE", tripId, tripTitle);
    }

    // Shortcut สำหรับ COMMENT
    public void notifyComment(Long tripAuthorId, UserEntity commenter,
                              Long tripId, String tripTitle) {
        create(tripAuthorId, commenter, "COMMENT", tripId, tripTitle);
    }

    // Shortcut สำหรับ FOLLOW
    public void notifyFollow(Long followedUserId, UserEntity follower) {
        create(followedUserId, follower, "FOLLOW", null, null);
    }
}
