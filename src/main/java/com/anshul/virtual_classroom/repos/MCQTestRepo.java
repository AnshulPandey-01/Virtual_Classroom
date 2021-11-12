package com.anshul.virtual_classroom.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.anshul.virtual_classroom.entity.MCQTest;
import com.anshul.virtual_classroom.utility.test.TestContainer;

@Repository
public interface MCQTestRepo extends JpaRepository<MCQTest, String> {
	
	List<TestContainer> findByTestId(int testId);
	
	@Query("select count(question_id) from MCQ_Test where test_id =:test_id")
	int getNoOfQuestions(@Param("test_id") int test_id);
	
}
