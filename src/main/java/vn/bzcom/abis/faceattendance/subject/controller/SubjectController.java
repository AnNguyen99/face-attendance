package vn.bzcom.abis.faceattendance.subject.controller;

import com.neurotec.biometrics.NBiometricStatus;
import com.neurotec.biometrics.NSubject;
import com.neurotec.images.NImageFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.bzcom.abis.faceattendance.common.dto.ApiResponse;
import vn.bzcom.abis.faceattendance.common.dto.BioType;
import vn.bzcom.abis.faceattendance.common.model.ImageFormat;
import vn.bzcom.abis.faceattendance.common.model.ImageInfo;
import vn.bzcom.abis.faceattendance.common.model.Pose;
import vn.bzcom.abis.faceattendance.common.service.RecognitionService;
import vn.bzcom.abis.faceattendance.subject.dto.SubjectDto;
import vn.bzcom.abis.faceattendance.subject.entity.Subject;
import vn.bzcom.abis.faceattendance.subject.service.SubjectService;

import javax.validation.Valid;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequiredArgsConstructor

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("/subjects")
public class SubjectController {

    private final SubjectService subjectService;

    private final RecognitionService recognitionService;

    @PostMapping()
    @ResponseStatus(HttpStatus.CREATED)
    public ApiResponse<SubjectDto.SubjectRes> createSubject(@RequestBody @Valid SubjectDto.CreateReq dto) throws Exception {
        String base64Image = dto.getImage().getBase64Image();
        NSubject faceSubject = recognitionService.extractTemplate(base64Image, dto.getImage().getFormat());
        byte[] template = faceSubject.getTemplateBuffer().toByteArray();
        String imagePath = generateFaceImagePath();
        String filePath = "./uploads/" + imagePath;
        Path path = Paths.get(filePath);
        if (Files.notExists(path)) {
            Files.createDirectories(Paths.get(filePath));
        }
        String fileName = dto.getBioGraphy().getNid() + "_FACE.png";
        faceSubject.getFaces().get(1).getImage().save(filePath +fileName , NImageFormat.getPNG());
        int quality = (int) faceSubject.getFaces().get(1).getObjects().get(0).getQuality();
        ImageInfo imageInfo = ImageInfo.builder()
                .imageFormat(ImageFormat.PNG)
                .imageUrl("/" + imagePath + fileName)
                .imageQuality(quality)
                .bioType(BioType.FACE)
                .pose(Pose.FACE_FRONT)
                .enabled(true)
                .build();
        // insert to subject data to db
        Subject subject = subjectService.create(dto, imageInfo, template);

        // enroll to MMA
        faceSubject.setId(subject.getId().toString());
        NBiometricStatus biometricStatus = recognitionService.enrollOnServer(faceSubject);
        log.debug("MMA enroll results: {}", biometricStatus.name());

        SubjectDto.SubjectRes subjectRes = new SubjectDto.SubjectRes(subject);
        return ApiResponse.<SubjectDto.SubjectRes>builder()
                .status(HttpStatus.CREATED.value())
                .message("Subject created Successfully with id : " + subject.getId())
                .result(subjectRes)
                .build();
    }

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    private ApiResponse<SubjectDto.SubjectRes> findAll() throws Exception{
        List<Subject> subjects = subjectService.findAll();

        List<SubjectDto.SubjectRes> results = new ArrayList<>();

        for (Subject subject : subjects) {
            results.add(new SubjectDto.SubjectRes(subject));
        }

        return ApiResponse.<SubjectDto.SubjectRes>builder()
                .status(HttpStatus.OK.value())
                .message("get all subjects returned successfully!!")
                .result(results)
                .build();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    private ApiResponse<SubjectDto.SubjectRes> findById(@PathVariable("id") Long id) throws Exception{
        Subject subject = subjectService.findById(id);
        SubjectDto.SubjectRes res = new SubjectDto.SubjectRes(subject);

        return ApiResponse.<SubjectDto.SubjectRes>builder()
                .status(HttpStatus.OK.value())
                .message("requested subject returned successfully!!")
                .result(res)
                .build();
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public ApiResponse<SubjectDto.SubjectRes> updateSubject(
            @PathVariable("id") Long id,
            @RequestBody @Valid SubjectDto.CreateReq dto) throws Exception {

        String base64Image = dto.getImage().getBase64Image();
        NSubject faceSubject = recognitionService.extractTemplate(base64Image, dto.getImage().getFormat());
        byte[] template = faceSubject.getTemplateBuffer().toByteArray();
        String imagePath = generateFaceImagePath();
        String filePath = "./uploads/" + imagePath;
        Path path = Paths.get(filePath);
        if (Files.notExists(path)) {
            Files.createDirectories(Paths.get(filePath));
        }
        String fileName = dto.getBioGraphy().getNid() + "_FACE.png";
        faceSubject.getFaces().get(1).getImage().save(filePath +fileName , NImageFormat.getPNG());
        int quality = (int) faceSubject.getFaces().get(1).getObjects().get(0).getQuality();

        ImageInfo imageInfo = ImageInfo.builder()
                .imageFormat(ImageFormat.PNG)
                .imageUrl("/" + imagePath + fileName)
                .imageQuality(quality)
                .bioType(BioType.FACE)
                .pose(Pose.FACE_FRONT)
                .enabled(true)
                .build();

        Subject subject = subjectService.update(id, dto, imageInfo, template);

        SubjectDto.SubjectRes subjectRes = new SubjectDto.SubjectRes(subject);
        return ApiResponse.<SubjectDto.SubjectRes>builder()
                .status(HttpStatus.CREATED.value())
                .message("Subject created Successfully with id : " + subject.getId())
                .result(subjectRes)
                .build();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> deleteSubject(@PathVariable long id) throws Exception {
        Subject subject = subjectService.findById(id);
        subjectService.delete(subject);
        return ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("User deleted successfully.")
                .result(null)
                .build();
    }


    private String generateFaceImagePath() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd/HH/mm/");
        String imagePath = LocalDateTime.now().format(formatter);
        return imagePath;
    }

}
