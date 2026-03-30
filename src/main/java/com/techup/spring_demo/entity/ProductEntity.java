package com.techup.spring_demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.util.UUID;

@Entity // บอกว่าคลาสนี้คือตารางในฐานข้อมูล
@Table(name = "products") // ตั้งชื่อตารางว่า products
@Data // ใช้ Lombok เพื่อสร้าง Getter/Setter ให้อัตโนมัติ
public class ProductEntity {

    @Id // กำหนดให้เป็น Primary Key
    @GeneratedValue(strategy = GenerationType.UUID) // ให้ระบบสร้าง ID แบบ UUID ให้อัตโนมัติ
    private UUID id;

    @Column(nullable = false) // ห้ามเป็นค่าว่าง
    private String name;

    private Double price;

    private String description;

    private String category;
}