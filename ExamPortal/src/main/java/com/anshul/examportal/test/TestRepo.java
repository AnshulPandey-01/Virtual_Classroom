package com.anshul.examportal.test;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface TestRepo extends JpaRepository<Test, Integer> {
	
	@Query("from Test where test_id= :testId")
	Test getByTestId(@Param("testId") int testId);
	
	List<Test> findByCreatedBy(String created_by);
	
	@Query("from Test where sem= :sem and branch= :branch and section= :section")
	List<Test> getBySBS(@Param("sem") int sem, @Param("branch") String branch, @Param("section") String section);
}
