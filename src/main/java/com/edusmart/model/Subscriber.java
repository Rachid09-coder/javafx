package com.edusmart.model;

import java.time.LocalDateTime;

public class Subscriber {
    private int id;
    private String email;
    private LocalDateTime subscribedAt;

    public Subscriber() {}

    public Subscriber(String email) {
        this.email = email;
        this.subscribedAt = LocalDateTime.now();
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDateTime getSubscribedAt() { return subscribedAt; }
    public void setSubscribedAt(LocalDateTime subscribedAt) { this.subscribedAt = subscribedAt; }
}
