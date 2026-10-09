package com.example.meet;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import java.util.*;
import java.net.http.HttpClient;
import java.net.http.HttpResponse;
import java.net.URI;
import java.time.Duration;
@RestController @RequestMapping("/api")
public class MeetingController {
 private final MeetingStore store; private final LiveKitTokens tokens; private final String url;
 private final String internalUrl;
 public MeetingController(MeetingStore store,@Value("${meet.api-key}") String key,@Value("${meet.api-secret}") String secret,@Value("${meet.public-url}") String url,@Value("${meet.internal-url:http://sfu:7880}") String internalUrl){this.store=store;this.tokens=new LiveKitTokens(key,secret);this.url=url;this.internalUrl=internalUrl;}
 // An omitted room triggers backend-generated ID. An explicit room remains supported for existing clients.
 public record NewRoom(@Pattern(regexp="[A-Za-z0-9_-]{3,64}") String room,@NotBlank @Size(max=60) String displayName){}
 public record Admission(@NotBlank @Pattern(regexp="[A-Za-z0-9_-]{3,64}") String room,@NotBlank @Size(max=60) String displayName){}
 public record JoinRequest(@NotBlank String room,@NotBlank String accessKey){}
 public record Decision(@NotBlank String requestId,boolean approve){}
 public record JoinResponse(String token,String serverUrl,String room,String role,String identity){}
 @PostMapping("/meetings") public Map<String,Object> create(@Valid @RequestBody NewRoom input){return input.room()==null?store.createGenerated(input.displayName().trim()):store.create(input.room(),input.displayName().trim());}
 @PostMapping("/admissions") public Map<String,Object> admission(@Valid @RequestBody Admission input){return store.request(input.room(),input.displayName().trim());}
 @GetMapping("/meetings/{room}/admissions/me") public Map<String,Object> admissionStatus(@PathVariable String room,@RequestHeader("X-Guest-Key") String key){return store.state(room,key);}
 @GetMapping("/meetings/{room}/admissions") public List<Map<String,Object>> queue(@PathVariable String room,@RequestHeader("X-Host-Key") String key){return store.queue(room,key);}
 @PostMapping("/meetings/{room}/admissions/decision") public Map<String,String> decide(@PathVariable String room,@RequestHeader("X-Host-Key") String key,@RequestBody Decision decision){store.decide(room,key,decision.requestId(),decision.approve());return Map.of("status","OK");}
 @GetMapping("/meetings/{room}/capacity") public MeetingStore.Capacity capacity(@PathVariable String room){return store.capacity(room);}
 @PostMapping("/join") public JoinResponse join(@Valid @RequestBody JoinRequest req){String identity=UUID.randomUUID().toString();boolean host=store.host(req.room(),req.accessKey());String name=store.join(req.room(),req.accessKey(),identity);return new JoinResponse(tokens.issue(req.room(),name,identity,host),url,req.room(),host?"HOST":"GUEST",identity);}
 @PostMapping("/meetings/{room}/leave") public Map<String,String> leave(@PathVariable String room,@RequestHeader("X-Guest-Key") String key){store.leave(room,key);return Map.of("status","LEFT");}
 @PostMapping("/meetings/{room}/end") public Map<String,String> end(@PathVariable String room,@RequestHeader("X-Host-Key") String key){store.requireHost(room,key);
  try {java.net.http.HttpRequest request=java.net.http.HttpRequest.newBuilder(URI.create(internalUrl+"/twirp/livekit.RoomService/DeleteRoom")).timeout(Duration.ofSeconds(4)).header("Authorization","Bearer "+tokens.adminToken(room)).header("Content-Type","application/json").POST(java.net.http.HttpRequest.BodyPublishers.ofString("{\"room\":\""+room+"\"}")).build();
   HttpResponse<String> response=HttpClient.newHttpClient().send(request,HttpResponse.BodyHandlers.ofString());
   if(response.statusCode()>=400 && !response.body().contains("not_found"))throw new IllegalStateException("SFU returned "+response.statusCode()+": "+response.body());
  }catch(Exception ex){throw new org.springframework.web.server.ResponseStatusException(HttpStatus.BAD_GATEWAY,"Could not stop the SFU room: "+ex.getMessage());}
  store.close(room,key);return Map.of("status","ENDED");}
 @GetMapping("/meetings/history") public List<Map<String,Object>> history(@RequestHeader("X-Host-Key") String key){return store.history(key);}
 @ExceptionHandler(MeetingStore.Denied.class) ResponseEntity<Map<String,String>> denied(Exception ex){return ResponseEntity.status(403).body(Map.of("error",ex.getMessage()));}
 @ExceptionHandler(MeetingStore.Conflict.class) ResponseEntity<Map<String,String>> conflict(Exception ex){return ResponseEntity.status(409).body(Map.of("error",ex.getMessage()));}
 @ExceptionHandler(MeetingStore.Missing.class) ResponseEntity<Map<String,String>> missing(Exception ex){return ResponseEntity.status(404).body(Map.of("error",ex.getMessage()));}
 @GetMapping("/protocols") public Map<String,Object> protocols(){return Map.of("transport","ICE / DTLS-SRTP","media","RTP/RTCP","negotiation","SDP","adaptation","SFU bandwidth estimation","inspectable","Browser getStats and captured SDP signalling events; encrypted payloads not exposed");}
}
