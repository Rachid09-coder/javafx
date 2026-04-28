package com.edusmart.service.impl;

import com.edusmart.dao.SubscriberDao;
import com.edusmart.model.Subscriber;
import com.edusmart.service.SubscriptionService;

import java.util.List;
import java.util.regex.Pattern;

public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriberDao subscriberDao;
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
            "^[A-Za-z0-9+_.-]+@(.+)$");

    public SubscriptionServiceImpl(SubscriberDao subscriberDao) {
        this.subscriberDao = subscriberDao;
    }

    @Override
    public boolean addSubscriber(String email) {
        if (email == null || email.isBlank()) return false;
        email = email.trim().toLowerCase();
        if (!EMAIL_PATTERN.matcher(email).matches()) return false;
        if (subscriberDao.exists(email)) return false;

        Subscriber sub = new Subscriber(email);
        return subscriberDao.addSubscriber(sub);
    }

    @Override
    public boolean removeSubscriber(String email) {
        if (email == null || email.isBlank()) return false;
        return subscriberDao.removeSubscriber(email.trim().toLowerCase());
    }

    @Override
    public List<Subscriber> getAllSubscribers() {
        return subscriberDao.getAllSubscribers();
    }

    @Override
    public boolean isSubscribed(String email) {
        if (email == null || email.isBlank()) return false;
        return subscriberDao.exists(email.trim().toLowerCase());
    }
}
