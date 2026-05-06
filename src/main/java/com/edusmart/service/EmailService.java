package com.edusmart.service;

import com.edusmart.model.Course;
import com.edusmart.model.Subscriber;
import java.util.List;

public interface EmailService {
    void sendEmail(String to, String subject, String content);
    void sendPriceDropNotification(Course course, double oldPrice, List<Subscriber> subscribers);
}
