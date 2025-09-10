package com.anshul.virtual_classroom;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import com.anshul.virtual_classroom.entity.Admin;
import com.anshul.virtual_classroom.repos.AdminRepo;

@OpenAPIDefinition(
        info = @Info(
                title = "Virtual Classroom APIs",
                version = "1.0",
                description = "API documentation for project Virtual Classroom"
        )
)
@SpringBootApplication
public class VirtualClassroomApplication {

	public static void main(String[] args) {
		SpringApplication.run(VirtualClassroomApplication.class, args);
	}
	
	@Bean
    public CommandLineRunner demoData(AdminRepo aRepo) {
        return args -> { 
        	Admin admin = new Admin("admin@gmail.com", "Master Admin", "123456");
    		admin.setPassword(new BCryptPasswordEncoder().encode(admin.getPassword()));
            aRepo.save(admin);
        };
    }
}
