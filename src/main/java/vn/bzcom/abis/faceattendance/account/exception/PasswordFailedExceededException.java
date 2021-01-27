package vn.bzcom.abis.faceattendance.account.exception;

import vn.bzcom.abis.faceattendance.error.ErrorCode;

public class PasswordFailedExceededException extends RuntimeException {

    private ErrorCode errorCode;

    public PasswordFailedExceededException() {
        this.errorCode = ErrorCode.PASSWORD_FAILED_EXCEEDED;
    }
}
