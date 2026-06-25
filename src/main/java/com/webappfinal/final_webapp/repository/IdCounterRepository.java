package com.webappfinal.final_webapp.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import com.webappfinal.final_webapp.entity.IdCounter;

import jakarta.persistence.LockModeType;

public interface IdCounterRepository extends JpaRepository<IdCounter, String> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from IdCounter c where c.entityName = :entityName")
    Optional<IdCounter> findByEntityNameForUpdate(String entityName);
}
