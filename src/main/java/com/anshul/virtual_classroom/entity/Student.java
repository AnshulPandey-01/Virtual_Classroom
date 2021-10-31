package com.anshul.virtual_classroom.entity;

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
public class Student {

	@Id
	@Column(name="roll_no")
	private String rollNo;
	
	@Column(unique = true, nullable = false)
	private String email;
	
	@Column(nullable = false)
	private String name;
	
	@Column(nullable = false)
	private String password;
	
	@Column(nullable = false)
	private String branch;
	
	@Column(nullable = false)
	private int sem;
	
	@Column(nullable = false)
	private String section;
	
}
