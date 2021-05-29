package com.anshul.examportal.test.answer;

public class TestResultInfo {
	
	private String name;
	private String rollNo;
	private int score;
	private boolean isPresent;
	
	public TestResultInfo(String name, String rollNo, int score, boolean isPresent) {
		this.name = name;
		this.rollNo = rollNo;
		this.score = score;
		this.isPresent = isPresent;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getRollNo() {
		return rollNo;
	}
	
	public void setRollNo(String rollNo) {
		this.rollNo = rollNo;
	}
	
	public int getScore() {
		return score;
	}
	
	public void setScore(int score) {
		this.score = score;
	}
	
	public boolean getIsPresent() {
		return isPresent;
	}
	
	public void setIsPresent(boolean isPresent) {
		this.isPresent = isPresent;
	}
	
}
