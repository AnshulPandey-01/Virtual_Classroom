package com.anshul.examportal.faculty;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface FacultyRepo extends JpaRepository<Faculty, String> {
	
	@Query("from Faculty where email= :email")
	Faculty getByEmail(@Param("email") String email);
}
