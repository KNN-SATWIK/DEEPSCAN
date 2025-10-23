package com.deepscan.repository;

import com.deepscan.model.BreachRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BreachRepository extends JpaRepository<BreachRecord, Long> {

    // Custom query methods

    // Find breach by exact email
    Optional<BreachRecord> findByEmail(String email);

    // Find all breaches from a specific source
    List<BreachRecord> findByBreachSource(String breachSource);

    // Find breaches after a specific date
    List<BreachRecord> findByBreachDateAfter(String date);

    // Count breaches from a specific source
    long countByBreachSource(String breachSource);

    // Check if email exists in breaches
    boolean existsByEmail(String email);

    // Find breaches containing email text (partial match)
    List<BreachRecord> findByEmailContaining(String emailPart);

    // Custom SQL query - find breaches by email domain
    @Query("SELECT b FROM BreachRecord b WHERE b.email LIKE %:domain")
    List<BreachRecord> findBreachesByEmailDomain(@Param("domain") String domain);

    // Custom SQL query - get breach sources with counts
    @Query("SELECT b.breachSource, COUNT(b) FROM BreachRecord b GROUP BY b.breachSource")
    List<Object[]> getBreachSourceStats();
}