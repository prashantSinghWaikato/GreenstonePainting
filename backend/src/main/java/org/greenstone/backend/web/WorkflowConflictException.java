package org.greenstone.backend.web;

public class WorkflowConflictException extends RuntimeException {
    public WorkflowConflictException() {
        super("This enquiry was updated by another staff member. Reload the latest version before saving again.");
    }

    public WorkflowConflictException(String message) {
        super(message);
    }
}
