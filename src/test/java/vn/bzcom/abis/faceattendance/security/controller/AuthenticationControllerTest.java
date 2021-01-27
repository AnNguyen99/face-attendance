package vn.bzcom.abis.faceattendance.security.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kbjung.abis.neurotec.biometrics.utils.NImageUtils;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import vn.bzcom.abis.faceattendance.account.dto.AccountDto;
import vn.bzcom.abis.faceattendance.common.model.ImageFormat;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuthenticationControllerTest {

  @Autowired
  private MockMvc mockMvc;

  @Autowired
  private ObjectMapper objectMapper;

  @Test
  void logInByFaceAndCreateJwtToken() throws Exception {
    String testBase64Image = NImageUtils.imageFileToBase64String("./JKB.png");

    AccountDto.LoginFace loginFaceDto = AccountDto.LoginFace.builder()
        .faceImage(testBase64Image)
        .imageFormat(ImageFormat.PNG)
        .build();

    String reqContent = objectMapper.writeValueAsString(loginFaceDto);
    System.out.println("request body: " + reqContent);

    mockMvc.perform(post("/token/generate-token-by-face").content(reqContent).contentType(MediaType.APPLICATION_JSON))
        .andDo(print())
        .andExpect(status().isOk());

  }

}