package com.anshul.examportal.test;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Test {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int test_id;
	private String title;
	private String password;
	private boolean is_subjective;
	private int duration; // duration in minutes
	private String schedule_on;
	private String result_on;
	private String created_by;
	private String subject_code;
	private String branch;
	private int sem;
	private String section;
	private int negative_marking;
	
	public int getTest_id() {
		return test_id;
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
	
	public boolean isIs_subjective() {
		return is_subjective;
	}
	
	public void setIs_subjective(boolean is_subjective) {
		this.is_subjective = is_subjective;
	}
	
	public int getDuration() {
		return duration;
	}
	
	public void setDuration(int duration) {
		this.duration = duration;
	}

	public String getSchedule_on() {
		return schedule_on;
	}
	
	public void setSchedule_on(String schedule_on) {
		this.schedule_on = schedule_on;
	}

	public String getResult_on() {
		return result_on;
	}

	public void setResult_on(String result_on) {
		this.result_on = result_on;
	}

	public String getCreated_by() {
		return created_by;
	}
	
	public void setCreated_by(String created_by) {
		this.created_by = created_by;
	}
	
	public String getSubject_code() {
		return subject_code;
	}
	
	public void setSubject_code(String subject_code) {
		this.subject_code = subject_code;
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
	
	public int getNegative_marking() {
		return negative_marking;
	}
	
	public void setNegative_marking(int negative_marking) {
		this.negative_marking = negative_marking;
	}
}
