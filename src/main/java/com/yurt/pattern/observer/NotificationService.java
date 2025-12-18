package com.yurt.pattern.observer;

public class NotificationService implements Observer {
    @Override
    public void update(String message) {
        System.out.println("[BİLDİRİM SİSTEMİ]: " + message);
    }
}