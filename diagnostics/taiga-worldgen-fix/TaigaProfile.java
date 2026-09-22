import java.nio.file.*;
import java.util.*;
import jdk.jfr.consumer.*;
class TaigaProfile {
 public static void main(String[] args)throws Exception {
  int total=0,taiga=0; Map<String,Integer> calls=new HashMap<>();
  try(var r=new RecordingFile(Path.of(args[0]))) { while(r.hasMoreEvents()) {
   var e=r.readEvent(); if(!e.getEventType().getName().equals("jdk.ExecutionSample") || !e.getThread("sampledThread").getJavaName().equals("Server thread") || e.getStackTrace()==null)continue;
   total++; var names=e.getStackTrace().getFrames().stream().map(f->f.getMethod().getType().getName()+"."+f.getMethod().getName()).toList();
   int pos=names.indexOf("com.sosnitzka.taiga.util.Generator.generateOreDescending"); if(pos<0)continue; taiga++;
   new HashSet<>(names.subList(0,pos)).forEach(n->calls.merge(n,1,Integer::sum));
  }}
  System.out.println("Server samples="+total+"; TAIGA descending="+taiga);
  calls.entrySet().stream().sorted(Map.Entry.<String,Integer>comparingByValue().reversed()).limit(55).forEach(System.out::println);
 }
}
