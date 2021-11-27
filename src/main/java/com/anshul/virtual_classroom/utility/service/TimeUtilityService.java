package com.anshul.virtual_classroom.utility.service;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.springframework.stereotype.Service;

@Service
public class TimeUtilityService {
	
	private static final long ONE_MINUTE_IN_MILLIS = 60000;
	
	private SimpleDateFormat formatter;
	
	public TimeUtilityService() {
		formatter = new SimpleDateFormat("yyyy-MM-dd HH:mm");
	}
	
	public String getCurrentTime() {
		return formatter.format(new Date());
	}

	public boolean testTimeCheck(String dateTime, int duration){
		try {			
			Date scheduleTime = formatter.parse(dateTime);
			Date endTime = new Date(scheduleTime.getTime() + duration * ONE_MINUTE_IN_MILLIS);
			Date currentTime = new Date(System.currentTimeMillis());
			
			return currentTime.before(endTime);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	public boolean checkTestTime(String dateTime, int duration){
		try {			
			Date currentDate = new Date(System.currentTimeMillis());
			Date scheduleDate = formatter.parse(dateTime);
			Date endDate = new Date(scheduleDate.getTime() + duration * ONE_MINUTE_IN_MILLIS);
			
			return scheduleDate.before(currentDate) && endDate.after(currentDate);
		} catch (Exception e) {
			System.out.println(e.getMessage());
			return false;
		}
	}
	
	public boolean checkTimeInBetween(String start, String end) {
		try {
			Date currentTime = new Date(System.currentTimeMillis());
			Date startTime = formatter.parse(start);
			Date endTime = formatter.parse(end);
			
			return currentTime.after(startTime) && currentTime.before(endTime);
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
