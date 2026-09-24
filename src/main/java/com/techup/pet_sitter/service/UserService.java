package com.techup.pet_sitter.service;

import com.techup.pet_sitter.entity.User;
import com.techup.pet_sitter.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User create(User user) {
        if (user.getId() == null) {
            user.setId(UUID.randomUUID());
        }
        return userRepository.save(user);
    }

    public List<User> getAll() {
        return userRepository.findAll();
    }

    public User getById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + id));
    }

    public User update(UUID id, User updated) {
        User existing = getById(id);
        existing.setName(updated.getName());
        existing.setEmail(updated.getEmail());
        existing.setPhone(updated.getPhone());
        existing.setIdNumber(updated.getIdNumber());
        existing.setDateOfBirth(updated.getDateOfBirth());
        existing.setAvatarUrl(updated.getAvatarUrl());
        existing.setAdmin(updated.isAdmin());
        existing.setVerified(updated.isVerified());
        existing.setBanned(updated.isBanned());
        return userRepository.save(existing);
    }

    public void delete(UUID id) {
        User existing = getById(id);
        userRepository.delete(existing);
    }
}
