package com.example.meet;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import jakarta.annotation.PostConstruct;
import java.security.SecureRandom;
import java.util.*;
@Service
public class MeetingStore {
 private final JdbcTemplate db; private final SecureRandom random=new SecureRandom();
 public MeetingStore(JdbcTemplate db){this.db=db;}
 @PostConstruct void init(){
  db.execute("CREATE TABLE IF NOT EXISTS meetings(room VARCHAR(64) PRIMARY KEY, owner_name VARCHAR(60) NOT NULL, owner_key VARCHAR(100) NOT NULL, created TIMESTAMP DEFAULT CURRENT_TIMESTAMP, ended TIMESTAMP, status VARCHAR(20) DEFAULT 'ACTIVE')");
  db.execute("CREATE TABLE IF NOT EXISTS attendees(id VARCHAR(100) PRIMARY KEY, room VARCHAR(64) NOT NULL, display_name VARCHAR(60) NOT NULL, ticket VARCHAR(100) UNIQUE NOT NULL, status VARCHAR(20) NOT NULL, created TIMESTAMP DEFAULT CURRENT_TIMESTAMP, identity VARCHAR(100))");
 }
 private String secret(){byte[] bs=new byte[32];random.nextBytes(bs);return Base64.getUrlEncoder().withoutPadding().encodeToString(bs);}
 public synchronized Map<String,Object> create(String room,String name){if(db.queryForObject("SELECT COUNT(*) FROM meetings WHERE room=?",Integer.class,room)>0)throw new Conflict("Room already exists");String key=secret();db.update("INSERT INTO meetings(room,owner_name,owner_key) VALUES(?,?,?)",room,name,key);return Map.of("room",room,"hostKey",key,"role","HOST");}
 public boolean host(String room,String key){return key!=null&&!key.isBlank()&&db.queryForObject("SELECT COUNT(*) FROM meetings WHERE room=? AND owner_key=? AND status='ACTIVE'",Integer.class,room,key)>0;}
 public void requireHost(String room,String key){if(!host(room,key))throw new Denied("Host key invalid");}
 public boolean active(String room){return db.queryForObject("SELECT COUNT(*) FROM meetings WHERE room=? AND status='ACTIVE'",Integer.class,room)>0;}
 public synchronized Map<String,Object> request(String room,String name){if(!active(room))throw new Missing("Meeting not found or ended");String id=UUID.randomUUID().toString(),ticket=secret();db.update("INSERT INTO attendees(id,room,display_name,ticket,status) VALUES(?,?,?,?,?)",id,room,name,ticket,"WAITING");return Map.of("requestId",id,"guestKey",ticket,"status","WAITING");}
 public Map<String,Object> state(String room,String ticket){List<Map<String,Object>> matches=db.queryForList("SELECT status,display_name,id FROM attendees WHERE room=? AND ticket=?",room,ticket);if(matches.isEmpty())throw new Denied("Invalid guest ticket");return matches.get(0);}
 public List<Map<String,Object>> queue(String room,String hostKey){requireHost(room,hostKey);return db.queryForList("SELECT id,display_name,status,created FROM attendees WHERE room=? ORDER BY created",room);}
 public synchronized void decide(String room,String hostKey,String id,boolean approve){requireHost(room,hostKey);String state=db.query("SELECT status FROM attendees WHERE room=? AND id=?",rs->rs.next()?rs.getString(1):null,room,id);if(!"WAITING".equals(state))throw new Conflict("Request is not waiting");if(approve&&occupied(room)>=59)throw new Conflict("Meeting capacity of 60 reached");db.update("UPDATE attendees SET status=? WHERE room=? AND id=?",approve?"APPROVED":"DENIED",room,id);}
 public int occupied(String room){return db.queryForObject("SELECT COUNT(*) FROM attendees WHERE room=? AND status IN ('APPROVED','JOINED')",Integer.class,room);}
 public synchronized String join(String room,String key,String identity){if(!active(room))throw new Missing("Meeting ended or unavailable");if(host(room,key))return db.queryForObject("SELECT owner_name FROM meetings WHERE room=?",String.class,room);List<Map<String,Object>> rs=db.queryForList("SELECT id,display_name,status FROM attendees WHERE room=? AND ticket=?",room,key);if(rs.isEmpty()||!(rs.get(0).getOrDefault("STATUS",rs.get(0).get("status")).equals("APPROVED")||rs.get(0).getOrDefault("STATUS",rs.get(0).get("status")).equals("JOINED")))throw new Denied("Await host approval");db.update("UPDATE attendees SET status='JOINED',identity=? WHERE id=?",identity,rs.get(0).getOrDefault("ID",rs.get(0).get("id")));return (String)rs.get(0).getOrDefault("DISPLAY_NAME",rs.get(0).get("display_name"));}
 public synchronized void leave(String room,String key){db.update("UPDATE attendees SET status='LEFT' WHERE room=? AND ticket=?",room,key);}
 public synchronized void close(String room,String hostKey){requireHost(room,hostKey);db.update("UPDATE meetings SET status='ENDED',ended=CURRENT_TIMESTAMP WHERE room=?",room);db.update("UPDATE attendees SET status='LEFT' WHERE room=? AND status IN ('JOINED','APPROVED','WAITING')",room);}
 public List<Map<String,Object>> history(String hostKey){return db.queryForList("SELECT room,owner_name,created,ended,status FROM meetings WHERE owner_key=? ORDER BY created DESC",hostKey);}
 public record Capacity(int admitted,int maximum){}
 public Capacity capacity(String room){return new Capacity(occupied(room)+1,60);}
 public static class Denied extends RuntimeException{Denied(String m){super(m);}}
 public static class Conflict extends RuntimeException{Conflict(String m){super(m);}}
 public static class Missing extends RuntimeException{Missing(String m){super(m);}}
}
