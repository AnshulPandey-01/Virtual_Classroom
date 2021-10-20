package com.anshul.virtualexam.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.anshul.virtualexam.entity.MCQAnswer;
import com.anshul.virtualexam.utility.AnswerId;
import com.anshul.virtualexam.utility.MCQTestData;
import com.anshul.virtualexam.utility.MCQTestResult;

public interface MCQAnswerRepo extends JpaRepository<MCQAnswer, AnswerId> {
	
	boolean existsByRollNoAndTestId(String rollNo, int testId);
	
	@Query(value = "select s.roll_no as rollNo, s.name, ans.question_id as questionId, ans.answer, t.correct_option as correctOption from student s inner join mcq_answer ans on s.roll_no = ans.roll_no inner join mcq_test t on ans.question_id = t.question_id where ans.test_id= :testId order by ans.roll_no", nativeQuery = true)
	List<MCQTestResult> getTestResult(@Param("testId") int testId);

	@Query(value = "select t.question_id as questionId, t.question, t.option1, t.option2, t.option3, t.option4, t.correct_option as correctOption, a.answer from mcq_test t inner join mcq_answer a on t.question_id = a.question_id where a.test_id = :testId and a.roll_no = :rollNo", nativeQuery = true)
	List<MCQTestData> getAnswers(@Param("testId") int testId, @Param("rollNo") String rollNo);
	
}
