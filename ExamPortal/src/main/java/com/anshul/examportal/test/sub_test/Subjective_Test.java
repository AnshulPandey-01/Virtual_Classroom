package com.anshul.examportal.test.sub_test;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Subjective_Test {
	
	@Id
	private String question_id;
	private int test_id;
	private String question_description;
	private int marks;
	
	public Subjective_Test() {}

	public String getQuestion_id() {
		return question_id;
	}

	public void setQuestion_id(String question_id) {
		this.question_id = question_id;
	}

	public int getTest_id() {
		return test_id;
	}

	public void setTest_id(int test_id) {
		this.test_id = test_id;
	}

	public String getQuestion_description() {
		return question_description;
	}

	public void setQuestion_description(String question_description) {
		this.question_description = question_description;
	}

	public int getMarks() {
		return marks;
	}

	public void setMarks(int marks) {
		this.marks = marks;
	}
	
	
}
