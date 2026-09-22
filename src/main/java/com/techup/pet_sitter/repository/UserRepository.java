package com.techup.pet_sitter.repository;


import org.springframework.data.jpa.repository.JpaRepository;
import com.techup.pet_sitter.entity.User;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmailIgnoreCase(String email);

    boolean existsByPhone(String phone);

    boolean existsByPhoneAndIdNot(String phone, UUID id);
}