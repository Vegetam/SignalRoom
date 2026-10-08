package com.example.meet;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import java.util.UUID;

/** LiveKit-compatible HS256 join JWT. Room access MUST be authenticated in production. */
public final class LiveKitTokens {
  private final ObjectMapper mapper = new ObjectMapper();
  private final String key, secret;
  public LiveKitTokens(String key, String secret) {
    if (key == null || key.isBlank() || secret == null || secret.length() < 32) throw new IllegalArgumentException("LiveKit key and 32+ character secret required");
    this.key = key; this.secret = secret;
  }
  public String issue(String room, String displayName) {return issue(room,displayName,UUID.randomUUID().toString(),false);}
  public String issue(String room,String displayName,String identity,boolean host) {
    long now = Instant.now().getEpochSecond();
    
    try {
      String header = b64(mapper.writeValueAsBytes(Map.of("alg", "HS256", "typ", "JWT")));
      String payload = b64(mapper.writeValueAsBytes(Map.of(
        "iss", key, "sub", identity, "name", displayName, "iat", now, "nbf", now - 2,
        "exp", now + 3600,
        "video", Map.of("room", room, "roomJoin", true, "canPublish", true, "canSubscribe", true, "canPublishData", true, "roomAdmin", host))));
      String content = header + "." + payload;
      Mac mac = Mac.getInstance("HmacSHA256");
      mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
      return content + "." + b64(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
    } catch (Exception e) { throw new IllegalStateException("Could not sign meeting token", e); }
  }
  public String adminToken(String room){
    long now=Instant.now().getEpochSecond();
    try {
      String h=b64(mapper.writeValueAsBytes(Map.of("alg","HS256","typ","JWT")));
      String body=b64(mapper.writeValueAsBytes(Map.of("iss",key,"sub","server-admin","iat",now,"nbf",now-2,"exp",now+120,
        "video",Map.of("roomAdmin",true,"room",room))));
      String content=h+"."+body;
      Mac mac=Mac.getInstance("HmacSHA256");mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8),"HmacSHA256"));
      return content+"."+b64(mac.doFinal(content.getBytes(StandardCharsets.UTF_8)));
    }catch(Exception e){throw new IllegalStateException("Could not sign admin token",e);}
  }
  private static String b64(byte[] bytes) { return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes); }
}
