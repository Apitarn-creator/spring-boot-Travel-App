package com.techup.spring_demo.repository;

import com.techup.spring_demo.entity.TripEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TripRepository extends JpaRepository<TripEntity, Long> {
    // JpaRepository จะสร้างคำสั่งพื้นฐานให้เราอัตโนมัติครับ
}