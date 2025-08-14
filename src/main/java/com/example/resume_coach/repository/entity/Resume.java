package com.example.resume_coach.repository.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Entity
@Table(name = "resumes")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Resume {
    
    @Id
    @Column(name = "id", columnDefinition = "VARCHAR(36)")
    private String id;
    
    @Column(name = "career_summary", columnDefinition = "TEXT", nullable = false)
    private String careerSummary;
    
    @Column(name = "job_experience", columnDefinition = "TEXT", nullable = false)
    private String jobExperience;
    
    @Column(name = "skills", columnDefinition = "TEXT", nullable = false)
    private String skills;
    
    @Column(name = "desired_position")
    private String desiredPosition;
    
    @Column(name = "years_of_experience")
    private Integer yearsOfExperience;
    
    @Column(name = "industry")
    private String industry;
    
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    @PrePersist
    protected void onCreate() {
        if (id == null) {
            id = UUID.randomUUID().toString();
        }
        createdAt = LocalDateTime.now(ZoneOffset.UTC);
        updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now(ZoneOffset.UTC);
    }

}