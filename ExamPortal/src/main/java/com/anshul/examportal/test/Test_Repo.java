package com.anshul.examportal.test;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;


public interface Test_Repo extends JpaRepository<Test, Integer> {
	
	List<Test> findByCreatedBy(String created_by);
	
}
