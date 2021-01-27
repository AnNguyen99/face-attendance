package vn.bzcom.abis.faceattendance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kbjung.abis.neurotec.biometrics.utils.NImageUtils;
import com.neurotec.biometrics.NSubject;
import com.neurotec.images.NImageFormat;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import vn.bzcom.abis.faceattendance.account.dto.AccountDto;
import vn.bzcom.abis.faceattendance.common.StorageProperties;
import vn.bzcom.abis.faceattendance.common.dto.*;
import vn.bzcom.abis.faceattendance.common.model.*;
import vn.bzcom.abis.faceattendance.common.service.RecognitionService;
import vn.bzcom.abis.faceattendance.subject.service.SubjectService;

import javax.security.auth.Subject;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@SpringBootApplication
@EnableConfigurationProperties(StorageProperties.class)
public class FaceAttendanceApplication {
    public static void main(String[] args) {
        SpringApplication.run(FaceAttendanceApplication.class, args);
    }
}
