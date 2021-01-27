package vn.bzcom.abis.faceattendance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kbjung.abis.neurotec.biometrics.utils.NImageUtils;
import com.neurotec.biometrics.NBiometricStatus;
import com.neurotec.biometrics.NSubject;
import com.neurotec.images.NImageFormat;
import javafx.scene.effect.SepiaTone;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import vn.bzcom.abis.faceattendance.account.dto.AccountDto;
import vn.bzcom.abis.faceattendance.account.entity.Authority;
import vn.bzcom.abis.faceattendance.account.repository.AuthorityRepository;
import vn.bzcom.abis.faceattendance.common.dto.*;
import vn.bzcom.abis.faceattendance.common.model.*;
import vn.bzcom.abis.faceattendance.common.service.RecognitionService;
import vn.bzcom.abis.faceattendance.subject.entity.Subject;
import vn.bzcom.abis.faceattendance.subject.service.SubjectService;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@RequiredArgsConstructor

@Component
public class DatabaseInitializer implements CommandLineRunner {

    private final AuthorityRepository authorityRepository;

    private final SubjectService subjectService;

    private final RecognitionService recognitionService;

    @Override
    public void run(String... args) throws Exception {
        createRoles();
        createAdminUser();
        createNormalUser();
    }

    private void createRoles() {
        if (authorityRepository.count() == 0) {
//            for (int i = 0; i < Role.values().length; i++) {
//                Authority authority = Authority.builder().role(Role.values()[i]).build();
//                authorityRepository.save(authority);
//            }

            List<Authority> authorities = Arrays.stream(Role.values()).map(role -> {
                return Authority.builder().role(role).build();
            }).collect(Collectors.toList());
            authorityRepository.saveAll(authorities);
        }
    }

    private void createAdminUser() throws Exception {
        if (subjectService.count() > 0) {
            return;
        }
        String inputFilePath = "./JKB.png";
        String testString = NImageUtils.imageFileToBase64String(inputFilePath);

        Authority authority = authorityRepository.findByRole(Role.ADMIN);
        if (authority == null) {
            log.error("Cannot find {} role", Role.ADMIN.name());
            return;
        }

        Set<Role> roles = new HashSet<>();
        roles.add(authority.getRole());

        Auth auth = Auth.builder()
            .username("admin")
            .password("bzcomAdmin")
            .roles(roles)
            .build();

        AccountDto.SignUpReq dtoTest = AccountDto.SignUpReq.builder()
            .auth(auth)
            .bioGraphy(
                BioGraphy.builder()
                    .firstName("BzCom")
                    .lastName("BzCom")
                    .birthDate(LocalDate.of(2010, 1, 1))
                    .gender(Gender.MALE)
                    .nid("201001011")
                    .build())
            .contact(
                Contact.builder()
                    .email("bzcom@gmail.com")
                    .phoneNumber("0968-057-949")
                    .address(
                        Address.builder()
                            .street("7 ton that thuyet")
                            .district("cau giay")
                            .province("hanoi")
                            .country("vietnam")
                            .zip("")
                            .build())
                    .build())
            .image(
                Image.builder()
                    .base64Image(testString)
                    .format(ImageFormat.PNG)
                    .bioType(BioType.FACE)
                    .quality(0)
                    .pose(Pose.FACE_FRONT)
                    .enabled(true)
                    .build())
            .build();

//        ObjectMapper objectMapper = new ObjectMapper();
//        log.info("JSON: " + objectMapper.writeValueAsString(dtoTest));

        String encodedString = dtoTest.getImage().getBase64Image();
        NSubject faceSubject = recognitionService.extractTemplate(encodedString, dtoTest.getImage().getFormat());
        if (faceSubject.getFaces().size() < 2) {
            log.error("face extract failed");
            return;
        }

        String imagePath = generateFaceImagePath();
        String filePath = "./uploads/" + imagePath;
        Path path = Paths.get(filePath);
        if (Files.notExists(path)) {
            Files.createDirectories(Paths.get(filePath));
        }
        String fileName = dtoTest.getBioGraphy().getNid() + "_FACE.png";

        faceSubject.getFaces().get(1).getImage().save(filePath +fileName , NImageFormat.getPNG());

        int quality = (int) faceSubject.getFaces().get(1).getObjects().get(0).getQuality();
        byte[] faceTemplate = faceSubject.getTemplateBuffer().toByteArray();
        ImageInfo imageInfo = ImageInfo.builder()
            .imageFormat(ImageFormat.PNG)
            .imageUrl("/" + imagePath + fileName)
            .imageQuality(quality)
            .bioType(BioType.FACE)
            .pose(Pose.FACE_FRONT)
            .enabled(true)
            .build();

        Subject subject = subjectService.create(dtoTest, imageInfo, faceTemplate);
        faceSubject.setId(String.valueOf(subject.getId()));
        NBiometricStatus biometricStatus = recognitionService.enrollOnServer(faceSubject);
        if (biometricStatus == NBiometricStatus.DUPLICATE_ID)
            log.debug("admin user template was duplicated on MMA with id: {}", subject.getId());
        log.info("admin user created with password: bzcomAdmin by id: {}", subject.getId());
    }

    private void createNormalUser() throws Exception {
        if (subjectService.count() > 1) {
            return;
        }
        String inputFilePath = "./tuan.png";
        String testString = NImageUtils.imageFileToBase64String(inputFilePath);

        Authority authority = authorityRepository.findByRole(Role.USER);
        if (authority == null) {
            log.error("Cannot find {} role", Role.USER.name());
            return;
        }

        Set<Role> roles = new HashSet<>();
        roles.add(authority.getRole());

        Auth auth = Auth.builder()
            .username("tuan")
            .password("tuan1234")
            .roles(roles)
            .build();

        AccountDto.SignUpReq dtoTest = AccountDto.SignUpReq.builder()
            .auth(auth)
            .bioGraphy(
                BioGraphy.builder()
                    .firstName("Quoc Tuan")
                    .lastName("Luong")
                    .birthDate(LocalDate.of(1992, 1, 1))
                    .gender(Gender.MALE)
                    .nid("19921101")
                    .build())
            .contact(
                Contact.builder()
                    .email("bzcom1@gmail.com")
                    .phoneNumber("0968-057-949")
                    .address(
                        Address.builder()
                            .street("7 ton that thuyet")
                            .district("cau giay")
                            .province("hanoi")
                            .country("vietnam")
                            .zip("")
                            .build())
                    .build())
            .image(
                Image.builder()
                    .base64Image(testString)
                    .format(ImageFormat.PNG)
                    .bioType(BioType.FACE)
                    .quality(0)
                    .pose(Pose.FACE_FRONT)
                    .enabled(true)
                    .build())
            .build();

//        ObjectMapper objectMapper = new ObjectMapper();
//        log.info("JSON: " + objectMapper.writeValueAsString(dtoTest));


        String encodedString = dtoTest.getImage().getBase64Image();
        log.info("Start face extract");
        NSubject faceSubject = recognitionService.extractTemplate(encodedString, dtoTest.getImage().getFormat());
        if (faceSubject.getFaces().size() < 2) {
            log.error("face extract failed");
            return;
        }

        String imagePath = generateFaceImagePath();
        String filePath = "./uploads/" + imagePath;
        Path path = Paths.get(filePath);
        if (Files.notExists(path)) {
            Files.createDirectories(Paths.get(filePath));
        }
        String fileName = dtoTest.getBioGraphy().getNid() + "_FACE.png";
        faceSubject.getFaces().get(1).getImage().save(filePath +fileName , NImageFormat.getPNG());

        int quality = (int) faceSubject.getFaces().get(1).getObjects().get(0).getQuality();
        byte[] faceTemplate = faceSubject.getTemplateBuffer().toByteArray();
        ImageInfo imageInfo = ImageInfo.builder()
            .imageFormat(ImageFormat.PNG)
            .imageUrl("/" + imagePath + fileName)
            .imageQuality(quality)
            .bioType(BioType.FACE)
            .pose(Pose.FACE_FRONT)
            .enabled(true)
            .build();

        Subject subject = subjectService.create(dtoTest, imageInfo, faceTemplate);
        faceSubject.setId(String.valueOf(subject.getId()));
        NBiometricStatus biometricStatus = recognitionService.enrollOnServer(faceSubject);
        if (biometricStatus == NBiometricStatus.DUPLICATE_ID)
            log.debug("admin user template was duplicated on MMA with id: {}", subject.getId());
        log.info("admin user created with password: bzcomAdmin by id: {}", subject.getId());
    }

    private String generateFaceImagePath() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd/HH/mm/");
        String imagePath = LocalDateTime.now().format(formatter);
        return imagePath;
    }
}
