package com.edusmart.service;

import com.edusmart.model.Bulletin;
import com.edusmart.model.Certification;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

public class HmacService {
    private static final String SECRET = "09032004";
    private static final String ALGORITHM = "HmacSHA256";

    public String signBulletin(Bulletin b) {
        String data = String.format("%d|%s|%s|%.2f|%s",
                b.getStudentId(), b.getAcademicYear(), b.getSemester(), b.getAverage(), 
                (b.getMetier() != null ? b.getMetier() : ""));
        return calculateHmac(data);
    }

    public String signCertification(Certification c) {
        String data = String.format("%d|%s|%s|%s|%s",
                c.getStudentId(), c.getCertificationType(), c.getUniqueNumber(), c.getVerificationCode(),
                (c.getMetier() != null ? c.getMetier() : ""));
        return calculateHmac(data);
    }

    private String calculateHmac(String data) {
        try {
            SecretKeySpec secretKeySpec = new SecretKeySpec(SECRET.getBytes(StandardCharsets.UTF_8), ALGORITHM);
            Mac mac = Mac.getInstance(ALGORITHM);
            mac.init(secretKeySpec);
            byte[] hmacBytes = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hmacBytes);
        } catch (Exception e) {
            throw new RuntimeException("Failed to calculate HMAC", e);
        }
    }
}
