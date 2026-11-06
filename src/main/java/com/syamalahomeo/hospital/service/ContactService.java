package com.syamalahomeo.hospital.service;

import com.syamalahomeo.hospital.dto.ContactMessageView;
import com.syamalahomeo.hospital.dto.ContactRequest;
import com.syamalahomeo.hospital.entity.ContactMessage;
import com.syamalahomeo.hospital.repository.ContactMessageRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ContactService {

    private static final Logger log = LoggerFactory.getLogger(ContactService.class);

    private final ContactMessageRepository repository;
    private final DateTimeFormatter format;

    public ContactService(ContactMessageRepository repository, Clock clock) {
        this.repository = repository;
        this.format = DateTimeFormatter.ofPattern("dd MMM yyyy, HH:mm").withZone(clock.getZone());
    }

    @Transactional
    public void save(ContactRequest request) {
        ContactMessage saved = repository.save(new ContactMessage(
                request.getName().trim(),
                blankToNull(request.getPhone()),
                blankToNull(request.getEmail()),
                request.getMessage().trim()));
        log.info("Contact message {} received", saved.getId()); // no personal details in logs
    }

    @Transactional(readOnly = true)
    public List<ContactMessageView> latest() {
        return repository.findTop100ByOrderByCreatedAtDesc().stream()
                .map(m -> new ContactMessageView(m.getId(), m.getName(), m.getPhone(),
                        m.getEmail(), m.getMessage(), format.format(m.getCreatedAt())))
                .toList();
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}