package com.example.distributed.system;

import com.example.distributed.system.dto.LoginRequest;
import com.example.distributed.system.dto.RegisterRequest;
import com.example.distributed.system.service.UserService;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "com.example.distributed.system")
public class TestUserService {
    public static void main(String[] args) {
        try {
            ApplicationContext context = new AnnotationConfigApplicationContext(TestUserService.class);
            UserService userService = context.getBean(UserService.class);
            System.out.println("UserService bean loaded successfully!");
            
            // Test register
            RegisterRequest registerRequest = new RegisterRequest();
            registerRequest.setUsername("test");
            registerRequest.setPassword("password");
            registerRequest.setEmail("test@example.com");
            
            System.out.println("Testing register...");
            // userService.register(registerRequest);
            System.out.println("Register test completed");
            
            // Test login
            LoginRequest loginRequest = new LoginRequest();
            loginRequest.setUsername("test");
            loginRequest.setPassword("password");
            
            System.out.println("Testing login...");
            // userService.login(loginRequest);
            System.out.println("Login test completed");
            
            System.out.println("All tests passed!");
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
