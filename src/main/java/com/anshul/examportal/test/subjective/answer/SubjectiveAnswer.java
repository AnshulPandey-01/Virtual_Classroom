package com.anshul.examportal.test.subjective.answer;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.IdClass;

import com.anshul.examportal.test.answer.AnswerId;

@Entity(name = "Subjective_Answer")
@IdClass(AnswerId.class)
public class SubjectiveAnswer {

	@Id
	@Column(name = "roll_no")
	private String rollNo;
	
	@Column(name = "test_id")
	private int testId;
	
	@Id
	@Column(name = "question_id")
	private String questionId;
	
	private String answer;
	
	private int score = -1;
	
	public SubjectiveAnswer() {}

	public String getRollNo() {
		return rollNo;
	}

	public void setRollNo(String rollNo) {
		this.rollNo = rollNo;
	}

	public int getTestId() {
		return testId;
	}

	public void setTestId(int testId) {
		this.testId = testId;
	}

	public String getQuestionId() {
		return questionId;
	}

	public void setQuestionId(String questionId) {
		this.questionId = questionId;
	}

	public String getAnswer() {
		return answer;
	}

	public void setAnswer(String answer) {
		this.answer = answer;
	}

	public int getScore() {
		return score;
	}

	public void setScore(int score) {
		this.score = score;
	}
	
}