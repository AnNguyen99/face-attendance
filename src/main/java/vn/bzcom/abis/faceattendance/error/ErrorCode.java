package vn.bzcom.abis.faceattendance.error;

import lombok.Getter;

@Getter
public enum ErrorCode {

    ACCOUNT_NOT_FOUND("AC_001", "Specified Account could not found.", 404),
    EMAIL_DUPLICATION("AC_002", "Email was duplicated.", 404),
    INPUT_VALUE_INVALID("???", "Input value is not correct.", 400),
    PASSWORD_FAILED_EXCEEDED("???", "exceeded count of password input failed", 400);

    private final String code;
    private final String message;
    private final int status;

    ErrorCode(String code, String message, int status) {
        this.code = code;
        this.message = message;
        this.status = status;
    }
}
