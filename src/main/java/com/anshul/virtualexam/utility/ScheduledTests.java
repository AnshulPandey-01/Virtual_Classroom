package com.anshul.virtualexam.utility;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class ScheduledTests {
	
	private int testId;
	private String title;
	private String subjectCode;
	private boolean isSubjective;
	private int duration; // in minutes
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
	
}
