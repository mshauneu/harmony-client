package com.oath.client.harmony;

/**
 * Represent an Exception related to sending message process.
 *
 * @author Mike Shauneu
 */
public class SendMailException extends RuntimeException {

    private static final long serialVersionUID = -1552751255604355489L;

    public SendMailException() {
    }

    public SendMailException(String message) {
        super(message);
    }

    public SendMailException(Throwable cause) {
        super(cause);
    }

    public SendMailException(String message, Throwable cause) {
        super(message, cause);
    }


}
