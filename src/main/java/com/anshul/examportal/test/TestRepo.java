package com.anshul.examportal.test;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface TestRepo extends JpaRepository<Test, Integer> {
	
	@Query("from Test where test_id= :testId")
	Test getByTestId(@Param("testId") int testId);
	
	@Query(value = "select * from test_table where DATE_ADD(schedule_on, INTERVAL duration MINUTE) > NOW() and created_by = :createdBy order by schedule_on asc", nativeQuery = true)
	List<Test> getScheduledTestsByFaculty(String createdBy);
	
	@Query(value = "select * from test_table where DATE_ADD(schedule_on, INTERVAL duration MINUTE) < NOW() and created_by = :createdBy order by ABS(DATEDIFF(result_on, NOW()))", nativeQuery = true)
	List<Test> getPastTestsByFaculty(String createdBy);
	
	@Query(value = "select * from test_table where DATE_ADD(schedule_on, INTERVAL duration MINUTE) > NOW() and sem= :sem and branch= :branch and section= :section order by schedule_on asc", nativeQuery = true)
	List<Test> getUpComingTestsBySBS(@Param("sem") int sem, @Param("branch") String branch, @Param("section") String section);
	
	@Query(value = "select * from test_table where schedule_on < NOW() and sem= :sem and branch= :branch and section= :section order by ABS(DATEDIFF(result_on, NOW()))", nativeQuery = true)
	List<Test> getPastTestsBySBS(@Param("sem") int sem, @Param("branch") String branch, @Param("section") String section);
}
