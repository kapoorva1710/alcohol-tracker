package com.tracker.alcohol.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "drink_events")
public class DrinkEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "beverage_name", nullable = false)
    private String beverageName;

    @Column(nullable = false)
    private Double quantity; // in standard drinks

    @Column(name = "source_type")
    private String sourceType; // "MANUAL", "EMAIL_RECEIPT", "MESSAGE", "HEALTH_APP"

    @Column(name = "consumed_at", nullable = false)
    private LocalDateTime consumedAt;

    private String notes;

    public DrinkEvent() {
    }

    public DrinkEvent(User user, String beverageName, Double quantity, String sourceType, LocalDateTime consumedAt, String notes) {
        this.user = user;
        this.beverageName = beverageName;
        this.quantity = quantity;
        this.sourceType = sourceType;
        this.consumedAt = consumedAt;
        this.notes = notes;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getBeverageName() {
        return beverageName;
    }

    public void setBeverageName(String beverageName) {
        this.beverageName = beverageName;
    }

    public Double getQuantity() {
        return quantity;
    }

    public void setQuantity(Double quantity) {
        this.quantity = quantity;
    }

    public String getSourceType() {
        return sourceType;
    }

    public void setSourceType(String sourceType) {
        this.sourceType = sourceType;
    }

    public LocalDateTime getConsumedAt() {
        return consumedAt;
    }

    public void setConsumedAt(LocalDateTime consumedAt) {
        this.consumedAt = consumedAt;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
    }
}
