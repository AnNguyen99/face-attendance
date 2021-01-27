package vn.bzcom.abis.faceattendance.subject.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.bzcom.abis.faceattendance.common.dto.Image;
import vn.bzcom.abis.faceattendance.common.model.Auditable;
import vn.bzcom.abis.faceattendance.common.model.ImageInfo;

import javax.persistence.*;
import java.io.IOException;

@Entity
@Table(name = "subject_image")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SubjectImage extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Embedded
    private ImageInfo imageInfo;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @Builder
    public SubjectImage(ImageInfo imageInfo, Subject subject) {
        this.imageInfo = imageInfo;
        this.subject = subject;
    }

    public void updateSubjectImage(Image image) throws IOException {
        this.imageInfo.updateImageInfo(image);
    }

}
