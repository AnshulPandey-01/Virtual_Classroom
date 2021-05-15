package com.anshul.examportal.student;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.lang.String;

public interface StudentRepo extends JpaRepository<Student, String> {
	
	@Query("from Student where email= :email")
	Student getByEmail(@Param("email") String email);
	
	@Query("from Student where roll_no= :rollNo")
	Student getByRollNo(@Param("rollNo") String rollNo);
	
	void deleteByEmail(String email);
	
}
