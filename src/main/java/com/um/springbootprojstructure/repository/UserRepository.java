package com.um.springbootprojstructure.repository;

import com.um.springbootprojstructure.entity.AppUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsername(String username);
    Optional<AppUser> findByPublicRef(String publicRef);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);
    boolean existsByPublicRef(String publicRef);
}