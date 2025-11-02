package com.vibecode.coding.repository;

import com.vibecode.coding.entity.DsaSheet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DsaSheetRepository extends JpaRepository<DsaSheet, Integer> {
}

