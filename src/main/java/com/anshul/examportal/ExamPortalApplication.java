package com.anshul.examportal;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.anshul.examportal.admin.Admin;
import com.anshul.examportal.admin.Admin_Repo;

@SpringBootApplication
public class ExamPortalApplication {

	public static void main(String[] args) {
		SpringApplication.run(ExamPortalApplication.class, args);
	}
	
	@Bean
    public CommandLineRunner demoData(Admin_Repo aRepo) {
        return args -> { 
        	Admin admin = new Admin("admin@gmail.com", "Master Admin", "123456");
    		admin.setPassword(new BCryptPasswordEncoder().encode(admin.getPassword()));
            aRepo.save(admin);
        };
    }

}
