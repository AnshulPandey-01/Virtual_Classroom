package com.anshul.virtual_classroom.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;
import org.springframework.http.ResponseEntity;

@Getter
@Setter
@AllArgsConstructor
public class Response <T> {
	
	public enum Status {
		success("success"),
		error("error");
		
		private final String status;

		Status(final String st) {
			this.status = st;
		}
	}
	
	private Status status;

	private T response;

    public static <R> ResponseEntity<Response<R>> SuccessResponse(R object) {
    	return ResponseEntity.ok(new Response<>(Status.success, object));
    }
}
