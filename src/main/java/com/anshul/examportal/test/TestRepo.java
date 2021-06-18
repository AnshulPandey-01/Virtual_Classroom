package com.anshul.examportal.test;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface TestRepo extends JpaRepository<Test, Integer> {
	
	@Query(value = "select * from test where test_id= :testId", nativeQuery = true)
	Test getByTestId(@Param("testId") int testId);
	
	@Query(value = "select * from test where TO_TIMESTAMP(schedule_on, 'YYYY-MM-DD HH24:MI:SS') + (duration * INTERVAL '1 MINUTE') > NOW() and created_by = :createdBy order by schedule_on asc", nativeQuery = true)
	List<Test> getScheduledTestsByFaculty(String createdBy);
	
	@Query(value = "select * from test where TO_TIMESTAMP(schedule_on, 'YYYY-MM-DD HH24:MI:SS') + (duration * INTERVAL '1 MINUTE') < NOW() and created_by = :createdBy order by ABS(extract(epoch from (TO_TIMESTAMP(result_on, 'YYYY-MM-DD HH24:MI:SS') - NOW())))", nativeQuery = true)
	List<Test> getPastTestsByFaculty(String createdBy);
	
	@Query(value = "select * from test where TO_TIMESTAMP(schedule_on, 'YYYY-MM-DD HH24:MI:SS') + (duration * INTERVAL '1 MINUTE') > NOW() and sem= :sem and branch= :branch and section= :section order by schedule_on asc", nativeQuery = true)
	List<Test> getUpComingTestsBySBS(@Param("sem") int sem, @Param("branch") String branch, @Param("section") String section);
	
	@Query(value = "select * from test where TO_TIMESTAMP(schedule_on, 'YYYY-MM-DD HH24:MI:SS') < NOW() and sem= :sem and branch= :branch and section= :section order by ABS(extract(epoch from (TO_TIMESTAMP(result_on, 'YYYY-MM-DD HH24:MI:SS') - NOW())))", nativeQuery = true)
	List<Test> getPastTestsBySBS(@Param("sem") int sem, @Param("branch") String branch, @Param("section") String section);
}
