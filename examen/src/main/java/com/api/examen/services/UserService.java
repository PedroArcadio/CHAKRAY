package com.api.examen.services;

import com.api.examen.entities.User;
import com.api.examen.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public void save (User user) {
        userRepository.save(user);
    }

    public Optional<User> findById(int userId) {
        return userRepository.findById(userId);
    }

    public boolean existsById(int userId) {
        return userRepository.existsById(userId);
    }

    public void deleteById(int userId) {
        userRepository.deleteById(userId);
    }

    public void deleteAll() {
        userRepository.deleteAll();
    }


}
