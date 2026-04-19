package com.techup.spring_demo.repository;

import com.techup.spring_demo.entity.TripCommentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface TripCommentRepository extends JpaRepository<TripCommentEntity, Long> {
    // คำสั่งหาคอมเมนต์ทั้งหมดของทริปนั้นๆ (เรียงจากใหม่ไปเก่า)
    List<TripCommentEntity> findByTripIdOrderByIdDesc(Long tripId);
}