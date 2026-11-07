package com.syamalahomeo.hospital.service;

import com.syamalahomeo.hospital.entity.ContactMessage;
import com.syamalahomeo.hospital.repository.ContactMessageRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class RetentionServiceTest {

    @Autowired private RetentionService retention;
    @Autowired private ContactMessageRepository messages;

    @Test
    void purgeRemovesOldMessages() {
        messages.saveAndFlush(new ContactMessage("A", null, "a@example.com", "Hi"));
        long removed = retention.purgeContactMessagesCreatedBefore(Instant.now().plusSeconds(60));
        assertEquals(1, removed);
        assertEquals(0, messages.count());
    }
}