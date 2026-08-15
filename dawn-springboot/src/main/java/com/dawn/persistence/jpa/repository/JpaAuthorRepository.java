package com.dawn.persistence.jpa.repository;

import com.dawn.persistence.jpa.domain.JpaAuthor;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface JpaAuthorRepository extends JpaRepository<JpaAuthor, Long> {

    Optional<JpaAuthor> findByNameIgnoreCase(String name);
}
