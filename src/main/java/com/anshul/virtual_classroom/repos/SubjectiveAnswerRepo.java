package com.anshul.virtual_classroom.repos;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.anshul.virtual_classroom.entity.SubjectiveAnswer;
import com.anshul.virtual_classroom.utility.AnswerId;
import com.anshul.virtual_classroom.utility.subjective.SubjectiveTestData;
import com.anshul.virtual_classroom.utility.subjective.SubjectiveTestResult;

@Repository
public interface SubjectiveAnswerRepo extends JpaRepository<SubjectiveAnswer, AnswerId>{

	boolean existsByRollNoAndTestId(String rollNo, int testId);
	
	List<SubjectiveAnswer> findByRollNoAndTestId(String rollNo, int testId);
	
	@Query(value = "select s.roll_no as rollNo, s.name, t.score from Subjective_Answer t inner join Student s on s.roll_no = t.roll_no where test_id= :testId order by t.roll_no", nativeQuery = true)
	List<SubjectiveTestResult> getTestResult(@Param("testId") int testId);
	
	@Query(value = "select t.question_id as questionId, t.question, t.marks, a.answer, a.score from subjective_test t inner join subjective_answer a on t.question_id = a.question_id where a.test_id = :testId and a.roll_no = :rollNo", nativeQuery = true)
	List<SubjectiveTestData> getAnswers(@Param("testId") int testId, @Param("rollNo") String rollNo);
	
	@Query(value = "select sum(score) from subjective_answer where test_id = :testId and roll_no = :rollNo", nativeQuery = true)
	Integer getTotalScore(@Param("testId") int testId, @Param("rollNo") String rollNo);
	
	@Query(value = "select sum(score) from subjective_answer where test_id = :testId", nativeQuery = true)
	Integer getSumOfAllStudentsScore(@Param("testId") int testId);
	
	@Query(value = "select count(distinct roll_no) from subjective_answer where test_id = :testId", nativeQuery = true)
	int getAllStudents(@Param("testId") int testId);
	
}
