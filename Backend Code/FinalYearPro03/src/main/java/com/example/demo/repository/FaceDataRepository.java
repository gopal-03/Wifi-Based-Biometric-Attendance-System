package com.example.demo.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.example.demo.model.FaceData;

public interface FaceDataRepository extends JpaRepository<FaceData, Long> {
    FaceData findByUsername(String username);
}

