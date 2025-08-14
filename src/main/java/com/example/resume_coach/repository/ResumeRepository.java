package com.example.resume_coach.repository;

import com.example.resume_coach.repository.entity.Resume;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;


@Repository
public interface ResumeRepository extends JpaRepository<Resume, String> {

}