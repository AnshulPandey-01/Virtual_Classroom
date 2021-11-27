package com.anshul.virtual_classroom.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Lob;

import org.hibernate.annotations.Type;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.bytebuddy.utility.RandomString;

@Getter
@Setter
@ToString
@Entity(name = "Assignment_Submission")
public class AssignmentSubmission {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	
	@Column(name = "unique_key", unique = true, nullable = false)
	private String uniqueKey;
	
	@Column(name = "assignment_id", nullable = false)
	private int assignmentId;
	
	@Column(nullable = false)
	private String rollNo;
	
	@Lob
	@Type(type="org.hibernate.type.MaterializedBlobType")
	@Column(nullable = false, columnDefinition = "oid")
	private byte[] attachment;
	
	@Column(name = "submitted_on", nullable = false)
	private String submittedOn;
	
	@Column(name = "is_late", nullable = false)
	private boolean isLate;
	
	private int score;
	
	public AssignmentSubmission(int assignmentId, String rollNo, byte[] attachment, String submittedOn, boolean isLate) {
		this.uniqueKey = RandomString.make();
		this.assignmentId = assignmentId;
		this.rollNo = rollNo;
		this.attachment = attachment;
		this.submittedOn = submittedOn;
		this.isLate = isLate;
		this.score = -1;
	}
	
}
