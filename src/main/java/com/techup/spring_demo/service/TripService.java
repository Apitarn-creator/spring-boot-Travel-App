package com.techup.spring_demo.service;

import com.techup.spring_demo.entity.TripEntity;
import com.techup.spring_demo.repository.TripRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TripService {

    @Autowired
    private TripRepository tripRepository;

    // 1. ดึงข้อมูลทริปทั้งหมด (โชว์หน้าแรก)
    public List<TripEntity> getAllTrips(String keyword) {
        // ถ้ามีการพิมพ์คำค้นหามา ให้ไปเรียกคำสั่งค้นหาใน Repository
        if (keyword != null && !keyword.trim().isEmpty()) {
            return tripRepository.searchTrips(keyword);
        }
        // ถ้าไม่ได้ค้นหาอะไรเลย ให้ดึงทริปทั้งหมดตามปกติ
        return tripRepository.findAll();
    }

    // 2. ดึงข้อมูลทริปแค่ 1 รายการ (โชว์หน้าอ่านรายละเอียดทริป)
    public TripEntity getTripById(Long id) {
        return tripRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("ไม่พบข้อมูลทริปนี้"));
    }

    // 3. สร้างทริปใหม่
    public TripEntity createTrip(TripEntity trip) {
        return tripRepository.save(trip);
    }

    // ✅ บันทึกทริป (ใช้กับ like/unlike)
    public TripEntity saveTrip(TripEntity trip) {
        return tripRepository.save(trip);
    }

    // ดึงทริปที่ตัวเองสร้าง
    public List<TripEntity> getTripsByAuthor(Long authorId) {
        return tripRepository.findByAuthorIdOrderByIdDesc(authorId);
    }

    // ดึงทริปที่เคยไปคอมเมนต์
    public List<TripEntity> getTripsCommentedByUser(Long userId) {
        return tripRepository.findTripsCommentedByUser(userId);
    }
}