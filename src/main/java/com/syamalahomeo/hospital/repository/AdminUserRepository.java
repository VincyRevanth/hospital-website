package com.syamalahomeo.hospital.repository;

import com.syamalahomeo.hospital.entity.AdminUser;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AdminUserRepository extends JpaRepository<AdminUser, Long> {

    Optional<AdminUser> findByUsername(String username);

    List<AdminUser> findByRoleOrderByUsername(String role);
}