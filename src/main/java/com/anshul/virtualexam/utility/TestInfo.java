package com.anshul.virtualexam.utility;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class TestInfo {
	
	private String title;
	private String facultyName;
	private String subjectCode;
	private String scheduledOn;
	private int duration;
	private int totalQuestions;
	private int maxMarks;
	private int negativeMarks;
	private int totalMarks;
	
	public List<AnswerContainer> ansData;

	public TestInfo(String title, String facultyName, String subjectCode, String scheduledOn, int duration, int totalQuestions, int maxMarks, int negativeMarks) {
		this.title = title;
		this.facultyName = facultyName;
		this.subjectCode = subjectCode;
		this.scheduledOn = scheduledOn;
		this.duration = duration;
		this.totalQuestions = totalQuestions;
		this.maxMarks = maxMarks;
		this.negativeMarks = negativeMarks;
		
		ansData = new ArrayList<>();
	}
}
