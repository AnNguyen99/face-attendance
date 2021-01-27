package vn.bzcom.abis.faceattendance.event.entity;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)

@Entity
@Table(name = "detected_face")
public class DetectedFace {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "scene_url")
    private String sceneUrl;

    @Column(name = "image_url")
    private String imageUrl;

    @Lob
    @Column(name = "template")
    private byte[] bioTemplate;

    @OneToMany(mappedBy = "detectedFace")
    private List<MatchingResult> matchingResults = new ArrayList<>();

    @Builder
    public DetectedFace(String sceneUrl, String imageUrl, byte[] bioTemplate, MatchingResult matchingResult) {
        this.sceneUrl = sceneUrl;
        this.imageUrl = imageUrl;
        this.bioTemplate = bioTemplate;
        addMatchingResult(matchingResult);
    }

    public void addMatchingResult(MatchingResult matchingResult) {
        if (this.matchingResults.contains(matchingResult)) {
            this.matchingResults.remove(matchingResult);
        }
        matchingResult.setDetectedFace(this);
        matchingResults.add(matchingResult);
    }


}
