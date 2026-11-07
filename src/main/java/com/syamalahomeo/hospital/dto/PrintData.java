package com.syamalahomeo.hospital.dto;

public record PrintData(PatientView patient, ConsultationView consultation, String printedOn) {
}