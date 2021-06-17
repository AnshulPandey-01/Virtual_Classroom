package com.anshul.examportal.test;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

@Entity
@Table(name="test_table")
public class Test {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	@Column(name = "test_id", columnDefinition = "serial")
	private int testId;
	
	@Column(nullable = false)
	private String title;
	
	@Column(nullable = false)
	private String password;
	
	@Column(name = "is_subjective", nullable = false, columnDefinition="INT(1)")
	private boolean isSubjective;
	
	@Column(nullable = false)
	private int duration; // duration in minutes
	
	@Column(name = "schedule_on", nullable = false)
	private String scheduleOn;
	
	@Column(name = "result_on", nullable = false)
	private String resultOn;
	
	@Column(name = "created_by", nullable = false)
	private String createdBy;
	
	@Column(name = "subject_code", nullable = false)
	private String subjectCode;
	
	@Column(nullable = false)
	private String branch;
	
	@Column(nullable = false)
	private int sem;
	
	@Column(nullable = false)
	private String section;
	
	@Column(nullable = true)
	private int marks;
	
	@Column(name = "negative_marks", nullable = true)
	private int negativeMarks;
	
	public Test() {}

	@Override
	public String toString() {
		return "Test [testId=" + testId + ", title=" + title + ", password=" + password + ", isSubjective="
				+ isSubjective + ", duration=" + duration + ", scheduleOn=" + scheduleOn + ", resultOn=" + resultOn
				+ ", createdBy=" + createdBy + ", subjectCode=" + subjectCode + ", branch=" + branch + ", sem=" + sem
				+ ", section=" + section + ", marks=" + marks + ", negativeMarks=" + negativeMarks + "]";
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

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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

	public String getCreatedBy() {
		return createdBy;
	}

	public void setCreatedBy(String createdBy) {
		this.createdBy = createdBy;
	}

	public String getSubjectCode() {
		return subjectCode;
	}

	public void setSubjectCode(String subjectCode) {
		this.subjectCode = subjectCode;
	}

	public String getBranch() {
		return branch;
	}

	public void setBranch(String branch) {
		this.branch = branch;
	}

	public int getSem() {
		return sem;
	}

	public void setSem(int sem) {
		this.sem = sem;
	}

	public String getSection() {
		return section;
	}

	public void setSection(String section) {
		this.section = section;
	}

	public int getMarks() {
		return marks;
	}

	public void setMarks(int marks) {
		this.marks = marks;
	}

	public int getNegativeMarks() {
		return negativeMarks;
	}

	public void setNegativeMarks(int negativeMarks) {
		this.negativeMarks = negativeMarks;
	}
	
}
