package vn.bzcom.abis.faceattendance.identify.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import vn.bzcom.abis.faceattendance.identify.entity.Candidate;
import vn.bzcom.abis.faceattendance.subject.dto.SubjectDto;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class CandidateDto {

    private long candidateId;

    private int score;

    private SubjectDto.SubjectRes subject;

    @Builder
    public CandidateDto(Candidate candidate) {
        this.candidateId = candidate.getId();
        this.score = candidate.getScore();
        this.subject = SubjectDto.SubjectRes.builder()
                .subject(candidate.getBioTemplate().getSubject())
                .build();
    }
}
