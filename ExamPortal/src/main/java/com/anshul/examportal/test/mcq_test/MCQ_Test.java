package com.anshul.examportal.test.mcq_test;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class MCQ_Test {
	
	@Id
	private String question_id;
	private int test_id;
	private String question;
	private int marks;
	private String choice1;
	private String choice2;
	private String choice3;
	private String choice4;
	private String correct_choice;
	
	public MCQ_Test() {}

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

	public String getChoice1() {
		return choice1;
	}

	public void setChoice1(String choice1) {
		this.choice1 = choice1;
	}

	public String getChoice2() {
		return choice2;
	}

	public void setChoice2(String choice2) {
		this.choice2 = choice2;
	}

	public String getChoice3() {
		return choice3;
	}

	public void setChoice3(String choice3) {
		this.choice3 = choice3;
	}

	public String getChoice4() {
		return choice4;
	}

	public void setChoice4(String choice4) {
		this.choice4 = choice4;
	}

	public String getCorrect_choice() {
		return correct_choice;
	}

	public void setCorrect_choice(String correct_choice) {
		this.correct_choice = correct_choice;
	}
}
