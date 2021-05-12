package com.anshul.examportal.test.sub_test.sub_answer;

import org.springframework.data.jpa.repository.JpaRepository;

import com.anshul.examportal.test.AnswerId;

public interface SubAnswerRepo extends JpaRepository<SubjectiveAnswer, AnswerId> {

	boolean existsByRollNoAndTestId(String rollNo, int testId);
	
}
