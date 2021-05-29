package com.anshul.examportal.faculty;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Faculty {

	@Id
	private String email;
	private String name;
	private String password;
	@Column(name="is_allowed")
	private boolean isAllowed = true;
	
	public Faculty() {}

	public Faculty(String email, String name, String password) {
		this.email = email;
		this.name = name;
		this.password = password;
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

	public boolean getIsAllowed() {
		return isAllowed;
	}

	public void setIsAllowed(boolean isAllowed) {
		this.isAllowed = isAllowed;
	}
	
}
