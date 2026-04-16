package com.techup.spring_demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.OffsetDateTime;

@Entity
@Table(name = "trips", schema = "public")
@Data
public class TripEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY) // ตรงกับ bigserial
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "text")
    private String description;

    // สำหรับ PostgreSQL Array ใน Spring Boot 3/Hibernate 6 สามารถใช้ String[] ได้เลย
    @Column(name = "photos", columnDefinition = "text[]")
    private String[] photos;

    @Column(name = "tags", columnDefinition = "text[]")
    private String[] tags;

    private Double latitude;
    private Double longitude;

    @Column(name = "author_id")
    private Long authorId;

    // ✅ like count
    @Column(name = "likes", nullable = false, columnDefinition = "integer default 0")
    private Integer likes = 0;

    // เก็บ userId ที่กด like ไว้เป็น comma-separated string เช่น "1,5,23"
    // ใช้ text แทน array เพื่อให้ Hibernate map ได้ง่าย
    @Column(name = "liked_by_str", columnDefinition = "text default ''")
    private String likedByStr = "";

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;
}