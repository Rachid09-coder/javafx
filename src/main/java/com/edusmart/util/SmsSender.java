package com.edusmart.util;

import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

public class SmsSender {
    // A remplacer par vos identifiants Twilio
    public static final String ACCOUNT_SID = "ACxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx";
    public static final String AUTH_TOKEN = "your_auth_token_here";
    public static final String FROM_NUMBER = "+1234567890";

    static {
        Twilio.init(ACCOUNT_SID, AUTH_TOKEN);
    }

    public static boolean sendSms(String toNumber, String content) {
        try {
            System.out.println("[SMS Sending] To: " + toNumber);

            Message message = Message.creator(
                new PhoneNumber(toNumber),
                new PhoneNumber(FROM_NUMBER),
                content
            ).create();
            return message.getSid() != null;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public static void notifyBulletin(String phone, String studentName, String semester, String year) {
        String msg = String.format("EduSmart - Bonjour %s, votre bulletin %s %s est prêt. Connectez-vous pour le voir.",
                studentName, semester, year);
        sendSms(phone, msg);
    }
}
