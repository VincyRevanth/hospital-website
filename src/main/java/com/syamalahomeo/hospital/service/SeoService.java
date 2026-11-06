package com.syamalahomeo.hospital.service;

import com.syamalahomeo.hospital.dto.DoctorView;
import com.syamalahomeo.hospital.dto.HospitalInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SeoService {

    private static final List<String> STATIC_PATHS = List.of(
            "/", "/about", "/treatments", "/doctors", "/faq", "/contact",
            "/appointments/new", "/privacy");

    private final String baseUrl;
    private final DoctorService doctorService;
    private final SettingsService settingsService;

    public SeoService(@Value("${site.base-url:http://localhost:8080}") String baseUrl,
                      DoctorService doctorService, SettingsService settingsService) {
        this.baseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        this.doctorService = doctorService;
        this.settingsService = settingsService;
    }

    public String robotsTxt() {
        return "User-agent: *\n"
                + "Disallow: /admin\n"
                + "Disallow: /api/admin\n"
                + "Disallow: /appointments/received\n"
                + "Sitemap: " + baseUrl + "/sitemap.xml\n";
    }

    public String sitemapXml() {
        StringBuilder xml = new StringBuilder(
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");
        STATIC_PATHS.forEach(p -> addUrl(xml, p));
        for (DoctorView d : doctorService.findAll()) {
            addUrl(xml, "/doctors/" + d.getSlug());
        }
        return xml.append("</urlset>\n").toString();
    }

    /** Only verified facts: name, website, logo and phone. No address or hours until verified. */
    public String hospitalJsonLd() {
        HospitalInfo info = settingsService.current();
        return "{\"@context\":\"https://schema.org\",\"@type\":\"Hospital\","
                + "\"name\":" + json(info.getName()) + ","
                + "\"url\":" + json(baseUrl + "/") + ","
                + "\"logo\":" + json(baseUrl + "/img/logo.jpeg") + ","
                + "\"telephone\":" + json(info.getPhoneLink()) + "}";
    }

    private void addUrl(StringBuilder xml, String path) {
        xml.append("  <url><loc>").append(escapeXml(baseUrl + path)).append("</loc></url>\n");
    }

    private static String escapeXml(String s) {
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;")
                .replace("\"", "&quot;").replace("'", "&apos;");
    }

    private static String json(String value) {
        StringBuilder sb = new StringBuilder("\"");
        for (char c : (value == null ? "" : value).toCharArray()) {
            switch (c) {
                case '"' -> sb.append("\\\"");
                case '\\' -> sb.append("\\\\");
                case '<' -> sb.append("\\u003c");
                case '>' -> sb.append("\\u003e");
                case '&' -> sb.append("\\u0026");
                default -> {
                    if (c < 0x20) {
                        sb.append(String.format("\\u%04x", (int) c));
                    } else {
                        sb.append(c);
                    }
                }
            }
        }
        return sb.append('"').toString();
    }
}