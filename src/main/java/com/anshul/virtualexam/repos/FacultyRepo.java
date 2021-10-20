package com.anshul.virtualexam.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.anshul.virtualexam.entity.Faculty;

public interface FacultyRepo extends JpaRepository<Faculty, String> {
	
	Faculty getOneByEmail(String email);
	
	@Query(value = "select count(*) from faculty where name= :name or email= :email", nativeQuery = true)
	int checkFacultyExists(@Param("name") String name, @Param("email") String email);
	
}
