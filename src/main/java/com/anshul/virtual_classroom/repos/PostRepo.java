package com.anshul.virtual_classroom.repos;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.anshul.virtual_classroom.entity.Post;
import com.anshul.virtual_classroom.utility.assignment.AssignmentFacultyView;
import com.anshul.virtual_classroom.utility.post.PostFacultyView;
import com.anshul.virtual_classroom.utility.post.PostStudentView;

@Repository
public interface PostRepo extends JpaRepository<Post, Integer> {
	
	Optional<Post> findByUniqueKeyAndIsAssignment(String uniqueKey, boolean isAssignment);
	
	@Query(value = "select unique_key as uniqueKey, title, assign_time as assignTime, due_time as dueTime, section, marks from post where created_by = :createdBy and is_assignment = true and TO_TIMESTAMP(due_time, 'YYYY-MM-DD HH24:MI') > NOW() order by assign_time asc", nativeQuery = true)
	List<AssignmentFacultyView> getAssignmentsCreatedBy(@Param("createdBy") String createdBy);
	
	@Query(value = "select unique_key as uniqueKey, title, assign_time as assignTime, due_time as dueTime, section, marks from post where created_by = :createdBy and is_assignment = true and TO_TIMESTAMP(due_time, 'YYYY-MM-DD HH24:MI') < NOW() order by due_time desc", nativeQuery = true)
	List<AssignmentFacultyView> getPastAssignmentsCreatedBy(@Param("createdBy") String createdBy);
	
	@Query(value = "select unique_key as uniqueKey, title, content, (select case when attachment is NULL then false else true end) as attachment, created_at as createdAt, subject_code as subjectCode, section from post where created_by = :createdBy and is_assignment = false order by created_at desc", nativeQuery = true)
	List<PostFacultyView> getPostsCreatedBy(@Param("createdBy") String createdBy);
	
	@Query(value = "select * from post where is_assignment = true and sem = :sem and branch = :branch and section = :section and TO_TIMESTAMP(assign_time, 'YYYY-MM-DD HH24:MI') < NOW() and TO_TIMESTAMP(due_time, 'YYYY-MM-DD HH24:MI') > NOW() order by assign_time desc", nativeQuery = true)
	List<Post> getOngoingAssignmentsBySBS(@Param("sem") int sem, @Param("branch") String branch, @Param("section") String section);
	
	@Query(value = "select * from post where is_assignment = true and sem = :sem and branch = :branch and section = :section and TO_TIMESTAMP(due_time, 'YYYY-MM-DD HH24:MI') < NOW() order by due_time desc", nativeQuery = true)
	List<Post> getDueAssignmentsBySBS(@Param("sem") int sem, @Param("branch") String branch, @Param("section") String section);
	
	@Query(value = "select unique_key as uniqueKey, title, content, created_by as createdBy, created_at as createdAt, subject_code as subjectCode, (select case when attachment is NULL then false else true end) as attachment from post where is_assignment = false and sem = :sem and branch = :branch and section = :section order by created_at desc", nativeQuery = true)
	List<PostStudentView> getPostsBySBS(@Param("sem") int sem, @Param("branch") String branch, @Param("section") String section);
	
}
