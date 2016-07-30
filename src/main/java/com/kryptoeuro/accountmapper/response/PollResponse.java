package com.kryptoeuro.accountmapper.response;

import com.kryptoeuro.accountmapper.state.PollResponseStatus;
import lombok.Builder;
import lombok.Data;

@Data
public class PollResponse {
	String status;

	@Builder
	public PollResponse(PollResponseStatus status) {
		this.status = status.name();
	}
}
