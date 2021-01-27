package vn.bzcom.abis.faceattendance.common.model;

import com.fasterxml.jackson.annotation.JsonProperty;

public enum Pose {
    @JsonProperty("FACE_FRONT")
    FACE_FRONT,
    @JsonProperty("FACE_LEFT")
    FACE_LEFT,
    @JsonProperty("FACE_RIGHT")
    FACE_RIGHT,
    @JsonProperty("FINGER_RIGHT_THUMB")
    FINGER_RIGHT_THUMB,
    @JsonProperty("FINGER_RIGHT_INDEX")
    FINGER_RIGHT_INDEX,
    @JsonProperty("FINGER_RIGHT_MIDDLE")
    FINGER_RIGHT_MIDDLE,
    @JsonProperty("FINGER_RIGHT_RING")
    FINGER_RIGHT_RING,
    @JsonProperty("FINGER_RIGHT_LITTLE")
    FINGER_RIGHT_LITTLE,
    @JsonProperty("FINGER_LEFT_THUMB")
    FINGER_LEFT_THUMB,
    @JsonProperty("FINGER_LEFT_INDEX")
    FINGER_LEFT_INDEX,
    @JsonProperty("FINGER_LEFT_MIDDLE")
    FINEGR_LEFT_MIDDLE,
    @JsonProperty("FINGER_LEFT_RING")
    FINEGR_LEFT_RING,
    @JsonProperty("FINGER_LEFT_LITTLE")
    FINGER_LEFT_LITTLE;
}
