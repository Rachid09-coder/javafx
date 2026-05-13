package com.edusmart.service;

import com.edusmart.model.Subscriber;
import java.util.List;

public interface SubscriptionService {
    boolean addSubscriber(String email);
    boolean removeSubscriber(String email);
    List<Subscriber> getAllSubscribers();
    boolean isSubscribed(String email);
}
