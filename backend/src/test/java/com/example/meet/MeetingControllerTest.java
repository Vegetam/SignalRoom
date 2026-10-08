package com.example.meet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import java.util.UUID;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(properties="DATABASE_URL=jdbc:h2:mem:tests;DB_CLOSE_DELAY=-1") @AutoConfigureMockMvc class MeetingControllerTest {
 @Autowired MockMvc mvc; @Autowired ObjectMapper mapper;
 @Test void admissionIsHostControlled() throws Exception {
  String room="room-"+UUID.randomUUID().toString().substring(0,8);
  var created=mvc.perform(post("/api/meetings").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("room",room,"displayName","Host")))).andExpect(status().isOk()).andReturn();
  String host=mapper.readTree(created.getResponse().getContentAsString()).get("hostKey").asText();
  var guest=mvc.perform(post("/api/admissions").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("room",room,"displayName","Guest")))).andExpect(status().isOk()).andReturn();
  var guestData=mapper.readTree(guest.getResponse().getContentAsString());String key=guestData.get("guestKey").asText();
  mvc.perform(post("/api/join").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("room",room,"accessKey",key)))).andExpect(status().isForbidden());
  mvc.perform(post("/api/meetings/"+room+"/admissions/decision").header("X-Host-Key","wrong").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("requestId",guestData.get("requestId").asText(),"approve",true)))).andExpect(status().isForbidden());
  mvc.perform(post("/api/meetings/"+room+"/admissions/decision").header("X-Host-Key",host).contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("requestId",guestData.get("requestId").asText(),"approve",true)))).andExpect(status().isOk());
  mvc.perform(post("/api/join").contentType(MediaType.APPLICATION_JSON).content(mapper.writeValueAsString(Map.of("room",room,"accessKey",key)))).andExpect(status().isOk()).andExpect(jsonPath("$.role").value("GUEST"));
  mvc.perform(get("/api/meetings/history").header("X-Host-Key",host)).andExpect(status().isOk());
 }
}
