package com.freestack.evaluation.models;


import javax.persistence.*;
import java.time.Instant;


@Entity
public class Booking {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "start_of_booking", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant startOfBooking;
    @Column(name = "end_of_booking", columnDefinition = "TIMESTAMP WITH TIME ZONE")
    private Instant endOfBooking;

    private Integer evaluation;

    @ManyToOne
    @JoinColumn(name = "driver_id", nullable = false)
    private Driver driver;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    public Booking() {
    }

    public Booking(User user) {
        this.user = user;
    }
    @Override
    public String toString() {
        return "Course, "+ user + ", " + driver + ", Heure " + startOfBooking +'}';
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Instant getStartOfBooking() {
        return startOfBooking;
    }

    public void setStartOfBooking(Instant startOfBooking) {
        this.startOfBooking = startOfBooking;
    }

    public Instant getEndOfBooking() {
        return endOfBooking;
    }

    public void setEndOfBooking(Instant endOfBooking) {
        this.endOfBooking = endOfBooking;
    }

    public Integer getEvaluation() {
        return evaluation;
    }

    public void setEvaluation(Integer evaluation) {
        this.evaluation = evaluation;
    }

    public Driver getDriver() {
        return driver;
    }

    public void setDriver(Driver driver) {
        this.driver = driver;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }
}
