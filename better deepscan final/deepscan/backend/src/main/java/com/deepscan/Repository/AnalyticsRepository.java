package com.deepscan.repository;

import com.deepscan.model.AnalyticsRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AnalyticsRepository extends JpaRepository<AnalyticsRecord, Long> {

    List<AnalyticsRecord> findByEmail(String email);

    @Query("SELECT COUNT(a) FROM AnalyticsRecord a WHERE a.passwordProvided = true")
    long countByPasswordProvided();

    @Query("SELECT a FROM AnalyticsRecord a ORDER BY a.createdAt DESC")
    List<AnalyticsRecord> findAllOrderByCreatedAtDesc();

    @Query("SELECT COUNT(a) FROM AnalyticsRecord a WHERE a.email LIKE %:domain")
    long countByEmailDomain(String domain);
}