package com.techup.pet_sitter.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import com.techup.pet_sitter.entity.SitterPhoto;


public interface SitterPhotoRepository extends JpaRepository<SitterPhoto, Long> {
}
