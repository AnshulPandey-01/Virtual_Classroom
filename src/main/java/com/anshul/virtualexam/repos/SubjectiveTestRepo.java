package com.anshul.virtualexam.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.anshul.virtualexam.entity.SubjectiveTest;
import com.anshul.virtualexam.utility.TestContainer;
import com.anshul.virtualexam.utility.TestDetails;

public interface SubjectiveTestRepo extends JpaRepository<SubjectiveTest, String> {
	
	List<TestContainer> findByTestId(int testId);

	@Query(value = "select count(question_id) as noOfQuestions, sum(marks) as totalMarks from Subjective_Test where test_id =:test_id", nativeQuery = true)
	TestDetails getNoOfQuestionsAndMaxMarks(@Param("test_id") int test_id);
	
}
