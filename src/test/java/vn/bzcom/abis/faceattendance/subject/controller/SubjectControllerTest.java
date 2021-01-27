package vn.bzcom.abis.faceattendance.subject.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.kbjung.abis.neurotec.biometrics.utils.NImageUtils;
import javafx.geometry.Pos;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.json.JacksonTester;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import vn.bzcom.abis.faceattendance.common.dto.BioGraphy;
import vn.bzcom.abis.faceattendance.common.dto.BioType;
import vn.bzcom.abis.faceattendance.common.dto.Contact;
import vn.bzcom.abis.faceattendance.common.dto.Image;
import vn.bzcom.abis.faceattendance.common.model.Address;
import vn.bzcom.abis.faceattendance.common.model.Gender;
import vn.bzcom.abis.faceattendance.common.model.ImageFormat;
import vn.bzcom.abis.faceattendance.common.model.Pose;
import vn.bzcom.abis.faceattendance.subject.dto.SubjectDto;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class SubjectControllerTest {

    private JacksonTester<SubjectDto.CreateReq> reqJacksonTester;

    @Autowired
    MockMvc mockMvc;

    @Before
    public void setUp() {
        JacksonTester.initFields(this, new ObjectMapper());
    }

    @Test
    @Ignore
    public void createSubjectTest() throws Exception {

        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModule(new JavaTimeModule());
        mapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        BioGraphy bioGraphy = BioGraphy.builder()
                .firstName("Kyung Bae")
                .lastName("Jung")
                .nid("987654321")
                .gender(Gender.MALE)
                .birthDate(LocalDate.of(1969, 11, 2))
                .build();

        Address address = Address.builder()
                .street("84 ngo 57 dung me tri ha")
                .city("nam tu liem")
                .province("ha noi")
                .country("vietnam")
                .zip("123567")
                .build();

        Contact contact = Contact.builder()
                .address(address)
                .phoneNumber("0382682585")
                .email("iland112@naver.com")
                .build();
        String base64Image = NImageUtils.imageFileToBase64String("./JKB.png");

        Image image = Image.builder()
                .quality(100)
                .bioType(BioType.FACE)
                .format(ImageFormat.PNG)
                .base64Image(base64Image)
                .enabled(true)
                .pose(Pose.FACE_FRONT)
                .build();

        SubjectDto.CreateReq dto = SubjectDto.CreateReq.builder()
                .bioGraphy(bioGraphy)
                .contact(contact)
                .image(image)
                .build();

        System.out.println(dto);

        String reqContent = mapper.writeValueAsString(dto);

        mockMvc.perform(post("/subjects")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(reqContent))
                .andDo(print())
                .andExpect(status().isCreated());
    }
}
