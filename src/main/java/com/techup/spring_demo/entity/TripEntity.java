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

    @Column(name = "created_at", insertable = false, updatable = false)
    private OffsetDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private OffsetDateTime updatedAt;
}