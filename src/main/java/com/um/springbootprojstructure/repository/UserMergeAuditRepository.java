package com.um.springbootprojstructure.repository;

import com.um.springbootprojstructure.entity.UserMergeAudit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserMergeAuditRepository extends JpaRepository<UserMergeAudit, Long> {
}