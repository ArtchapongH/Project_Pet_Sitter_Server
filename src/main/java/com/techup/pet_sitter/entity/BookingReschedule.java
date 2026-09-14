package com.techup.pet_sitter.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;

@Entity
@Table(name = "booking_reschedules")
public class BookingReschedule {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private Booking booking;

    @Column(name = "old_date", nullable = false)
    private LocalDate oldDate;

    @Column(name = "old_start_time", nullable = false)
    private LocalTime oldStartTime;

    @Column(name = "old_end_time", nullable = false)
    private LocalTime oldEndTime;

    @Column(name = "new_date", nullable = false)
    private LocalDate newDate;

    @Column(name = "new_start_time", nullable = false)
    private LocalTime newStartTime;

    @Column(name = "new_end_time", nullable = false)
    private LocalTime newEndTime;

    @Column(name = "changed_at", nullable = false)
    private OffsetDateTime changedAt;

    public BookingReschedule() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Booking getBooking() {
        return booking;
    }

    public void setBooking(Booking booking) {
        this.booking = booking;
    }

    public LocalDate getOldDate() {
        return oldDate;
    }

    public void setOldDate(LocalDate oldDate) {
        this.oldDate = oldDate;
    }

    public LocalTime getOldStartTime() {
        return oldStartTime;
    }

    public void setOldStartTime(LocalTime oldStartTime) {
        this.oldStartTime = oldStartTime;
    }

    public LocalTime getOldEndTime() {
        return oldEndTime;
    }

    public void setOldEndTime(LocalTime oldEndTime) {
        this.oldEndTime = oldEndTime;
    }

    public LocalDate getNewDate() {
        return newDate;
    }

    public void setNewDate(LocalDate newDate) {
        this.newDate = newDate;
    }

    public LocalTime getNewStartTime() {
        return newStartTime;
    }

    public void setNewStartTime(LocalTime newStartTime) {
        this.newStartTime = newStartTime;
    }

    public LocalTime getNewEndTime() {
        return newEndTime;
    }

    public void setNewEndTime(LocalTime newEndTime) {
        this.newEndTime = newEndTime;
    }

    public OffsetDateTime getChangedAt() {
        return changedAt;
    }

    public void setChangedAt(OffsetDateTime changedAt) {
        this.changedAt = changedAt;
    }
}
