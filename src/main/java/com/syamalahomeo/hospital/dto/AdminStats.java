package com.syamalahomeo.hospital.dto;

public record AdminStats(long total, long pending, long confirmed, long rejected,
                         long cancelled, long upcomingConfirmed) {
}