package raf.bolnica1.patient;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import raf.bolnica1.patient.domain.constants.*;
import raf.bolnica1.patient.dto.general.PatientDto;

import java.sql.Date;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class PatientServiceIntegrationTest {

    @Autowired
    MockMvc mockMvc;
    @Autowired
    ObjectMapper objectMapper;
    private final String token = "eyJhbGciOiJIUzUxMiJ9.eyJpZCI6MSwidXNlcm5hbWUiOiJqb2huZG9lMSIsImxieiI6IkFCQzEyMyIsImlhdCI6MTY3ODc5Mzc3MywiZXhwIjoxNjc4ODI5NzczfQ.CivcCJRIxQLZt5DEW6lsxe1bxn9xcMEIjZks9p4chb1Xnm2YjcaLD5n0hAqmBht5Pn4fwhVmftiKiJvdEKH9sw";

    @Test
    void testRegisterPatient() throws Exception{
        PatientDto dto = createDto();

        mockMvc.perform(post("/patient/register")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());

    }

    @Test
    void testDeletePatient() throws Exception{

        PatientDto dto = createDto();
        dto.setName("Testoslav");
        dto.setSurname("Pacijentic");
        dto.setJmbg("6549873210");

        ResultActions resultActions = mockMvc.perform(post("/patient/register")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
        MvcResult mvcResult = resultActions.andReturn();

        String loginResponse = mvcResult.getResponse().getContentAsString();
        PatientDto patientDto = objectMapper.readValue(loginResponse, PatientDto.class);

        mockMvc.perform(delete("/patient/delete/"+patientDto.getLbp())
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }

    @Test
    void testUpdatePatient() throws Exception{
        PatientDto dto = createDto();

        ResultActions resultActions = mockMvc.perform(post("/patient/register")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(dto)))
                .andExpect(status().isOk());
        MvcResult mvcResult = resultActions.andReturn();

        String loginResponse = mvcResult.getResponse().getContentAsString();
        PatientDto patientDto = objectMapper.readValue(loginResponse, PatientDto.class);

        patientDto.setName("Perica");
        patientDto.setSurname("Mali");

        //azuriramo pacijenta koji ima id pacijenta koji je kreiran iznad
        mockMvc.perform(put("/patient")
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + token)
                        .content(objectMapper.writeValueAsString(patientDto)))
                .andExpect(status().isOk());
    }

    @Test
    void testFilterPatients() throws Exception{

        String jmbg = "12345678910";
        String lbp = "";
        String name = "Marko";
        String surname = "";

        mockMvc.perform(get("/patient/filter?lbp="+lbp+"&jmbg="+jmbg+"&name="+name+"&surname="+surname)
                        .contentType("application/json")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk());
    }


    private PatientDto createDto(){
        PatientDto dto = new PatientDto();
        dto.setLbp(UUID.randomUUID().toString());
        dto.setName("Petar");
        dto.setSurname("Petrovic");
        dto.setCitizenship(CountryCode.SRB);
        dto.setEmail("p4cijent@mail.com");
        dto.setBirthPlace("Beograd");
        dto.setJmbg("11111112345");
        dto.setGender(Gender.MUSKO);
        dto.setDateOfBirth(Date.valueOf("2011-11-11"));
        dto.setFamilyStatus(FamilyStatus.OBA_RODITELJA);
        dto.setExpertiseDegree(ExpertiseDegree.BEZ_OSNOVNOG);
        dto.setGuardianJmbg("20030612345");
        dto.setMaritalStatus(MaritalStatus.SAMAC_SAMICA);
        dto.setParentName("Marko");
        dto.setNumOfChildren(0);
        dto.setGuardianNameAndSurname("Marko Markovic");
        dto.setPhone("0630744261");
        dto.setProfession("");
        dto.setPlaceOfLiving("Beograd");
        return dto;
    }
}
