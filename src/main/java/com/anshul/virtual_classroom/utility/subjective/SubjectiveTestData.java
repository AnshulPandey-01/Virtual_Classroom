package com.anshul.virtual_classroom.utility.subjective;

import com.anshul.virtual_classroom.utility.AnswerContainer;

public interface SubjectiveTestData extends AnswerContainer {
	
	public String getQuestionId();
	public String getQuestion();
	public int getMarks();
	public String getAnswer();
	public int getScore();
	
}
