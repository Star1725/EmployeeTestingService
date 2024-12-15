package com.myservice.employeetestingservice.repository;

import com.myservice.employeetestingservice.domain.UsersStorage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UsersStorageRepository extends JpaRepository<UsersStorage, Long> {
    UsersStorage findByUsersStorageName(String organizationName);
}
