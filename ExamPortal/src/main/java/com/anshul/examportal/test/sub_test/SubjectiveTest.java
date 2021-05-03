package com.anshul.examportal.test.sub_test;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

import com.anshul.examportal.test.TestContainer;

@Entity(name = "Subjective_Test")
public class SubjectiveTest implements TestContainer {
	
	@Id
	@Column(name = "question_id")
	private String questionId;

	@Column(name = "test_id")
	private int testId;
	
	private String question;
	private int marks;
	
	public SubjectiveTest() {}

	public String getQuestionId() {
		return questionId;
	}

	public void setQuestionId(String questionId) {
		this.questionId = questionId;
	}

	public int getTestId() {
		return testId;
	}

	public void setTestId(int testId) {
		this.testId = testId;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

	public int getMarks() {
		return marks;
	}

	public void setMarks(int marks) {
		this.marks = marks;
	}
	
}
