package app;

class UnmatchedTransaction {
	private String referenceId;
	
	private String reason;
	
	public UnmatchedTransaction(String referenceId, String reason) {
		this.referenceId = referenceId;
		this.reason = reason;
	}

	public String getReferenceId() {
		return referenceId;
	}

	public void setReferenceId(String referenceId) {
		this.referenceId = referenceId;
	}

	public String getReason() {
		return reason;
	}

	public void setReason(String reason) {
		this.reason = reason;
	}
	
	
}