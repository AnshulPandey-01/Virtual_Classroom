package com.anshul.examportal.test.answer;

import java.util.ArrayList;
import java.util.List;

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

	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getFacultyName() {
		return facultyName;
	}
	
	public void setFacultyName(String facultyName) {
		this.facultyName = facultyName;
	}
	
	public String getSubjectCode() {
		return subjectCode;
	}
	
	public void setSubjectCode(String subjectCode) {
		this.subjectCode = subjectCode;
	}
	
	public String getScheduledOn() {
		return scheduledOn;
	}
	
	public void setScheduledOn(String scheduledOn) {
		this.scheduledOn = scheduledOn;
	}
	
	public int getDuration() {
		return duration;
	}
	
	public void setDuration(int duration) {
		this.duration = duration;
	}
	
	public int getTotalQuestions() {
		return totalQuestions;
	}
	
	public void setTotalQuestions(int totalQuestions) {
		this.totalQuestions = totalQuestions;
	}
	
	public int getMaxMarks() {
		return maxMarks;
	}
	
	public void setMaxMarks(int maxMarks) {
		this.maxMarks = maxMarks;
	}
	
	public int getNegativeMarks() {
		return negativeMarks;
	}
	
	public void setNegativeMarks(int negativeMarks) {
		this.negativeMarks = negativeMarks;
	}
	
	public int getTotalMarks() {
		return totalMarks;
	}
	
	public void setTotalMarks(int totalMarks) {
		this.totalMarks = totalMarks;
	}
	
}
