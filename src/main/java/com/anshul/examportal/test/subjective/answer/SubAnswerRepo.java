package com.anshul.examportal.test.subjective.answer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.anshul.examportal.test.answer.AnswerId;
import com.anshul.examportal.test.answer.SubTestResult;
import com.anshul.examportal.test.answer.SubjectiveData;

public interface SubAnswerRepo extends JpaRepository<SubjectiveAnswer, AnswerId> {

	boolean existsByRollNoAndTestId(String rollNo, int testId);
	
	List<SubjectiveAnswer> findByRollNoAndTestId(String rollNo, int testId);
	
	@Query(value = "select s.roll_no as rollNo, s.name, t.score from Subjective_Answer t inner join Student s on s.roll_no = t.roll_no where test_id= :testId order by t.roll_no", nativeQuery = true)
	List<SubTestResult> getTestResult(@Param("testId") int testId);
	
	@Query(value = "select t.question_id as questionId, t.question, t.marks, a.answer, a.score from subjective_test t inner join subjective_answer a on t.question_id = a.question_id where a.test_id = :testId and a.roll_no = :rollNo", nativeQuery = true)
	List<SubjectiveData> getAnswers(@Param("testId") int testId, @Param("rollNo") String rollNo);
	
}
