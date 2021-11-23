package com.anshul.virtual_classroom.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Response {
	
	public enum Status {
		success("success"),
		error("error");
		
		private final String st;
		
		Status(final String st) {
			this.st = st;
		}
	}
	
	private Status status;
	private Object response;
	
}
