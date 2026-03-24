package com.resumebuilder.repository;

import com.resumebuilder.model.Resume;
import com.resumebuilder.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ResumeRepository extends JpaRepository<Resume, Long> {
    List<Resume> findByUser(User user);
}
