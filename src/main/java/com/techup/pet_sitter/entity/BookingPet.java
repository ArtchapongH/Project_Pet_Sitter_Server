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

@Entity
@Table(name = "booking_pets")
public class BookingPet {

    @EmbeddedId
    private BookingPetId id;

    @MapsId("bookingId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @MapsId("petId")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "pet_id", nullable = false)
    private Pet pet;

    public BookingPet() {
    }

    public BookingPetId getId() {
        return id;
    }

    public void setId(BookingPetId id) {
        this.id = id;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public Pet getPet() {
        return pet;
    }

    public void setPet(Pet pet) {
        this.pet = pet;
    }

    @Embeddable
    public static class BookingPetId implements Serializable {
        private Long bookingId;
        private Long petId;

        public BookingPetId() {
        }

        public BookingPetId(Long bookingId, Long petId) {
            this.bookingId = bookingId;
            this.petId = petId;
        }

        public Long getBookingId() {
            return bookingId;
        }

        public void setBookingId(Long bookingId) {
            this.bookingId = bookingId;
        }

        public Long getPetId() {
            return petId;
        }

        public void setPetId(Long petId) {
            this.petId = petId;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            BookingPetId that = (BookingPetId) o;
            return Objects.equals(bookingId, that.bookingId) && Objects.equals(petId, that.petId);
        }

        @Override
        public int hashCode() {
            return Objects.hash(bookingId, petId);
        }
    }
}
