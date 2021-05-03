package com.anshul.examportal.test.sub_test;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.anshul.examportal.test.TestDetails;
import com.anshul.examportal.test.TestContainer;

public interface SubTestRepo extends JpaRepository<SubjectiveTest, String> {
	
	List<TestContainer> findByTestId(int testId);

	@Query(value = "select count(question_id) as noOfQuestions, sum(marks) as totalMarks from Subjective_Test where test_id =:test_id", nativeQuery = true)
	TestDetails getNoOfQuestions(@Param("test_id") int test_id);
	
}
