package com.anshul.virtual_classroom.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.anshul.virtual_classroom.entity.AssignmentSubmission;
import com.anshul.virtual_classroom.utility.assignment.StudentSubmittedView;

@Repository
public interface AssignmentSubmissionRepo extends JpaRepository<AssignmentSubmission, Integer> {
	
	boolean existsByAssignmentIdAndRollNo(int assignmentId, String rollNo);
	
	@Query(value = "select p.unique_key as assignmentUniqueKey, p.title as title, p.assign_time as scheduleOn, p.due_time as DueOn, p.subject_code as SubjectCode, s.is_late as lateSubmission, s.score as score, p.marks as maxMarks from Assignment_Submission s inner join post p on s.assignment_id = p.id where s.roll_no = :rollNo order by p.due_time desc", nativeQuery = true)
	List<StudentSubmittedView> getSubmittedAssignments(@Param("rollNo") String rollNo);
	
}
