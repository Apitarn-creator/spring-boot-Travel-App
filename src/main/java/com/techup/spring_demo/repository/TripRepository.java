package com.techup.spring_demo.repository;

import com.techup.spring_demo.entity.TripEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TripRepository extends JpaRepository<TripEntity, Long> {
    
    // 1. หาโพสต์ที่ตัวเองเป็นคนสร้าง (เรียงจากใหม่ไปเก่า)
    List<TripEntity> findByAuthorIdOrderByIdDesc(Long authorId);

    // 2. หาโพสต์ที่ตัวเองเคยไปคอมเมนต์ (ใช้ Subquery ดึงเฉพาะทริปที่มีคอมเมนต์ของเรา)
    @Query("SELECT t FROM TripEntity t WHERE t.id IN (SELECT c.tripId FROM TripCommentEntity c WHERE c.user.id = :userId) ORDER BY t.id DESC")
    List<TripEntity> findTripsCommentedByUser(@Param("userId") Long userId);

    @Query(value = "SELECT * FROM trips WHERE title ILIKE CONCAT('%', :keyword, '%') OR CAST(tags AS text) ILIKE CONCAT('%', :keyword, '%') ORDER BY id DESC", nativeQuery = true)
    List<TripEntity> searchTrips(@Param("keyword") String keyword);
}