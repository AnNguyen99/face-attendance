package vn.bzcom.abis.faceattendance.common.model;

import com.kbjung.abis.neurotec.biometrics.utils.NImageUtils;
import com.neurotec.images.NImage;
import com.neurotec.images.NImageFormat;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.bzcom.abis.faceattendance.common.dto.BioType;
import vn.bzcom.abis.faceattendance.common.dto.Image;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Embeddable
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ImageInfo {

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "image_quality")
    private int imageQuality;

    @Enumerated(EnumType.STRING)
    @Column(name = "format", length = 10)
    private ImageFormat format;

    @Enumerated(EnumType.STRING)
    @Column(name = "bio_type", length = 10)
    private BioType bioType;

    @Enumerated(EnumType.STRING)
    @Column(name = "pose", length = 50)
    private Pose pose;

    @Column(name = "enabled", columnDefinition = "boolean default true")
    private boolean enabled;

    @Builder
    public ImageInfo(String imageUrl, int imageQuality, ImageFormat imageFormat, BioType bioType, Pose pose, boolean enabled) {
        this.imageUrl = imageUrl;
        this.imageQuality = imageQuality;
        this.format = imageFormat;
        this.bioType = bioType;
        this.pose = pose;
        this.enabled = enabled;
    }

    public void updateImageInfo(Image image) throws IOException {
        updateImage(image.getBase64Image(), image.getFormat());
        this.imageQuality = image.getQuality();
        this.format = image.getFormat();
        this.bioType = image.getBioType();
        this.enabled = image.isEnabled();
    }

    public String getImageByBase64(final String imageRootPath) {
        String image = NImageUtils.imageFileToBase64String(imageRootPath + this.imageUrl);
        return image;
    }

    public String getImageByBase64() {
        String image = NImageUtils.imageFileToBase64String("./uploads" + this.imageUrl);
        return image;
    }

    public void saveImage(final String base64Image, ImageFormat format, String imageUrl) throws IOException {
        this.imageUrl = imageUrl;

        NImage nImage = NImageUtils.base64StringToNImage(base64Image, format.name().toUpperCase());
        switch (format) {
            case JPEG:
            case JPG:
                nImage.save("./uploads" + this.imageUrl, NImageFormat.getJPEG());
                break;
            case PNG:
                nImage.save("./uploads" + this.imageUrl, NImageFormat.getPNG());
                break;
        }
    }

    public void updateImage(final String base64Image, ImageFormat format) throws IOException {
        NImage nImage = NImageUtils.base64StringToNImage(base64Image, format.name().toUpperCase());
        switch (format) {
            case JPEG:
            case JPG:
                nImage.save("./uploads" + this.imageUrl, NImageFormat.getJPEG());
                break;
            case PNG:
                nImage.save("./uploads" + this.imageUrl, NImageFormat.getPNG());
                break;
        }
    }

    public void setDisable() {
        this.enabled = false;
    }

    private String generateFaceImagePath() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd/HH/mm/");
        String imagePath = LocalDateTime.now().format(formatter);
        return imagePath;
    }
}
