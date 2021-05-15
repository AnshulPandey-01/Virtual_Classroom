package com.anshul.examportal.test;


public class ScheduledTests {
	
	private int testId;
	private String title;
	private String subjectCode;
	private boolean isSubjective;
	private int duration; // duration in minutes
	private String scheduleOn;
	private String resultOn;
	private int noOfQuestions;
	private int totalMarks;
	private int negativeMarks;
	
	public ScheduledTests() {}
	
	public ScheduledTests(int testId, String title, String subjectCode, boolean isSubjective, int duration, String scheduleOn, String resultOn, int negativeMarks) {
		this.testId = testId;
		this.title = title;
		this.subjectCode = subjectCode;
		this.isSubjective = isSubjective;
		this.duration = duration;
		this.scheduleOn = scheduleOn;
		this.resultOn = resultOn;
		this.negativeMarks = negativeMarks;
	}
	
	public ScheduledTests(int testId, String title, String subjectCode, boolean isSubjective, String resultOn) {
		this.testId = testId;
		this.title = title;
		this.subjectCode = subjectCode;
		this.isSubjective = isSubjective;
		this.resultOn = resultOn;
	}
	
	@Override
	public String toString() {
		return "ScheduledTests [title=" + title + ", subjectCode=" + subjectCode + ", isSubjective=" + isSubjective
				+ ", duration=" + duration + ", scheduleOn=" + scheduleOn + ", resultOn=" + resultOn
				+ ", noOfQuestions=" + noOfQuestions + ", totalMarks=" + totalMarks + ", negativeMarks=" + negativeMarks
				+ "]";
	}

	public int getTestId() {
		return testId;
	}

	public void setTestId(int testId) {
		this.testId = testId;
	}

	public String getTitle() {
		return title;
	}
	
	public void setTitle(String title) {
		this.title = title;
	}
	
	public String getSubjectCode() {
		return subjectCode;
	}
	
	public void setSubjectCode(String subjectCode) {
		this.subjectCode = subjectCode;
	}
	
	public boolean getIsSubjective() {
		return isSubjective;
	}
	
	public void setIsSubjective(boolean isSubjective) {
		this.isSubjective = isSubjective;
	}
	
	public int getDuration() {
		return duration;
	}
	
	public void setDuration(int duration) {
		this.duration = duration;
	}
	
	public String getScheduleOn() {
		return scheduleOn;
	}
	
	public void setScheduleOn(String scheduleOn) {
		this.scheduleOn = scheduleOn;
	}
	
	public String getResultOn() {
		return resultOn;
	}
	
	public void setResultOn(String resultOn) {
		this.resultOn = resultOn;
	}
	
	public int getNoOfQuestions() {
		return noOfQuestions;
	}
	
	public void setNoOfQuestions(int noOfQuestions) {
		this.noOfQuestions = noOfQuestions;
	}
	
	public int getTotalMarks() {
		return totalMarks;
	}
	
	public void setTotalMarks(int totalMarks) {
		this.totalMarks = totalMarks;
	}
	
	public int getNegativeMarks() {
		return negativeMarks;
	}
	
	public void setNegativeMarks(int negativeMarks) {
		this.negativeMarks = negativeMarks;
	}
	
}
