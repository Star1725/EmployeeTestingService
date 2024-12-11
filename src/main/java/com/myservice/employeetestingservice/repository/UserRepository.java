package com.myservice.employeetestingservice.repository;

import com.myservice.employeetestingservice.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);
}
