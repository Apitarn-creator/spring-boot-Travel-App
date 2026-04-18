package com.techup.spring_demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "notifications")
public class NotificationEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // ผู้รับแจ้งเตือน
    @Column(name = "recipient_id", nullable = false)
    private Long recipientId;

    // ผู้ก่อเหตุ (คนที่ like/comment/follow)
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "actor_id")
    private UserEntity actor;

    // ประเภท: LIKE, COMMENT, FOLLOW
    @Column(nullable = false, length = 20)
    private String type;

    // trip ที่เกี่ยวข้อง (null ถ้าเป็น FOLLOW)
    @Column(name = "trip_id")
    private Long tripId;

    // ชื่อทริปสำหรับแสดงใน notification
    @Column(name = "trip_title", length = 255)
    private String tripTitle;

    // อ่านแล้วหรือยัง
    @Column(name = "is_read", nullable = false)
    private Boolean isRead = false;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }
}
