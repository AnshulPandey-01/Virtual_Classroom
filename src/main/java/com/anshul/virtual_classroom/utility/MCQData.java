package com.anshul.virtual_classroom.utility;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MCQData implements AnswerContainer{
	
	private String questionId;
	private String question;
	private String[] options;
	private String getCorrectOption;
	private String answer;
	
	public MCQData(MCQTestData mcqTestData) {
		this.questionId = mcqTestData.getQuestionId();
		this.question = mcqTestData.getQuestion();
		this.options = mcqTestData.getOptions().split("\\|,\\|");
		this.getCorrectOption = mcqTestData.getCorrectOption();
		this.answer = mcqTestData.getAnswer();
	}
	
}
