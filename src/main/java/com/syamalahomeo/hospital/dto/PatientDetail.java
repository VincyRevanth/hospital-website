package com.syamalahomeo.hospital.dto;

import java.util.List;

public record PatientDetail(PatientView patient, ConsultationView latest,
                            List<ConsultationView> history, int validityDays) {
}