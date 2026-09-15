package com.techup.pet_sitter.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import com.techup.pet_sitter.entity.SitterProfile;
import com.techup.pet_sitter.entity.SitterPetType;
import com.techup.pet_sitter.entity.SitterPhoto;
import com.techup.pet_sitter.entity.User;

public interface SitterProfileRepository extends JpaRepository<SitterProfile, Long> {
}
