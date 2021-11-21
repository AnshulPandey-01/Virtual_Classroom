package com.anshul.virtual_classroom.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Response {
	
	public enum Respond{
		success("success"),
		error("error");
		
		private final String res;
		
		Respond(final String res) {
	        this.res = res;
	    }
	}
	
	private String message;
	private Object response;
	
}
