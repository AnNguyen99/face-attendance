package vn.bzcom.abis.faceattendance.event.entity;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.bzcom.abis.faceattendance.subject.entity.BioTemplate;
import vn.bzcom.abis.faceattendance.subject.entity.Subject;

import javax.persistence.*;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)

@Entity
@Table(name = "matching_result")
public class MatchingResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int score;

    @ManyToOne()
    @JoinColumn(name = "detected_face_id")
    private DetectedFace detectedFace;

    @ManyToOne()
    @JoinColumn(name = "bio_template_id")
    private BioTemplate bioTemplate;

    public MatchingResult(int score, DetectedFace detectedFace, BioTemplate bioTemplate) {
        this.score = score;
        this.detectedFace = detectedFace;
        this.bioTemplate = bioTemplate;
    }

    public void setDetectedFace(DetectedFace detectedFace) {
        if (this.detectedFace != null) {
            this.detectedFace = null;
        }
        this.detectedFace = detectedFace;
    }

    public void setBioTemplate(BioTemplate bioTemplate) {
        if (this.bioTemplate != null) {
            this.bioTemplate = null;
        }
        this.bioTemplate = bioTemplate;
    }

}
