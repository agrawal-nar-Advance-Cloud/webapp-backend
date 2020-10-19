package com.webapp.backend.service;

import com.webapp.backend.model.User;
import com.webapp.backend.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private UserRepository repository;

        public User saveUser(User u){
            u.setPassword(BCrypt.hashpw(u.getPassword(), BCrypt.gensalt(10)));
            return repository.save(u);
        }

        public User findByEmail(String email){
            return repository.findByEmail(email);
        }

        public User userBYID(Long id){
            return repository.findById(id).orElse(null);
        }

        public User login(String email, String password){
            User user = repository.findByEmail(email);
            if(user != null) {
                if (BCrypt.checkpw(password, user.getPassword())) {
                        return user;
                  }
                }
            return null;
        }

        public User updateUser(User u, String email){

            User user = repository.findByEmail(email);

            user.setPassword(BCrypt.hashpw(u.getPassword(), BCrypt.gensalt(10)));
            user.setFirstName(u.getFirstName());
            user.setLastName(u.getLastName());

            return repository.save(user);
         }






 }
