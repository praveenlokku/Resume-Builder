package com.resumebuilder.service;

import com.resumebuilder.model.User;
import com.resumebuilder.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void registerUser(String username, String password) {
        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(password));
        user.setRoles("ROLE_USER");
        userRepository.save(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public void updateProfile(String username, String fullName, String location) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setFullName(fullName);
            user.setLocation(location);
            userRepository.save(user);
        });
    }

    public boolean updatePassword(String username, String oldPassword, String newPassword) {
        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (passwordEncoder.matches(oldPassword, user.getPassword())) {
                user.setPassword(passwordEncoder.encode(newPassword));
                userRepository.save(user);
                return true;
            }
        }
        return false;
    }

    public void updateProfessionalLinks(String username, String phone, String linkedin, String github, String portfolio) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setPhone(phone);
            user.setLinkedin(linkedin);
            user.setGithub(github);
            user.setPortfolio(portfolio);
            userRepository.save(user);
        });
    }

    public void updatePreferences(String username, int templateId) {
        userRepository.findByUsername(username).ifPresent(user -> {
            user.setDefaultTemplateId(templateId);
            userRepository.save(user);
        });
    }

    public void deleteUser(String username) {
        userRepository.findByUsername(username).ifPresent(user -> {
            userRepository.delete(user);
        });
    }
}
