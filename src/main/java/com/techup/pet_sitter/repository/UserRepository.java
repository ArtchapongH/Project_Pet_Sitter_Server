package com.techup.pet_sitter.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import com.techup.pet_sitter.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {
}