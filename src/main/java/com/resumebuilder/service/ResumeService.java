package com.resumebuilder.service;

import com.resumebuilder.model.Resume;
import com.resumebuilder.model.User;
import com.resumebuilder.repository.ResumeRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class ResumeService {

    @Autowired
    private ResumeRepository resumeRepository;

    public List<Resume> getResumesByUser(User user) {
        return resumeRepository.findByUser(user);
    }

    public Resume saveResume(Resume resume) {
        return resumeRepository.save(resume);
    }

    public Optional<Resume> getResumeById(Long id) {
        return resumeRepository.findById(id);
    }

    public void deleteResume(Long id) {
        resumeRepository.deleteById(id);
    }
}
