package vn.bzcom.abis.faceattendance.identify.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.kbjung.abis.neurotec.biometrics.utils.NImageUtils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;
import vn.bzcom.abis.faceattendance.common.model.ImageFormat;
import vn.bzcom.abis.faceattendance.identify.dto.IdentifyDto;
import vn.bzcom.abis.faceattendance.identify.entity.CaseType;
import vn.bzcom.abis.faceattendance.identify.service.IdentifyService;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class IdentifyControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private IdentifyService identifyService;

    static String reqContent;

    @BeforeAll
    protected static void setUp() throws Exception {

        String testBase64Image = NImageUtils.imageFileToBase64String("./JKB.png");
        IdentifyDto.Req req = IdentifyDto.Req.builder()
            .imageFormat(ImageFormat.PNG)
            .prob(testBase64Image)
            .build();

        ObjectMapper objectMapper = new ObjectMapper();
        reqContent = objectMapper.writeValueAsString(req);
    }

    @Test
    public void verifyTest() throws Exception {
        System.out.println("request body: " + reqContent);
        mockMvc.perform(post("/identifies/verification/1").content(reqContent).contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk());

        whenFindAllByCaseType_returnSpecifiedCaseTypeRecords(CaseType.VERIFICATION);
    }

//    @Test
//    public void identifyLocalTest() throws Exception {
//
//        mockMvc.perform(post("/identifies/identification-local").content(reqContent).contentType(MediaType.APPLICATION_JSON))
//                .andDo(print())
//                .andExpect(status().isOk());
//
//        whenFindAllByCaseType_returnSpecifiedCaseTypeRecords(CaseType.IDENTIFICATION);
//    }

    @Test
    public void identifyMMATest() throws Exception {

        mockMvc.perform(post("/identifies/identification-mma").content(reqContent).contentType(MediaType.APPLICATION_JSON))
            .andDo(print())
            .andExpect(status().isOk());

        whenFindAllByCaseType_returnSpecifiedCaseTypeRecords(CaseType.IDENTIFICATION);

    }

    private void whenFindAllByCaseType_returnSpecifiedCaseTypeRecords(CaseType caseType) throws Exception {
        MvcResult mvcResult = mockMvc.perform(get("/identifies/search/{caseType}", caseType.name()))
                .andDo(print())
                .andExpect(status().isOk())
                .andReturn();

        System.out.println("return: " + mvcResult.getResponse().getContentAsString());
    }
}