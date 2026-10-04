package com.example.curamentis.service;

import com.example.curamentis.model.Appointment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${admin.notification.email:sarag.mohan@gmail.com}")
    private String notificationEmail;

    @Value("${spring.mail.username:}")
    private String mailSenderAddress;

    @Async
    public void sendAppointmentNotification(Appointment appointment) {
        String fromAddress = (mailSenderAddress != null && !mailSenderAddress.isBlank()) 
                ? mailSenderAddress.trim() 
                : notificationEmail.trim();

        if (fromAddress == null || fromAddress.isBlank()) {
            System.err.println("⚠️ Email notification skipped: SPRING_MAIL_USERNAME environment variable is not configured.");
            return;
        }

        try {
            System.out.println("🔄 Preparing to send appointment notification email for patient: " + appointment.getName());
            
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
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
}
