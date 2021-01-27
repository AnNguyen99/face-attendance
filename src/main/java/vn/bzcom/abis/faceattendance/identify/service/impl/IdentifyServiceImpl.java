package vn.bzcom.abis.faceattendance.identify.service.impl;

import com.neurotec.biometrics.NMatchingDetails;
import com.neurotec.biometrics.NMatchingResult;
import com.sun.javafx.geom.transform.Identity;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import vn.bzcom.abis.faceattendance.common.model.ImageFormat;
import vn.bzcom.abis.faceattendance.identify.entity.Candidate;
import vn.bzcom.abis.faceattendance.identify.entity.CaseType;
import vn.bzcom.abis.faceattendance.identify.entity.Identify;
import vn.bzcom.abis.faceattendance.identify.exception.IdentifyNotFoundException;
import vn.bzcom.abis.faceattendance.identify.repository.IdentifyRepository;
import vn.bzcom.abis.faceattendance.identify.service.IdentifyService;
import vn.bzcom.abis.faceattendance.subject.entity.Subject;
import vn.bzcom.abis.faceattendance.subject.service.SubjectService;

import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

@Service
@RequiredArgsConstructor
public class IdentifyServiceImpl implements IdentifyService {

    private final IdentifyRepository identifyRepository;

    private final SubjectService subjectService;

    @Override
    public Identify createVerification(ImageFormat imageFormat, int imageQuality, byte[] prob, Subject subject) {
        Identify identify = Identify.builder()
                .caseType(CaseType.VERIFICATION)
                .imageFormat(imageFormat)
                .imageQuality(imageQuality)
                .prob(prob)
                .build();
        identify.addSubject(subject);
        identify = identifyRepository.save(identify);
        return identify;
    }

    @Override
    public Identify createVerification(ImageFormat imageFormat, int imageQuality, byte[] prob, Subject subject, int score) {
        Identify identify = Identify.builder()
                .caseType(CaseType.VERIFICATION)
                .imageFormat(imageFormat)
                .imageQuality(imageQuality)
                .prob(prob)
                .build();
        identify.addSubject(subject);

        Candidate candidate = Candidate.builder()
                .score(score)
                .identify(identify)
                .bioTemplate(subject.getBioTemplate())
                .build();
        identify.addCandidate(candidate);
        identify = identifyRepository.save(identify);
        return identify;
    }


    @Override
    public Identify createIdentification(ImageFormat imageFormat, int imageQuality, byte[] prob, List<NMatchingResult> nMatchingResults) {
        Identify identify = Identify.builder()
                .caseType(CaseType.IDENTIFICATION)
                .imageFormat(imageFormat)
                .imageQuality(imageQuality)
                .prob(prob)
                .build();

        for (int i = 0; i < nMatchingResults.size(); i++) {
            String id = nMatchingResults.get(i).getId();
            int index = id.indexOf('_');
            Long subjectId = Long.valueOf(id.substring(index + 1));
            Subject subject = subjectService.findById(subjectId);
            Candidate candidate = Candidate.builder()
                    .bioTemplate(subject.getBioTemplate())
                    .score(nMatchingResults.get(i).getScore())
                    .identify(identify)
                    .build();
            identify.addCandidate(candidate);
        }

        identify = identifyRepository.save(identify);
        return identify;
    }

    @Override
    public Identify findOne(long id) {
        Optional<Identify> identify = identifyRepository.findById(id);
        identify.orElseThrow(() -> new IdentifyNotFoundException(id));
        return identify.get();
    }

    @Override
    public List<Identify> findAll() {
        return identifyRepository.findAll();
    }

    @Override
    public List<Identify> findAllByCaseType(CaseType caseType) {
        return identifyRepository.findAllByCaseType(caseType);
    }

    @Override
    public Identify update(long id, Identify identify) {
        Identify forUpdate = findOne(id);
        forUpdate = identify;
        Identify updated = identifyRepository.save(forUpdate);
        return updated;
    }

    @Override
    public void delete(long id) {
        Identify identify = findOne(id);
        identifyRepository.delete(identify);
    }
}
