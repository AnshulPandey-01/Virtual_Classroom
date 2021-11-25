package com.anshul.virtual_classroom.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;

import org.hibernate.annotations.Type;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import net.bytebuddy.utility.RandomString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
public class Post {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Column(name = "unique_key", unique = true, nullable = false)
	private String uniqueKey;
	
	@Column(name = "is_assignment", nullable = false)
	private boolean isAssignment;
	
	@Column(name = "created_by", nullable = false)
	private String createdBy;
	
	@Column(name = "created_at", nullable = false)
	private String createdAt;
	
	@Column(nullable = false)
	private String title;
	
	@Column(nullable = false)
	private String content;
	
	@Lob
	@Type(type="org.hibernate.type.MaterializedBlobType")
	@Column(columnDefinition = "oid")
	private byte[] attachment;
	
	@Column(name = "assign_time")
	private String assignTime;
	
	@Column(name = "due_time")
	private String dueTime;
	
	@Column(nullable = false)
	private int sem;
	
	@Column(nullable = false)
	private String branch;
	
	@Column(nullable = false)
	private String section;
	
	@Column(name = "subject_code", nullable = false)
	private String subjectCode;
	
	private int marks;
	
	public Post(boolean isAssignment, String createdBy, String createdAt, String title, String content,
			int sem, String branch, String section, String subjectCode) {
		this.uniqueKey = title.charAt(title.length()-1) + RandomString.make(6) + title.charAt(0);
		this.isAssignment = isAssignment;
		this.createdBy = createdBy;
		this.createdAt = createdAt;
		this.title = title;
		this.content = content;		
		this.sem = sem;
		this.branch = branch;
		this.section = section;
		this.subjectCode = subjectCode;
	}
	
	public Post(boolean isAssignment, String createdBy, String createdAt, String title, String content, byte[] attachment,
			String assignTime, String dueTime, int sem, String branch, String section, String subjectCode, int marks) {
		this(isAssignment, createdBy, createdAt, title, content, sem, branch, section, subjectCode);
		this.attachment = attachment;
		this.assignTime = assignTime;
		this.dueTime = dueTime;
		this.marks = marks;
	}
	
}
