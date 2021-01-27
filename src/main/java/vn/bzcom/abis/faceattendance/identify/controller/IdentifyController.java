package vn.bzcom.abis.faceattendance.identify.controller;

import com.kbjung.abis.neurotec.biometrics.utils.NImageUtils;
import com.neurotec.biometrics.NLRecord;
import com.neurotec.biometrics.NMatchingResult;
import com.neurotec.biometrics.NSubject;
import com.neurotec.biometrics.NTemplate;
import com.neurotec.images.NImage;
import com.neurotec.images.NImageFormat;
import com.neurotec.io.NBuffer;
import com.sun.javafx.geom.transform.Identity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.repository.query.Param;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.bzcom.abis.faceattendance.common.dto.ApiResponse;
import vn.bzcom.abis.faceattendance.common.dto.BioGraphy;
import vn.bzcom.abis.faceattendance.common.dto.Image;
import vn.bzcom.abis.faceattendance.common.model.ImageFormat;
import vn.bzcom.abis.faceattendance.common.service.RecognitionService;
import vn.bzcom.abis.faceattendance.identify.dto.CandidateListItem;
import vn.bzcom.abis.faceattendance.identify.dto.IdentifyDto;
import vn.bzcom.abis.faceattendance.identify.entity.CaseType;
import vn.bzcom.abis.faceattendance.identify.entity.Identify;
import vn.bzcom.abis.faceattendance.identify.service.IdentifyService;

import vn.bzcom.abis.faceattendance.subject.entity.BioTemplate;
import vn.bzcom.abis.faceattendance.subject.entity.Subject;
import vn.bzcom.abis.faceattendance.subject.service.SubjectService;

import java.util.*;

@RequiredArgsConstructor
@Slf4j

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/identifies")
public class IdentifyController {

    private final IdentifyService identifyService;

    private final SubjectService subjectService;

    private final RecognitionService recognitionService;

    @PostMapping("verification/{subjectId}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<IdentifyDto.VerificationRes> verify(
            @PathVariable("subjectId") long id,
            @RequestBody IdentifyDto.Req dto) throws Exception {

        // step 1: get bioTemplate of subject by id
        Subject subject = subjectService.findById(id);
        byte[] template = subject.getBioTemplate().getTemplate();

        // step 2: verification
        NSubject probSubject = recognitionService.verify(dto.getProb(), dto.getImageFormat(), template);

        // step 3: convert face token NImage to byte array
        NImage probTokenImage = probSubject.getFaces().get(1).getImage();
        int imageQulaity = probSubject.getFaces().get(1).getObjects().get(0).getQuality();
        byte[] prob = probTokenImage.save(NImageFormat.getPNG()).toByteArray();

        // step 4: extract matching score
        log.info("Subject ID: " + probSubject.getId());
        int score = probSubject.getMatchingResults().get(0).getScore();

        Identify identify = identifyService.createVerification(ImageFormat.PNG, imageQulaity, prob, subject, score);

        IdentifyDto.VerificationRes res =
                IdentifyDto.VerificationRes.builder()
                        .matched(score > 0)
                        .score(score)
                        .tokenImage(NImageUtils.NImageToBase64String(probTokenImage))
                        .imageFormat(ImageFormat.PNG)
                        .nlAttributes(probSubject.getFaces().get(1).getObjects().get(0))
                        .build();

        return ApiResponse.<IdentifyDto.VerificationRes>builder()
                .message("Verification finished")
                .status(200)
                .result(res)
                .build();
    }

    @PostMapping("identification-local")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<IdentifyDto.IdentificationRes> identifyLocal(
            @RequestBody IdentifyDto.Req dto) throws Exception {

        // step 1: get bioTemplate all of subject
        List<Subject> subjects = subjectService.findAll();

        List<NSubject> referenceSubjects = new ArrayList<>();

        for (int i = 0; i < subjects.size(); i++) {
            BioTemplate bioTemplate = subjects.get(i).getBioTemplate();
            NSubject refrenceSubject = createSubject(bioTemplate.getTemplate(), String.valueOf(bioTemplate.getSubject().getId()));
            referenceSubjects.add(refrenceSubject);
        }

        // step 2: create prob subject
        NSubject probSubject = createSubject(dto.getProb(), dto.getImageFormat());

        // step 2: identification
        NSubject resultSubject = recognitionService.identify(probSubject, referenceSubjects);

        // step 3: convert face token NImage to byte array
        NImage probTokenImage = resultSubject.getFaces().get(1).getImage();
        int imageQuality = resultSubject.getFaces().get(1).getObjects().get(0).getQuality();
        byte[] prob = probTokenImage.save(NImageFormat.getPNG()).toByteArray();

        // step 4: extract matching score
        List<NMatchingResult> matchingResults = probSubject.getMatchingResults();

        // step 5: register identification data with DB
        Identify identify = identifyService.createIdentification(ImageFormat.PNG, imageQuality, prob, matchingResults);

        // step 6: return results
        Set<CandidateListItem> candidateList = new HashSet<>();
        for (int i = 0; i < matchingResults.size(); i++) {
            Subject subject = subjectService.findById(Long.parseLong(matchingResults.get(i).getId()));
            BioGraphy bioGraphy = BioGraphy.builder()
                    .nid(subject.getNid().getValue())
                    .gender(subject.getGender())
                    .firstName(subject.getName().getFirstName())
                    .lastName(subject.getName().getLastName())
                    .birthDate(subject.getBirthDate())
                    .build();
            Image image = Image.builder()
                    .quality(subject.getEnabledImage().getQuality())
                    .bioType(subject.getEnabledImage().getBioType())
                    .pose(subject.getEnabledImage().getPose())
                    .format(subject.getEnabledImage().getFormat())
                    .base64Image(subject.getEnabledImage().getBase64Image())
                    .enabled(true)
                    .build();

            CandidateListItem candidateListItem = CandidateListItem.builder()
                    .subjectId(Long.parseLong(matchingResults.get(i).getId()))
                    .score(matchingResults.get(i).getScore())
                    .bioGraphy(bioGraphy)
                    .image(image)
                    .build();
            candidateList.add(candidateListItem);
        }

        IdentifyDto.IdentificationRes res =
                IdentifyDto.IdentificationRes.builder()
                        .tokenImage(NImageUtils.NImageToBase64String(probTokenImage))
                        .imageFormat(ImageFormat.PNG)
                        .candiateList(candidateList)
                        .nlAttributes(resultSubject.getFaces().get(1).getObjects().get(0))
                        .build();

        return ApiResponse.<IdentifyDto.IdentificationRes>builder()
                .message("Verification finished")
                .status(200)
                .result(res)
                .build();
    }

    @PostMapping("identification-mma")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<IdentifyDto.IdentificationRes> identifyMMA(
        @RequestBody IdentifyDto.Req dto) throws Exception {

        // step 1: create prob subject
        NSubject probSubject = createSubject(dto.getProb(), dto.getImageFormat());

        // step 2: identification
        List<NMatchingResult> matchingResults = recognitionService.identifyOnServer(probSubject);

        // step 3: convert face token NImage to byte array
        NImage probTokenImage = probSubject.getFaces().get(1).getImage();
        int imageQuality = probSubject.getFaces().get(1).getObjects().get(0).getQuality();
        byte[] prob = probTokenImage.save(NImageFormat.getPNG()).toByteArray();

        // step 5: register identification data with DB
        Identify identify = identifyService.createIdentification(ImageFormat.PNG, imageQuality, prob, matchingResults);

        // step 6: return results
        Set<CandidateListItem> candidateList = new HashSet<>();
        for (int i = 0; i < matchingResults.size(); i++) {
            Subject subject = subjectService.findById(Long.parseLong(matchingResults.get(i).getId()));
            BioGraphy bioGraphy = BioGraphy.builder()
                .nid(subject.getNid().getValue())
                .gender(subject.getGender())
                .firstName(subject.getName().getFirstName())
                .lastName(subject.getName().getLastName())
                .birthDate(subject.getBirthDate())
                .build();
            Image image = Image.builder()
                .quality(subject.getEnabledImage().getQuality())
                .bioType(subject.getEnabledImage().getBioType())
                .pose(subject.getEnabledImage().getPose())
                .format(subject.getEnabledImage().getFormat())
                .base64Image(subject.getEnabledImage().getBase64Image())
                .enabled(true)
                .build();

            CandidateListItem candidateListItem = CandidateListItem.builder()
                .subjectId(Long.parseLong(matchingResults.get(i).getId()))
                .score(matchingResults.get(i).getScore())
                .bioGraphy(bioGraphy)
                .image(image)
                .build();
            candidateList.add(candidateListItem);
        }

        IdentifyDto.IdentificationRes res =
            IdentifyDto.IdentificationRes.builder()
                .tokenImage(NImageUtils.NImageToBase64String(probTokenImage))
                .imageFormat(ImageFormat.PNG)
                .candiateList(candidateList)
                .nlAttributes(probSubject.getFaces().get(1).getObjects().get(0))
                .build();

        return ApiResponse.<IdentifyDto.IdentificationRes>builder()
            .message("Identification finished")
            .status(200)
            .result(res)
            .build();
    }


    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<IdentifyDto.Response>> findAll() throws  Exception {
        List<Identify> identifies = identifyService.findAll();

        List<IdentifyDto.Response> responses = new ArrayList<>();
        for (int i = 0; i < identifies.size(); i++) {
            responses.add(IdentifyDto.Response.builder().identify(identifies.get(i)).build());
        }

        return ApiResponse.<List<IdentifyDto.Response>>builder()
                .status(HttpStatus.OK.value())
                .message("return all list of idenitfy records: " + identifies.size())
                .result(responses)
                .build();
    }

    @GetMapping("/search/{caseType}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<IdentifyDto.ResponseDetail>> findAllByCaseType(@PathVariable("caseType") CaseType caseType) throws  Exception {

        List<Identify> identifies = identifyService.findAllByCaseType(caseType);

        List<IdentifyDto.ResponseDetail> responses = new ArrayList<>();

        for (int i = 0; i < identifies.size(); i++) {
            responses.add(IdentifyDto.ResponseDetail.builder().identify(identifies.get(i)).build());
        }

        return ApiResponse.<List<IdentifyDto.ResponseDetail>>builder()
                .status(HttpStatus.OK.value())
                .message("return all list of idenitfy records: " + identifies.size())
                .result(responses)
                .build();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<IdentifyDto.ResponseDetail> findOne(@PathVariable("id") long id) throws Exception {
        Identify identify = identifyService.findOne(id);
        IdentifyDto.ResponseDetail response = IdentifyDto.ResponseDetail.builder()
                .identify(identify)
                .build();
        return ApiResponse.<IdentifyDto.ResponseDetail>builder()
                .status(HttpStatus.OK.value())
                .message("find successfully with id: " + identify.getId())
                .result(response)
                .build();
    }

    private NSubject createSubject(String probImage, ImageFormat format) {
        NSubject subject = recognitionService.extractTemplate(probImage, format);
        subject.setId("prob");
        return subject;
    }

    private NSubject createSubject(byte[] refTemplate, String subjectId) {
        NSubject subject = new NSubject();
        NTemplate template = new NTemplate(NBuffer.fromArray(refTemplate));
        if (template.getFaces() != null) {
            for(NLRecord record : template.getFaces().getRecords()) {
                log.info("Quality : " + record.getQuality());
            }
        }
        subject.setTemplate(template);
        subject.setId(subjectId);
        return subject;
    }
}
