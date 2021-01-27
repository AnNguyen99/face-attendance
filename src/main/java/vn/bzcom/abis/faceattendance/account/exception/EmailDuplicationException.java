package vn.bzcom.abis.faceattendance.account.exception;

import lombok.Getter;
import vn.bzcom.abis.faceattendance.common.model.Email;

@Getter
public class EmailDuplicationException extends RuntimeException {

    private Email email;
    private String field;

    public EmailDuplicationException(Email email) {
        this.field = "email";
        this.email = email;
    }
}
