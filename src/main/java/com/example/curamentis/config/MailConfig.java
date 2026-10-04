package com.example.curamentis.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Value("${spring.mail.username:thecuramentis@gmail.com}")
    private String usernameProp;

    @Value("${spring.mail.password:}")
    private String passwordProp;

    @Value("${spring.mail.host:smtp.gmail.com}")
    private String host;

    @Value("${spring.mail.port:465}")
    private int port;

    @Bean
    public JavaMailSender javaMailSender() {
        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(host);
        mailSender.setPort(port);

        // Fallback directly to System.getenv if Spring property placeholder resolved to empty
        String username = (usernameProp != null && !usernameProp.isBlank()) 
                ? usernameProp 
                : System.getenv("SPRING_MAIL_USERNAME");

        if (username == null || username.isBlank()) {
            username = "thecuramentis@gmail.com";
        }

        String password = (passwordProp != null && !passwordProp.isBlank()) 
                ? passwordProp 
                : System.getenv("SPRING_MAIL_PASSWORD");

        if (username != null && !username.isBlank()) {
            mailSender.setUsername(username.trim());
        }
        if (password != null && !password.isBlank()) {
            // Automatically strip spaces from 16-character Gmail App Passwords (e.g. "azqx mbyz faxx cfbl" -> "azqxmbyzfaxxcfbl")
            mailSender.setPassword(password.replaceAll("\\s+", ""));
        }

        Properties props = mailSender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.socketFactory.port", "465");
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        props.put("mail.smtp.socketFactory.fallback", "false");
        props.put("mail.smtp.ssl.trust", "*");
        props.put("mail.smtp.ssl.protocols", "TLSv1.2 TLSv1.3");
        props.put("mail.smtp.connectiontimeout", "10000");
        props.put("mail.smtp.timeout", "10000");
        props.put("mail.smtp.writetimeout", "10000");
        props.put("mail.debug", "true");

        return mailSender;
    }
}



