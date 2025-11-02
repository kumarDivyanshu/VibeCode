package com.vibecode.interview.repositories;

import com.vibecode.interview.models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<User, String> {
}

