package com.tracker.alcohol.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_settings")
public class UserSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Column(name = "weekly_goal", nullable = false)
    private Double weeklyGoal = 14.0;

    public UserSettings() {
    }

    public UserSettings(User user, Double weeklyGoal) {
        this.user = user;
        this.weeklyGoal = weeklyGoal;
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

    public Double getWeeklyGoal() {
        return weeklyGoal;
    }

    public void setWeeklyGoal(Double weeklyGoal) {
        this.weeklyGoal = weeklyGoal;
    }
}
