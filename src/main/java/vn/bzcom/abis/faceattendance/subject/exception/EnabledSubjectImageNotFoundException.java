package vn.bzcom.abis.faceattendance.subject.exception;

import lombok.Getter;

@Getter
public class EnabledSubjectImageNotFoundException extends RuntimeException {

    private long id;

    public EnabledSubjectImageNotFoundException(long id) {
        this.id = id;
    }
}
