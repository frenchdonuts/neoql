package net.ericaro.neoql.lbr;

public class FetchException extends Exception {

	public FetchException() {
	}

	public FetchException(String message) {
		super(message);
	}

	public FetchException(Throwable cause) {
		super(cause);
	}

	public FetchException(String message, Throwable cause) {
		super(message, cause);
	}

}
