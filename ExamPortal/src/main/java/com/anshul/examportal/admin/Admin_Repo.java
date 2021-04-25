package com.anshul.examportal.admin;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface Admin_Repo extends JpaRepository<Admin, String> {
	
	@Query("from Admin where user_email= :user_email")
	Admin find(@Param("user_email") String user_email);
}
