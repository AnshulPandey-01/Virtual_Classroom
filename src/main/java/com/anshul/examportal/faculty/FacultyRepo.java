package com.anshul.examportal.faculty;

import org.springframework.data.jpa.repository.JpaRepository;

public interface FacultyRepo extends JpaRepository<Faculty, String> {
	
	Faculty getOneByEmail(String email);
	
	boolean existsByName(String name);
	
}
