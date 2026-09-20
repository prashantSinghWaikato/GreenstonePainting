package org.greenstone.backend.publiccontent;

public class GoogleReviewsUnavailableException extends RuntimeException {
    public GoogleReviewsUnavailableException(String message) {
        super(message);
    }

    public GoogleReviewsUnavailableException(String message, Throwable cause) {
        super(message, cause);
    }
}
