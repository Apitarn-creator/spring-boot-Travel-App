package com.techup.spring_demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Data
@Entity
@Table(name = "users")
public class UserEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 💡 แมปตัวแปร username ใน Java เข้ากับคอลัมน์ display_name ใน DB
    @Column(name = "display_name", length = 100)
    private String username;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    // 💡 ใช้คอลัมน์ password ที่มีอยู่แล้ว
    @Column(name = "password_hash", columnDefinition = "text")
    private String password;

    // 💡 2 คอลัมน์นี้เดี๋ยว Spring Boot จะวิ่งไป ALTER TABLE เพิ่มให้เองอัตโนมัติครับ
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Role role = Role.USER;

    @Enumerated(EnumType.STRING)
    @Column(name = "auth_provider", nullable = false, length = 20)
    private AuthProvider authProvider;

    @Column(name = "provider_id", length = 255)
    private String providerId;

    @Column(name = "avatar_url", length = 255)
    private String avatarUrl;

    @Column(name = "created_at", updatable = false)
    private OffsetDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = OffsetDateTime.now();
    }
}