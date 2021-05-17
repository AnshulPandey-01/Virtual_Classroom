package com.anshul.examportal.test.mcq.answer;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.anshul.examportal.test.answer.AnswerId;
import com.anshul.examportal.test.answer.MCQData;

public interface MCQAnswerRepo extends JpaRepository<MCQAnswer, AnswerId> {
	
	boolean existsByRollNoAndTestId(String rollNo, int testId);
	
	@Query(value = "select t.question_id as questionId, t.question, t.option1, t.option2, t.option3, t.option4, t.correct_option as correctOption, a.answer from mcq_test t inner join mcq_answer a on t.question_id = a.question_id where a.test_id = :testId and a.roll_no = :rollNo", nativeQuery = true)
	List<MCQData> getAnswers(@Param("testId") int testId, @Param("rollNo") String rollNo);
	
}
