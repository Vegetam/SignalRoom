package com.example.meet;
import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DriverManagerDataSource;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;
class MeetingStoreTest {
 @Test void enforces60AdmittedGuests() {
  String url="jdbc:h2:mem:"+UUID.randomUUID().toString().replace("-","")+";DB_CLOSE_DELAY=-1";
  MeetingStore store=new MeetingStore(new JdbcTemplate(new DriverManagerDataSource(url,"sa","")));store.init();
  String room="capacity-test";String host=(String)store.create(room,"Host").get("hostKey");
  for(int i=0;i<59;i++){var g=store.request(room,"Guest"+i);store.decide(room,host,(String)g.get("requestId"),true);}
  assertEquals(59,store.occupied(room));
  var last=store.request(room,"Guest61");assertThrows(MeetingStore.Conflict.class,()->store.decide(room,host,(String)last.get("requestId"),true));
 }
}
