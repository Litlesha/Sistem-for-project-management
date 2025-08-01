package org.diploma.fordiplom.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
public class MailConfig {

    @Bean
    public JavaMailSender javaMailSender(JavaMailSenderImpl sender) {
        return sender;
    }

    @Bean
    public JavaMailSenderImpl mailSender(@Value("${spring.mail.host}") String host,
                                         @Value("${spring.mail.port}") int port,
                                         @Value("${spring.mail.username}") String username,
                                         @Value("${spring.mail.password}") String password) {
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(port);
        sender.setUsername(username);
        sender.setPassword(password);

        Properties props = sender.getJavaMailProperties();
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.transport.protocol", "smtp");

        return sender;
    }
}