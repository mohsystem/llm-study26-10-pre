package com.um.springbootprojstructure.repository;

import com.um.springbootprojstructure.entity.PasswordRules;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface PasswordRulesRepository extends JpaRepository<PasswordRules, Long> {

    @Query("select pr from PasswordRules pr order by pr.id desc limit 1")
    Optional<PasswordRules> findLatest();
}