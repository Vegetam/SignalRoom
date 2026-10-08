package com.example.meet;
import org.junit.jupiter.api.Test;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import static org.junit.jupiter.api.Assertions.*;
class LiveKitTokensTest {
 @Test void tokenHasThreeSegmentsAndRoomGrant() {
   String token = new LiveKitTokens("devkey", "developmentsecretmustbe32characterslong").issue("team_123", "Alice");
   String[] parts = token.split("\\.");
   assertEquals(3, parts.length);
   String payload = new String(Base64.getUrlDecoder().decode(parts[1]), StandardCharsets.UTF_8);
   assertTrue(payload.contains("team_123"));
   assertTrue(payload.contains("roomJoin"));
   assertTrue(payload.contains("Alice"));
 }
 @Test void rejectsWeakSecret() { assertThrows(IllegalArgumentException.class, () -> new LiveKitTokens("k","short")); }
}
