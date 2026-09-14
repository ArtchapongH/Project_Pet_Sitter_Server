package com.techup.pet_sitter.entity;

import jakarta.persistence.Embeddable;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.MapsId;
import jakarta.persistence.Table;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "sitter_pet_types")
public class SitterPetType {

    @EmbeddedId
    private SitterPetTypeId id;

    @MapsId("sitterId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sitter_id", nullable = false)
    private SitterProfile sitter;

    @MapsId("petTypeId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_type_id", nullable = false)
    private PetType petType;

    public SitterPetType() {
    }

    public SitterPetTypeId getId() {
        return id;
    }

    public void setId(SitterPetTypeId id) {
        this.id = id;
    }

    public SitterProfile getSitter() {
        return sitter;
    }

    public void setSitter(SitterProfile sitter) {
        this.sitter = sitter;
    }

    public PetType getPetType() {
        return petType;
    }

    public void setPetType(PetType petType) {
        this.petType = petType;
    }

    @Embeddable
    public static class SitterPetTypeId implements Serializable {
        private UUID sitterId;
        private Integer petTypeId;

        public SitterPetTypeId() {
        }

        public SitterPetTypeId(UUID sitterId, Integer petTypeId) {
            this.sitterId = sitterId;
            this.petTypeId = petTypeId;
        }

        public UUID getSitterId() {
            return sitterId;
        }

        public void setSitterId(UUID sitterId) {
            this.sitterId = sitterId;
        }

        public Integer getPetTypeId() {
            return petTypeId;
        }

        public void setPetTypeId(Integer petTypeId) {
            this.petTypeId = petTypeId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SitterPetTypeId that = (SitterPetTypeId) o;
            return Objects.equals(sitterId, that.sitterId) && Objects.equals(petTypeId, that.petTypeId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(sitterId, petTypeId);
        }
    }
}
