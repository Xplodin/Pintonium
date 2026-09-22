import java.nio.file.*;
import java.util.*;
import jdk.jfr.consumer.*;
class WorldgenSummary {
 public static void main(String[] args) throws Exception {
  Map<String,Long> threads=new HashMap<>(), methods=new HashMap<>(), alloc=new HashMap<>();
  long server=0, bytes=0; Set<Long> first=new HashSet<>();
  try(var r=new RecordingFile(Path.of(args[0]))) { while(r.hasMoreEvents()) {
   var e=r.readEvent(); var n=e.getEventType().getName();
   if(n.equals("jdk.ObjectAllocationSample") && !first.add(e.getThread().getJavaThreadId())) {
    long w=e.getLong("weight"); bytes+=w;
    String s=e.getStackTrace()==null?"":String.join(" ",e.getStackTrace().getFrames().stream().map(f->f.getMethod().getType().getName()+"."+f.getMethod().getName()).toList());
    for(String key:List.of("matteroverdrive.handler.thread.RegisterItemsFromRecipes", "retrosophisticatedbackpacks", "bettercaves", "depthsupdate", "journeymap", "appeng.services.CompassService", "qioprocessing", "ChunkBuilderMeshingTask", "curvy_pipes")) if(s.contains(key)) alloc.merge(key,w,Long::sum);
   }
   if(!n.equals("jdk.ExecutionSample")) continue;
   String t=e.getThread("sampledThread").getJavaName(); threads.merge(t,1L,Long::sum);
   if(!t.equals("Server thread")) continue; server++;
   if(e.getStackTrace()==null) continue;
   Set<String> seen=new HashSet<>();
   for(var f:e.getStackTrace().getFrames()) {String m=f.getMethod().getType().getName()+"."+f.getMethod().getName(); if(seen.add(m)) methods.merge(m,1L,Long::sum);}
  }}
  System.out.println("Allocation signatures (overlapping bytes, total="+bytes+"):");print(alloc,bytes);
  System.out.println("Thread execution sample counts:"); print(threads,0);
  System.out.println("Server inclusive method samples (overlapping, denominator="+server+"):");print(methods,server);
 }
 static void print(Map<String,Long> m,long total) {m.entrySet().stream().sorted(Map.Entry.<String,Long>comparingByValue().reversed()).limit(65).forEach(e->System.out.printf("%7d %6.2f%% %s%n",e.getValue(),total==0?0:100.*e.getValue()/total,e.getKey()));}
}
