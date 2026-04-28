package com.edusmart.dao;

import com.edusmart.model.Subscriber;
import java.util.List;

public interface SubscriberDao {
    boolean addSubscriber(Subscriber subscriber);
    boolean removeSubscriber(String email);
    List<Subscriber> getAllSubscribers();
    boolean exists(String email);
}
