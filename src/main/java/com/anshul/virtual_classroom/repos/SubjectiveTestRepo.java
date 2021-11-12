package com.anshul.virtual_classroom.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.anshul.virtual_classroom.entity.SubjectiveTest;
import com.anshul.virtual_classroom.utility.test.TestContainer;
import com.anshul.virtual_classroom.utility.test.TestDetails;

@Repository
public interface SubjectiveTestRepo extends JpaRepository<SubjectiveTest, String> {
	
	List<TestContainer> findByTestId(int testId);

	@Query(value = "select count(question_id) as noOfQuestions, sum(marks) as totalMarks from Subjective_Test where test_id =:testId", nativeQuery = true)
	TestDetails getNoOfQuestionsAndMaxMarks(@Param("testId") int testId);
	
}
