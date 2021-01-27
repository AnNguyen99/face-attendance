package vn.bzcom.abis.faceattendance.identify.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.bzcom.abis.faceattendance.common.dto.BioGraphy;
import vn.bzcom.abis.faceattendance.common.dto.Image;
import vn.bzcom.abis.faceattendance.common.model.Gender;
import vn.bzcom.abis.faceattendance.common.model.ImageFormat;
import vn.bzcom.abis.faceattendance.common.model.Name;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CandidateListItem {

    private long subjectId;

    private int score;

    private BioGraphy bioGraphy;

    private Image image;

    @Builder
    public CandidateListItem(long subjectId, int score, BioGraphy bioGraphy, Image image) {
        this.subjectId = subjectId;
        this.score = score;
        this.bioGraphy = bioGraphy;
        this.image = image;
    }
}
