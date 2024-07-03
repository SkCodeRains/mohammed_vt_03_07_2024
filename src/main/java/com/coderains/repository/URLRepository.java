package com.coderains.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.coderains.model.URL;

@Repository
public interface URLRepository extends JpaRepository<URL, Long> {
    Optional<URL> findByShortUrl(String shortUrl);
}
