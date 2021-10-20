package com.anshul.virtualexam.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
public class Faculty {

	@Id
	private String email;
	
	@Column(name ="name", unique = true, nullable = false)
	private String name;
	
	@Column(nullable = false)
	private String password;
	
	@Column(name="is_allowed", nullable = false)
	private boolean isAllowed = true;

	public Faculty(String email, String name, String password) {
		this.email = email;
		this.name = name;
		this.password = password;
	}
	
}

