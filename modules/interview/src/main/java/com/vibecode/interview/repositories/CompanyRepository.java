package com.vibecode.interview.repositories;

import com.vibecode.interview.models.Company;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CompanyRepository extends JpaRepository<Company, String> {
    Optional<Company> findByNameIgnoreCase(String name);
}
