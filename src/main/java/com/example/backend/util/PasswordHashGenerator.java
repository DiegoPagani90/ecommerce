package com.example.backend.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHashGenerator {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        
        System.out.println("Generazione hash BCrypt per le password di test:");
        System.out.println("=================================================");
        
        String[] passwords = {"alice123", "bobdev456", "marco87pass", "giulia2024", "admin123"};
        String[] usernames = {"alice123", "bobthedev", "marco87", "giulia_design", "admin"};
        
        for (int i = 0; i < passwords.length; i++) {
            String hash = encoder.encode(passwords[i]);
            System.out.println(String.format("-- %s (password: %s)", usernames[i], passwords[i]));
            System.out.println(String.format("'%s',", hash));
            System.out.println();
        }
    }
}
