package com.techup.spring_demo.controller;

import com.techup.spring_demo.entity.TripEntity;
import com.techup.spring_demo.service.TripService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/trips")
@CrossOrigin(origins = "http://localhost:5173") // อนุญาตให้หน้าบ้าน Vue เข้าถึงได้
public class TripController {

    private final TripService tripService;

    public TripController(TripService tripService) {
        this.tripService = tripService;
    }

    @GetMapping
    public List<TripEntity> getAllTrips() {
        return tripService.getAllTrips();
    }
}