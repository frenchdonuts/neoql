package net.ericaro.osql;

public class DQLException extends RuntimeException {

	public DQLException() {
		super();
	}

	protected DQLException(String message, Throwable cause,
			boolean enableSuppression, boolean writableStackTrace) {
		super(message, cause, enableSuppression, writableStackTrace);
	}

	public DQLException(String message, Throwable cause) {
		super(message, cause);
	}

	public DQLException(String message) {
		super(message);
	}

	public DQLException(Throwable cause) {
		super(cause);
	}

	
	
}
