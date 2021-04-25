package com.anshul.examportal.faculty;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface Faculty_Repo extends JpaRepository<Faculty, String> {
	
	@Query("from Faculty where user_email= :user_email")
	Faculty find(@Param("user_email") String user_email);
}
