package com.anshul.examportal.test.subjective.answer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.anshul.examportal.test.answer.AnswerId;
import com.anshul.examportal.test.answer.SubjectiveData;

public interface SubAnswerRepo extends JpaRepository<SubjectiveAnswer, AnswerId> {

	boolean existsByRollNoAndTestId(String rollNo, int testId);
	
	@Query(value = "select t.question_id as questionId, t.question, t.marks, a.answer, a.score from subjective_test t inner join subjective_answer a on t.question_id = a.question_id where a.test_id = :testId and a.roll_no = :rollNo", nativeQuery = true)
	List<SubjectiveData> getAnswers(@Param("testId") int testId, @Param("rollNo") String rollNo);
}
