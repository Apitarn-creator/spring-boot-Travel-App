package com.techup.spring_demo.service;

import com.techup.spring_demo.entity.TripEntity;
import com.techup.spring_demo.repository.TripRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class TripService {
    private final TripRepository tripRepository;

    public TripService(TripRepository tripRepository) {
        this.tripRepository = tripRepository;
    }

    public List<TripEntity> getAllTrips() {
        return tripRepository.findAll();
    }
}