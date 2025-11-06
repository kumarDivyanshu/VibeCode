package com.vibecode.interview.repositories;

import com.vibecode.interview.models.Company;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, String> {
}
