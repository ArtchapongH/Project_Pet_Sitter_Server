package com.techup.pet_sitter.service;

import com.techup.pet_sitter.entity.PetType;
import com.techup.pet_sitter.repository.PetTypeRepository;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class PetTypeSeeder implements ApplicationRunner {
    private final PetTypeRepository petTypes;

    public PetTypeSeeder(PetTypeRepository petTypes) {
        this.petTypes = petTypes;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (petTypes.count() > 0) {
            return;
        }
        for (String name : List.of("Dog", "Cat", "Bird", "Rabbit")) {
            if (petTypes.findByName(name).isEmpty()) {
                PetType type = new PetType();
                type.setName(name);
                petTypes.save(type);
            }
        }
    }
}