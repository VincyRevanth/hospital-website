package com.syamalahomeo.hospital.repository;

import com.syamalahomeo.hospital.entity.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {

    List<ContactMessage> findTop100ByOrderByCreatedAtDesc();
    long deleteByCreatedAtBefore(java.time.Instant cutoff);
}