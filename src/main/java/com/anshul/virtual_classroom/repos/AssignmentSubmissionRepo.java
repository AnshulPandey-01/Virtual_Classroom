package com.anshul.virtual_classroom.repos;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.anshul.virtual_classroom.entity.AssignmentSubmission;

@Repository
public interface AssignmentSubmissionRepo extends JpaRepository<AssignmentSubmission, Integer> {
	
	boolean existsByAssignmentIdAndRollNo(int assignmentId, String rollNo);
	
}
