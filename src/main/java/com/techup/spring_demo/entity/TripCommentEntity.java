package com.techup.spring_demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "trip_comments")
public class TripCommentEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trip_id")
    private Long tripId;

    private String content;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    // 💡 ดึงข้อมูลคนคอมเมนต์มาด้วยเลย (จะได้เอาชื่อ/รูปมาโชว์ง่ายๆ)
    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity user;
}