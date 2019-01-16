package com.quant.challenge.checker;

/**
 * This class is for handling all possible outcomes from the approval check process.
 */
public enum ApprovalResult {

    APPROVED(true, "Approved"),
    DISAPPROVED(false, "Insuficient Approvals");

    private boolean isApproved;
    private String message;

    ApprovalResult(boolean isApproved, String message) {
        this.isApproved = isApproved;
        this.message = message;
    }

    public boolean isApproved() {
        return isApproved;
    }

    public String getMessage() {
        return message;
    }
}
