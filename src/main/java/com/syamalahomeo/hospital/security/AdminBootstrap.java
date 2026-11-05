package com.syamalahomeo.hospital.security;

import com.syamalahomeo.hospital.entity.AdminUser;
import com.syamalahomeo.hospital.repository.AdminUserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class AdminBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrap.class);

    private final AdminUserRepository repository;
    private final PasswordEncoder encoder;
    private final String username;
    private final String password;

    public AdminBootstrap(AdminUserRepository repository, PasswordEncoder encoder,
                          @Value("${admin.bootstrap.username:}") String username,
                          @Value("${admin.bootstrap.password:}") String password) {
        this.repository = repository;
        this.encoder = encoder;
        this.username = username;
        this.password = password;
    }

    @Override
    public void run(ApplicationArguments args) {
        if (repository.count() > 0) {
            return;
        }
        if (username.isBlank() || password.isBlank()) {
            log.warn("No admin user exists. Set admin.bootstrap.username and admin.bootstrap.password "
                    + "(or the ADMIN_BOOTSTRAP_* environment variables) for the first start.");
            return;
        }
        if (!username.matches("^[A-Za-z0-9._-]{3,50}$")) {
            log.error("Bootstrap admin username must be 3-50 characters: letters, digits, . _ -");
            return;
        }
        if (password.length() < 12) {
            log.error("Bootstrap admin password must be at least 12 characters.");
            return;
        }
        repository.save(new AdminUser(username, encoder.encode(password)));
        log.info("Initial admin account created. Remove the bootstrap password from configuration now.");
    }
}