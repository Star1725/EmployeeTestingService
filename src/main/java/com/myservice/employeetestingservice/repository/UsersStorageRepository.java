package com.myservice.employeetestingservice.repository;

import com.myservice.employeetestingservice.domain.UserStorage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersStorageRepository extends JpaRepository<UserStorage, Long> {
    UserStorage findByUserStorageName(String usersStorageName);
}
