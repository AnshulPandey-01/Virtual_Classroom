package com.anshul.virtualexam.utility;

public interface MCQTestData extends AnswerContainer {
	
	public String getQuestionId();
	public String getQuestion();
	public String getOption1();
	public String getOption2();
	public String getOption3();
	public String getOption4();
	public String getCorrectOption();
	public String getAnswer();
	
}