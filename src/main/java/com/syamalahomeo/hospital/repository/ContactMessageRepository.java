package com.syamalahomeo.hospital.repository;

import com.syamalahomeo.hospital.entity.ContactMessage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ContactMessageRepository extends JpaRepository<ContactMessage, Long> {
}