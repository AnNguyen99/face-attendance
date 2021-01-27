package vn.bzcom.abis.faceattendance.common.dto;

import lombok.*;
import vn.bzcom.abis.faceattendance.common.model.ImageFormat;
import vn.bzcom.abis.faceattendance.common.model.Pose;

import javax.validation.constraints.NotNull;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Image {

    @NotNull
    private String base64Image;

    @NotNull
    private ImageFormat format;

    @NotNull
    private BioType bioType;
    private Pose pose;

    private int quality;

    private boolean enabled;


    @Builder
    private Image(String base64Image, ImageFormat format, BioType bioType, Pose pose, int quality, boolean enabled) {
        this.base64Image = base64Image;
        this.format = format;
        this.bioType = bioType;
        this.pose = pose;
        this.quality = quality;
        this.enabled = enabled;
    }
}
