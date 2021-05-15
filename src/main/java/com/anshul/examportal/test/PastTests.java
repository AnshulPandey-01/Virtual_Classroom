package com.anshul.examportal.test;

public class PastTests {
	
	private int testId;
	private String title;
	private String subjectCode;
	private boolean isSubjective;
	private String resultOn;
	private boolean isPresent;
	
	public PastTests(int testId, String title, String subjectCode, boolean isSubjective, String resultOn, boolean isPresent) {
		this.testId = testId;
		this.title = title;
		this.subjectCode = subjectCode;
		this.isSubjective = isSubjective;
		this.resultOn = resultOn;
		this.isPresent = isPresent;
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
	
	public String getResultOn() {
		return resultOn;
	}
	
	public void setResultOn(String resultOn) {
		this.resultOn = resultOn;
	}
	
	public boolean getIsPresent() {
		return isPresent;
	}
	
	public void setIsPresent(boolean isPresent) {
		this.isPresent = isPresent;
	}
	
}
