package com.techup.pet_sitter.repository;

import com.techup.pet_sitter.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
}
