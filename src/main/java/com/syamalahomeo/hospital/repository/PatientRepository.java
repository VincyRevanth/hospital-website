package com.syamalahomeo.hospital.repository;

import com.syamalahomeo.hospital.entity.Patient;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PatientRepository extends JpaRepository<Patient, Long> {

    @Query("select p from Patient p where lower(p.fullName) like :nameLike "
            + "or p.phone like :phoneLike or upper(p.patientCode) = :code order by p.fullName")
    List<Patient> search(@Param("nameLike") String nameLike, @Param("phoneLike") String phoneLike,
                         @Param("code") String code, Pageable pageable);

    List<Patient> findTop20ByOrderByCreatedAtDesc();

    List<Patient> findByPhoneAndFullNameIgnoreCase(String phone, String fullName);
}