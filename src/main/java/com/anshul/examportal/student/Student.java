package com.anshul.examportal.student;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Student {

	@Id
	@Column(name="roll_no")
	private String rollNo;
	private String email;
	private String name;
	private String password;
	private String branch;
	private int sem;
	private String section;
	
	public Student() {}

	@Override
	public String toString() {
		return "Student [rollNo=" + rollNo + ", email=" + email + ", name=" + name + ", password=" + password
				+ ", branch=" + branch + ", sem=" + sem + ", section=" + section + "]";
	}
	
	public String getRollNo() {
		return rollNo;
	}

	public void setRollNo(String rollNo) {
		this.rollNo = rollNo;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
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
	
}
