package com.syamalahomeo.hospital.service;

import com.syamalahomeo.hospital.dto.ServiceView;
import com.syamalahomeo.hospital.repository.ServiceOfferingRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ServiceOfferingService {

    private final ServiceOfferingRepository repository;

    public ServiceOfferingService(ServiceOfferingRepository repository) {
        this.repository = repository;
    }

    public List<ServiceView> findActive() {
        return repository.findByActiveTrueOrderByDisplayOrderAscNameAsc().stream()
                .map(s -> new ServiceView(s.getId(), s.getName(), s.getDescription()))
                .toList();
    }
}