package com.anshul.examportal.test;

import java.io.Serializable;

public class AnswerId implements Serializable {

	private String rollNo;
	
	private String questionId;
	
	public AnswerId() {}

	public AnswerId(String rollNo, String questionId) {
		super();
		this.rollNo = rollNo;
		this.questionId = questionId;
	}

	public String getRollNo() {
		return rollNo;
	}

	public void setRollNo(String rollNo) {
		this.rollNo = rollNo;
	}

	public String getQuestionId() {
		return questionId;
	}

	public void setQuestionId(String questionId) {
		this.questionId = questionId;
	}
	
	
}
