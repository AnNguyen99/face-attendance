package vn.bzcom.abis.faceattendance.identify.exception;

public class IdentifyNotFoundException extends RuntimeException {

    private long id;

    public IdentifyNotFoundException(long id) {
        this.id = id;
    }
}
