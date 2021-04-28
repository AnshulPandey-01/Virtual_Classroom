package com.anshul.examportal.test;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface Test_Repo extends JpaRepository<Test, Integer> {
	
	@Query("from Test where created_by= :created_by")
	List<Test> getByCreated_by(String created_by);
}
