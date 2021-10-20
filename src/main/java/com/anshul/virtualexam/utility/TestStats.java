package com.anshul.virtualexam.utility;

import java.util.ArrayList;
import java.util.List;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TestStats {
	
	private List<Double> userPercent;
	private List<Double> averagePercent;
	private List<String> dates;
	private List<Integer> IDs;
	private List<String> titles;
	
	public TestStats(){
		userPercent = new ArrayList<>();
		averagePercent = new ArrayList<>();
		dates = new ArrayList<>();
		IDs = new ArrayList<>();
		titles = new ArrayList<>();
	}
	
}
