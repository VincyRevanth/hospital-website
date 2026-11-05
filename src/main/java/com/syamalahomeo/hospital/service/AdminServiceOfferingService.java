package com.syamalahomeo.hospital.service;

import com.syamalahomeo.hospital.dto.AdminServiceView;
import com.syamalahomeo.hospital.dto.ServiceForm;
import com.syamalahomeo.hospital.entity.ServiceOffering;
import com.syamalahomeo.hospital.exception.BusinessRuleException;
import com.syamalahomeo.hospital.repository.ServiceOfferingRepository;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class AdminServiceOfferingService {

    private static final String DUPLICATE = "A service with this name already exists.";

    private final ServiceOfferingRepository repository;

    public AdminServiceOfferingService(ServiceOfferingRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public List<AdminServiceView> list() {
        return repository.findAll(Sort.by("displayOrder", "name")).stream()
                .map(s -> new AdminServiceView(s.getId(), s.getName(), s.getDisplayOrder(), s.isActive()))
                .toList();
    }

    @Transactional(readOnly = true)
    public ServiceForm form(Long id) {
        ServiceOffering s = find(id);
        ServiceForm form = new ServiceForm();
        form.setName(s.getName());
        form.setDescription(s.getDescription());
        form.setDisplayOrder(s.getDisplayOrder());
        form.setActive(s.isActive());
        return form;
    }

    @Transactional
    public void create(ServiceForm form) {
        String name = form.getName().trim();
        if (repository.existsByNameIgnoreCase(name)) {
            throw new BusinessRuleException(DUPLICATE);
        }
        ServiceOffering s = new ServiceOffering(name, blankToNull(form.getDescription()));
        s.setDisplayOrder(form.getDisplayOrder());
        s.setActive(form.isActive());
        repository.save(s);
    }

    @Transactional
    public void update(Long id, ServiceForm form) {
        ServiceOffering s = find(id);
        String name = form.getName().trim();
        if (repository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new BusinessRuleException(DUPLICATE);
        }
        s.setName(name);
        s.setDescription(blankToNull(form.getDescription()));
        s.setDisplayOrder(form.getDisplayOrder());
        s.setActive(form.isActive());
        repository.save(s);
    }

    private ServiceOffering find(Long id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    private static String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}