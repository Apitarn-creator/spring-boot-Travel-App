package com.techup.spring_demo.repository;

import com.techup.spring_demo.entity.NotificationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<NotificationEntity, Long> {

    // ดึงการแจ้งเตือนทั้งหมดของ user เรียงใหม่สุดก่อน
    List<NotificationEntity> findByRecipientIdOrderByCreatedAtDesc(Long recipientId);

    // นับ unread
    long countByRecipientIdAndIsReadFalse(Long recipientId);

    // mark all as read
    @Modifying
    @Transactional
    @Query("UPDATE NotificationEntity n SET n.isRead = true WHERE n.recipientId = :recipientId")
    void markAllAsRead(@Param("recipientId") Long recipientId);
}
