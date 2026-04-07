package com.techup.spring_demo.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDate;

@Data
@Entity
@Table(name = "user_profiles")
public class UserProfileEntity {

    @Id
    @Column(name = "user_id")
    private Long userId;

    @Column(length = 100)
    private String nickname;

    @Column(columnDefinition = "text")
    private String bio;

    @Column(length = 20)
    private String gender;

    private LocalDate birthdate;

    @Column(name = "social_link", length = 255)
    private String socialLink;

    @Column(name = "cover_url", columnDefinition = "text")
    private String coverUrl;

    // 💡 เชื่อมความสัมพันธ์ 1-to-1 กลับไปหาตาราง users
    @OneToOne
    @MapsId // บอกให้ใช้ id เดียวกับ UserEntity
    @JoinColumn(name = "user_id")
    @JsonIgnore // ป้องกัน Error วนลูปตอนส่งข้อมูลกลับไปให้หน้าบ้าน (สำคัญมาก)
    private UserEntity user;
}