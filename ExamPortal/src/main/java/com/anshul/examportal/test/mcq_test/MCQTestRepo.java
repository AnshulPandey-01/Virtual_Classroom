package com.anshul.examportal.test.mcq_test;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.anshul.examportal.test.TestContainer;

public interface MCQTestRepo extends JpaRepository<MCQTest, String> {
	
	List<TestContainer> findByTestId(int testId);
	
	@Query("select count(question_id) from MCQ_Test where test_id =:test_id")
	int getNoOfQuestions(@Param("test_id") int test_id);
	
}
