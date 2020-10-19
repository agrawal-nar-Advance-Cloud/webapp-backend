package com.webapp.backend.repository;

import com.webapp.backend.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UserRepository extends JpaRepository<User, Long> {

    @Query(value= "FROM User u WHERE u.email = :email")
    User findByEmail(String email);
}
