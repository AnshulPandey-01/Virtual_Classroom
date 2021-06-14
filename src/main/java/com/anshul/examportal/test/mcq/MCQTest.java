package com.anshul.examportal.test.mcq;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import com.anshul.examportal.test.TestContainer;

@Entity(name = "MCQ_Test")
public class MCQTest implements TestContainer {
	
	@Id
	@Column(name = "question_id")
	private String questionId;
	
	@Column(name = "test_id", nullable = false)
	private int testId;
	
	@Column(nullable = false)
	private String question;
	
	@Column(nullable = false)
	private String option1;
	
	@Column(nullable = false)
	private String option2;
	
	@Column(nullable = true)
	private String option3;
	
	@Column(nullable = true)
	private String option4;
	
	@Column(name = "correct_option", nullable = false)
	private String correctOption;
	
	public MCQTest() {}

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

	public String getOption1() {
		return option1;
	}

	public void setOption1(String option1) {
		this.option1 = option1;
	}

	public String getOption2() {
		return option2;
	}

	public void setOption2(String option2) {
		this.option2 = option2;
	}

	public String getOption3() {
		return option3;
	}

	public void setOption3(String option3) {
		this.option3 = option3;
	}

	public String getOption4() {
		return option4;
	}

	public void setOption4(String option4) {
		this.option4 = option4;
	}

	public String getCorrectOption() {
		return correctOption;
	}

	public void setCorrectOption(String correctOption) {
		this.correctOption = correctOption;
	}
	
}
