package com.anshul.virtual_classroom.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
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
