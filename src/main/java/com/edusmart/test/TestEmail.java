package com.edusmart.test;

import com.edusmart.model.Course;
import com.edusmart.model.Subscriber;
import com.edusmart.service.impl.EmailServiceImpl;

import java.util.ArrayList;
import java.util.List;

public class TestEmail {
    public static void main(String[] args) {
        EmailServiceImpl emailService = new EmailServiceImpl();
        Course course = new Course();
        course.setTitle("Test Course");
        course.setPrice(10.0);
        
        Subscriber sub = new Subscriber();
        sub.setEmail("firas.guizawi@gmail.com"); // send to self
        
        List<Subscriber> subs = new ArrayList<>();
        subs.add(sub);
        
        System.out.println("Starting to send email...");
        emailService.sendPriceDropNotification(course, 20.0, subs);
        
        // Wait for thread to finish
        try {
            Thread.sleep(5000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("Done.");
    }
}
