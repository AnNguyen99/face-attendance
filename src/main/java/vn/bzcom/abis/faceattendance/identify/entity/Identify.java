package vn.bzcom.abis.faceattendance.identify.entity;

import javafx.scene.canvas.Canvas;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.bzcom.abis.faceattendance.common.model.Auditable;
import vn.bzcom.abis.faceattendance.common.model.ImageFormat;
import vn.bzcom.abis.faceattendance.common.model.ImageInfo;
import vn.bzcom.abis.faceattendance.subject.entity.Subject;

import javax.persistence.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "identify")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Identify extends Auditable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "case_type", length = 20)
    private CaseType caseType;

    @Enumerated(EnumType.STRING)
    @Column(name = "prob_image_format", length = 5)
    private ImageFormat imageFormat;

    @Column(name = "prob_image_quality")
    private int imageQuality;

    @Lob
    @Column(name = "prob_image")
    private byte[] prob;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "subject_id")
    private Subject subject;

    @OneToMany(mappedBy = "identify", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Set<Candidate> candidates = new HashSet<>();

    @Builder
    public Identify(CaseType caseType, ImageFormat imageFormat, int imageQuality, byte[] prob) {
        this.caseType = caseType;
        this.imageFormat = imageFormat;
        this.imageQuality = imageQuality;
        this.prob = prob;
    }

    public void addSubject(Subject subject) {
        this.subject = subject;
    }

    public void addCandidate(Candidate candidate) {
        this.candidates.add(candidate);
    }

}
