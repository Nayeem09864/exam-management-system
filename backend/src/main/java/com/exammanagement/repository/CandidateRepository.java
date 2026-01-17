package com.exammanagement.repository;

import com.exammanagement.model.Candidate;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CandidateRepository extends JpaRepository<Candidate, Long> {
    Optional<Candidate> findByEmail(String email);
    Optional<Candidate> findByCandidateId(String candidateId);
}
