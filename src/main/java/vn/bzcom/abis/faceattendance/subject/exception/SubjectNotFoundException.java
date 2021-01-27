package vn.bzcom.abis.faceattendance.subject.exception;

import lombok.Getter;

@Getter
public class SubjectNotFoundException extends RuntimeException {

    private long id;

    public SubjectNotFoundException(long id) {
        this.id = id;
    }
}
