package com.techup.pet_sitter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.techup.pet_sitter.entity.SitterProfile;

import java.util.UUID;

public interface SitterProfileRepository extends JpaRepository<SitterProfile, UUID> {
}
