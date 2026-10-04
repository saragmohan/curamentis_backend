package com.example.curamentis.service;

import com.example.curamentis.model.Appointment;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
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
        sendEmailInternal(
            "🚨 New Appointment Booking: " + appointment.getName(),
            "Hello!\n\n" +
            "A new appointment has been successfully booked on Cura Mentis:\n\n" +
            "👤 Patient Name: " + appointment.getName() + "\n" +
            "📞 Mobile Number: " + appointment.getMobile() + "\n" +
            "📅 Date: " + appointment.getDate() + "\n" +
            "⏰ Time: " + appointment.getTime() + "\n\n" +
            "Please log in to your Admin Dashboard to manage appointments.\n"
        );
    }

    public Map<String, Object> sendTestEmail() {
        return sendEmailInternal("🧪 Cura Mentis Test Email", "This is a test notification email from Cura Mentis backend.");
    }

    private Map<String, Object> sendEmailInternal(String subject, String content) {
        Map<String, Object> result = new HashMap<>();

        String senderAddress = getSenderAddress();
        String notificationEmail = getNotificationEmail();

        result.put("senderAddress", senderAddress);
        result.put("notificationEmail", notificationEmail);

        // Check if an HTTP Email API Key is configured (Resend or Brevo)
        String resendApiKey = System.getenv("RESEND_API_KEY");
        String brevoApiKey = System.getenv("BREVO_API_KEY");

        if (resendApiKey != null && !resendApiKey.isBlank()) {
            return sendViaResend(resendApiKey, notificationEmail, subject, content, result);
        }

        if (brevoApiKey != null && !brevoApiKey.isBlank()) {
            return sendViaBrevo(brevoApiKey, senderAddress, notificationEmail, subject, content, result);
        }

        // Fallback to SMTP
        String password = null;
        if (mailSender instanceof JavaMailSenderImpl impl) {
            password = impl.getPassword();
        }

        result.put("passwordConfigured", password != null && !password.isBlank());

        if (password == null || password.isBlank()) {
            result.put("status", "ERROR");
            result.put("error", "SPRING_MAIL_PASSWORD is not set or empty in environment variables.");
            return result;
        }

        try {
            System.out.println("🔄 Sending notification email via SMTP to " + notificationEmail);
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(senderAddress.trim());
            message.setTo(notificationEmail.trim());
            message.setSubject(subject);
            message.setText(content);

            mailSender.send(message);
            System.out.println("📧 Notification email sent successfully via SMTP to " + notificationEmail);
            result.put("status", "SUCCESS");
            result.put("message", "Email sent successfully via SMTP to " + notificationEmail);
        } catch (Exception e) {
            System.err.println("❌ Failed to send email via SMTP: " + e.getMessage());
            result.put("status", "ERROR");
            result.put("error", "SMTP error: " + e.getMessage() + ". (Note: Render.com blocks outgoing SMTP ports 25, 587, 465. Use RESEND_API_KEY or BREVO_API_KEY for 100% reliable delivery over HTTP API).");
        }

        return result;
    }

    private Map<String, Object> sendViaResend(String apiKey, String toEmail, String subject, String content, Map<String, Object> result) {
        try {
            System.out.println("🔄 Sending notification email via Resend HTTP API to " + toEmail);
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

            // Resend test sender is onboarding@resend.dev unless a custom domain is verified
            String from = System.getenv("RESEND_FROM_EMAIL");
            if (from == null || from.isBlank()) {
                from = "Cura Mentis <onboarding@resend.dev>";
            }

            String jsonBody = String.format(
                "{\"from\":\"%s\",\"to\":[\"%s\"],\"subject\":\"%s\",\"text\":\"%s\"}",
                escapeJson(from), escapeJson(toEmail), escapeJson(subject), escapeJson(content)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.resend.com/emails"))
                    .header("Authorization", "Bearer " + apiKey.trim())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("📧 Email sent successfully via Resend HTTP API: " + response.body());
                result.put("status", "SUCCESS");
                result.put("provider", "Resend HTTP API");
                result.put("message", "Email sent successfully to " + toEmail);
            } else {
                System.err.println("❌ Resend HTTP API Error: " + response.statusCode() + " " + response.body());
                result.put("status", "ERROR");
                result.put("provider", "Resend HTTP API");
                result.put("error", "Resend API returned status " + response.statusCode() + ": " + response.body());
            }
        } catch (Exception e) {
            System.err.println("❌ Resend HTTP API Exception: " + e.getMessage());
            result.put("status", "ERROR");
            result.put("provider", "Resend HTTP API");
            result.put("error", e.getMessage());
        }
        return result;
    }

    private Map<String, Object> sendViaBrevo(String apiKey, String senderAddress, String toEmail, String subject, String content, Map<String, Object> result) {
        try {
            System.out.println("🔄 Sending notification email via Brevo HTTP API to " + toEmail);
            HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

            String jsonBody = String.format(
                "{\"sender\":{\"name\":\"Cura Mentis\",\"email\":\"%s\"},\"to\":[{\"email\":\"%s\"}],\"subject\":\"%s\",\"textContent\":\"%s\"}",
                escapeJson(senderAddress), escapeJson(toEmail), escapeJson(subject), escapeJson(content)
            );

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://api.brevo.com/v3/smtp/email"))
                    .header("api-key", apiKey.trim())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(jsonBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                System.out.println("📧 Email sent successfully via Brevo HTTP API: " + response.body());
                result.put("status", "SUCCESS");
                result.put("provider", "Brevo HTTP API");
                result.put("message", "Email sent successfully to " + toEmail);
            } else {
                System.err.println("❌ Brevo HTTP API Error: " + response.statusCode() + " " + response.body());
                result.put("status", "ERROR");
                result.put("provider", "Brevo HTTP API");
                result.put("error", "Brevo API returned status " + response.statusCode() + ": " + response.body());
            }
        } catch (Exception e) {
            System.err.println("❌ Brevo HTTP API Exception: " + e.getMessage());
            result.put("status", "ERROR");
            result.put("provider", "Brevo HTTP API");
            result.put("error", e.getMessage());
        }
        return result;
    }

    private String escapeJson(String input) {
        if (input == null) return "";
        return input.replace("\\", "\\\\")
                    .replace("\"", "\\\"")
                    .replace("\n", "\\n")
                    .replace("\r", "\\r")
                    .replace("\t", "\\t");
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



