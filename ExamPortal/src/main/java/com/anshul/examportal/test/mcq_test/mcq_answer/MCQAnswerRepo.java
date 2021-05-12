package com.anshul.examportal.test.mcq_test.mcq_answer;

import org.springframework.data.jpa.repository.JpaRepository;

import com.anshul.examportal.test.AnswerId;

public interface MCQAnswerRepo extends JpaRepository<MCQAnswer, AnswerId> {
	
	boolean existsByRollNoAndTestId(String rollNo, int testId);
	
}
