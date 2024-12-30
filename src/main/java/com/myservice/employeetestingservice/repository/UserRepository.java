package com.myservice.employeetestingservice.repository;

import com.myservice.employeetestingservice.domain.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserRepository extends JpaRepository<User, Long> {
    User findByUsername(String username);

    @Query("SELECT u FROM User u LEFT JOIN FETCH u.userStorage WHERE u.id = :id")
    User findByIdWithUserStorage(@Param("id") Long id);
}
