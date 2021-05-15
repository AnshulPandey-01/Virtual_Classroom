package com.anshul.examportal.test.answer;

public interface SubjectiveData extends AnswerContainer{
	
	public String getQuestionId();
	
	public String getQuestion();
	
	public int getMarks();

	public String getAnswer();
	
	public int getScore();
	
}
