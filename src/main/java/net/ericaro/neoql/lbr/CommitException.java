package net.ericaro.neoql.lbr;

public class CommitException extends Exception {

	public CommitException() {
	}

	public CommitException(String message) {
		super(message);
	}

	public CommitException(Throwable cause) {
		super(cause);
	}

	public CommitException(String message, Throwable cause) {
		super(message, cause);
	}

}
