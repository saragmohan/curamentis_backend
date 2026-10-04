package com.example.curamentis.service;

import com.example.curamentis.model.Appointment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${admin.notification.email:thecuramentis@gmail.com}")
    private String notificationEmailProp;

    @Value("${spring.mail.username:thecuramentis@gmail.com}")
    private String mailSenderAddressProp;

    @Async
    public void sendAppointmentNotification(Appointment appointment) {
        String senderAddress = getSenderAddress();
        String notificationEmail = getNotificationEmail();

        if (mailSender instanceof JavaMailSenderImpl impl) {
            if (impl.getPassword() == null || impl.getPassword().isBlank()) {
                System.err.println("⚠️ Email notification skipped: SPRING_MAIL_PASSWORD (16-character Gmail App Password) is not configured in environment variables.");
                return;
            }
        }

        try {
            System.out.println("🔄 Sending appointment notification email for patient: " + appointment.getName() + " to " + notificationEmail);
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderAddress.trim());
            message.setTo(notificationEmail.trim());
            message.setSubject("🚨 New Appointment Booking: " + appointment.getName());
            message.setText(
                "Hello!\n\n" +
                "A new appointment has been successfully booked on Cura Mentis:\n\n" +
                "👤 Patient Name: " + appointment.getName() + "\n" +
                "📞 Mobile Number: " + appointment.getMobile() + "\n" +
                "📅 Date: " + appointment.getDate() + "\n" +
                "⏰ Time: " + appointment.getTime() + "\n\n" +
                "Please log in to your Admin Dashboard to manage appointments.\n"
            );

            mailSender.send(message);
            System.out.println("📧 Appointment notification email sent successfully to " + notificationEmail);
        } catch (Exception e) {
            System.err.println("❌ Failed to send appointment notification email: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public Map<String, Object> sendTestEmail() {
        Map<String, Object> result = new HashMap<>();

        String senderAddress = getSenderAddress();
        String notificationEmail = getNotificationEmail();

        String password = null;
        if (mailSender instanceof JavaMailSenderImpl impl) {
            password = impl.getPassword();
        }

        result.put("senderAddress", senderAddress);
        result.put("notificationEmail", notificationEmail);
        result.put("passwordConfigured", password != null && !password.isBlank());

        if (password == null || password.isBlank()) {
            result.put("status", "ERROR");
            result.put("error", "SPRING_MAIL_PASSWORD is not set or is empty in environment variables.");
            return result;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderAddress.trim());
            message.setTo(notificationEmail.trim());
            message.setSubject("🧪 Cura Mentis Test Email");
            message.setText("This is a test notification email from Cura Mentis backend.");

            mailSender.send(message);
            result.put("status", "SUCCESS");
            result.put("message", "Test email sent successfully to " + notificationEmail);
        } catch (Exception e) {
            result.put("status", "ERROR");
            result.put("error", e.getMessage());
            e.printStackTrace();
        }

        return result;
    }

    private String getSenderAddress() {
        String senderAddress = (mailSenderAddressProp != null && !mailSenderAddressProp.isBlank())
                ? mailSenderAddressProp
                : System.getenv("SPRING_MAIL_USERNAME");

        if (senderAddress == null || senderAddress.isBlank()) {
            senderAddress = "thecuramentis@gmail.com";
        }
        return senderAddress;
    }

    private String getNotificationEmail() {
        String notificationEmail = (notificationEmailProp != null && !notificationEmailProp.isBlank())
                ? notificationEmailProp
                : System.getenv("ADMIN_NOTIFICATION_EMAIL");

        if (notificationEmail == null || notificationEmail.isBlank()) {
            notificationEmail = "thecuramentis@gmail.com";
        }
        return notificationEmail;
    }
}


