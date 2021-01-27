package vn.bzcom.abis.faceattendance.account.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.neurotec.biometrics.NBiometricStatus;
import com.neurotec.biometrics.NSubject;
import com.neurotec.images.NImageFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import vn.bzcom.abis.faceattendance.account.dto.AccountDto;
import vn.bzcom.abis.faceattendance.account.entity.Account;
import vn.bzcom.abis.faceattendance.account.service.AccountService;
import vn.bzcom.abis.faceattendance.common.dto.ApiResponse;
import vn.bzcom.abis.faceattendance.common.model.ImageInfo;
import vn.bzcom.abis.faceattendance.common.model.Pose;
import vn.bzcom.abis.faceattendance.common.service.RecognitionService;
import vn.bzcom.abis.faceattendance.subject.entity.Subject;
import vn.bzcom.abis.faceattendance.subject.service.SubjectService;

import javax.validation.Valid;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor

@CrossOrigin(origins = "*", maxAge = 3600)
@RestController
@RequestMapping("accounts")
public class AccountController {

    private final AccountService accountService;

    private final SubjectService subjectService;

    private final RecognitionService recognitionService;

    @PostMapping()
    @ResponseStatus(value = HttpStatus.CREATED)
    public ApiResponse<AccountDto.SignUpRes> createAccount(@RequestBody @Valid final AccountDto.SignUpReq dto) throws Exception {

        log.info("createAccount() called");

        String encodedString = dto.getImage().getBase64Image();

        // Call recognition Service
        NSubject faceSubject = recognitionService.extractTemplate(encodedString, dto.getImage().getFormat());
        // create folder for save face token image
        String imagePath = getImagePath();
        String filePath = "./uploads/" + imagePath;
        Path path = Paths.get(filePath);

        if (Files.notExists(path)) {
            Files.createDirectories(Paths.get(filePath));
        }

        // save token face image to file repository (path => [application root]/uploads/yyyyMMdd/HH/MM)
        // accept various image format
        String fileName = dto.getBioGraphy().getNid() + "_face." + dto.getImage().getFormat().name();
        switch (dto.getImage().getFormat().name()) {
            case "JPG":
            case "JPEG":
                faceSubject.getFaces().get(1).getImage().save(filePath +fileName , NImageFormat.getJPEG());
                break;
            case "PNG":
                faceSubject.getFaces().get(1).getImage().save(filePath + fileName, NImageFormat.getPNG());
                break;
            case "WSQ":
                faceSubject.getFaces().get(1).getImage().save(filePath + fileName, NImageFormat.getWSQ());
                break;
            case "TIFF":
                faceSubject.getFaces().get(1).getImage().save(filePath +fileName , NImageFormat.getTIFF());
                break;
        }

        // get image quality and face template from faceSubject
        int quality = (int) faceSubject.getFaces().get(1).getObjects().get(0).getQuality();
        byte[] faceTemplate = faceSubject.getTemplateBuffer().toByteArray();
        // build image info
        ImageInfo imageInfo = ImageInfo.builder()
                .imageUrl("/" + imagePath + fileName)
                .imageQuality(quality)
                .imageFormat(dto.getImage().getFormat())
                .bioType(dto.getImage().getBioType())
                .pose(Pose.FACE_FRONT)
                .enabled(true)
                .build();

        // insert Subject data to database
        Subject subject = subjectService.create(dto, imageInfo, faceTemplate);

        // enroll to MMA
        faceSubject.setId(subject.getId().toString());
        NBiometricStatus biometricStatus = recognitionService.enrollOnServer(faceSubject);
        log.debug("MMA enroll results: {}", biometricStatus.name());

        // covert account data to resDto
        AccountDto.SignUpRes resDto = new AccountDto.SignUpRes(subject);
        ObjectMapper objectMapper = new ObjectMapper();
        log.debug("result: " + objectMapper.writeValueAsString(resDto));

        return ApiResponse.<AccountDto.SignUpRes>builder()
                    .status(HttpStatus.CREATED.value())
                    .message("New account created with " + resDto.getSubjectId())
                    .result(resDto)
                    .build();

    }

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<List<AccountDto.Res>> listAccount() throws Exception {
        List<Account> accounts = accountService.findAll();
        List<AccountDto.Res> results = accounts.stream()
                .map(account -> new AccountDto.Res(account))
                .collect(Collectors.toList());

        return ApiResponse.<List<AccountDto.Res>>builder()
                .status(HttpStatus.OK.value())
                .message("Account list fetched successfully")
                .result(results)
                .build();
    }

    @GetMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<AccountDto.Res> getAccount(@PathVariable long id) throws Exception {
        return ApiResponse.<AccountDto.Res>builder()
                .status(HttpStatus.OK.value())
                .message("Account fetched successfully")
                .result(new AccountDto.Res(accountService.findById(id)))
                .build();
    }

    @PutMapping("/changepassword/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<AccountDto.Res> changePassword(
            @PathVariable("id") Long id,
            @RequestBody AccountDto.PasswordChangeReq updateReq) throws Exception {
        return ApiResponse.<AccountDto.Res>builder()
                .status(HttpStatus.OK.value())
                .message("Account updated successfully")
                .result(new AccountDto.Res(accountService.changePassword(id, updateReq)))
                .build();
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<AccountDto.Res> updateAccount(
            @PathVariable("id") Long id,
            @RequestBody AccountDto.SignUpReq updateReq) throws Exception {

        Account account = accountService.updateAccount(id, updateReq);

        return ApiResponse.<AccountDto.Res>builder()
                .status(HttpStatus.OK.value())
                .message("Account updated successfully")
                .result(new AccountDto.Res(account))
                .build();
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.OK)
    public ApiResponse<Void> deleteAccount(@PathVariable long id) throws Exception {
        accountService.deleteAccount(id);
        return ApiResponse.<Void>builder()
                .status(HttpStatus.OK.value())
                .message("User deleted successfully.")
                .result(null)
                .build();
    }

    // Create face image path based on local datetime.
    private String getImagePath() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd/HH/mm/");
        String imagePath = LocalDateTime.now().format(formatter);
        return imagePath;
    }
}
