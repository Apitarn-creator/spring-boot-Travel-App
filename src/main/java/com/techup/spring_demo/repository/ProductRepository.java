package com.techup.spring_demo.repository;

import com.techup.spring_demo.entity.ProductEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
// เราเลือกใช้ JpaRepository โดยระบุ Entity ที่จะใช้ (ProductEntity) และประเภทของ ID (UUID)
public interface ProductRepository extends JpaRepository<ProductEntity, UUID> {
    // แค่นี้เลยครับ! JpaRepository จะเตรียมคำสั่ง save, findAll, findById, delete ให้เราพร้อมใช้ทันที
}