package com.example.meet;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest(properties={"DATABASE_URL=jdbc:h2:mem:collabtest;DB_CLOSE_DELAY=-1","signalroom.files=./target/test-uploads"}) @AutoConfigureMockMvc
class CollaborationControllerTest {
 @Autowired MockMvc mvc; @Autowired ObjectMapper json;
 @Test void accountsTeamsMembershipAndMessages() throws Exception {
  String email="user-"+UUID.randomUUID()+"@example.test";
  var register=mvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(json.writeValueAsString(Map.of("name","Developer","email",email,"password","safePassword1234")))).andExpect(status().isOk()).andReturn();
  String token=json.readTree(register.getResponse().getContentAsString()).get("token").asText();
  var teamResponse=mvc.perform(post("/api/v1/teams").header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON).content("{\"name\":\"Engineering\"}")).andExpect(status().isOk()).andReturn();
  var team=json.readTree(teamResponse.getResponse().getContentAsString());String teamId=team.get("id").asText(),channel=team.get("channelId").asText();
  mvc.perform(get("/api/v1/teams/"+teamId+"/channels")).andExpect(status().isUnauthorized());
  mvc.perform(get("/api/v1/teams")).andExpect(status().isUnauthorized());
  mvc.perform(get("/api/v1/channels/"+channel+"/messages")).andExpect(status().isUnauthorized());
  mvc.perform(post("/api/v1/channels/"+channel+"/messages").header("Authorization","Bearer "+token).contentType(MediaType.APPLICATION_JSON).content("{\"body\":\"Team sync at 10\"}")).andExpect(status().isOk());
  mvc.perform(get("/api/v1/channels/"+channel+"/messages").header("Authorization","Bearer "+token)).andExpect(status().isOk()).andExpect(jsonPath("$[0].BODY").value("Team sync at 10"));
  mvc.perform(get("/api/v1/teams").header("Authorization","Bearer "+token)).andExpect(status().isOk()).andExpect(jsonPath("$[0].NAME").value("Engineering"));
 }
}
