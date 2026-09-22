import java.nio.file.*;
import java.util.*;
import jdk.jfr.consumer.*;

class AnalyzeStutterJfr {
    static String stack(RecordedEvent event) {
        var trace=event.getStackTrace();if(trace==null)return "[no stack]";
        return String.join(" > ",trace.getFrames().stream().map(f->f.getMethod().getType().getName()+"."+f.getMethod().getName()).toList());
    }
    static void top(String name,Map<String,Double> values,double scale) {
        System.out.println("\n"+name);
        values.entrySet().stream().sorted(Map.Entry.<String,Double>comparingByValue().reversed()).limit(18).forEach(e->System.out.printf(Locale.ROOT,"%12.3f %s%n",e.getValue()/scale,e.getKey()));
    }
    public static void main(String[] args)throws Exception {
        var stallSum=new HashMap<String,Double>();var stallMax=new HashMap<String,Double>();var stallCount=new HashMap<String,Integer>();
        var stallStacks=new HashMap<String,Double>();var allocStacks=new HashMap<String,Double>();var otherAllocs=new HashMap<String,Double>();
        var gcCauses=new HashMap<String,Integer>();var gcPause=new ArrayList<Double>();var afterHeap=new ArrayList<Long>();
        var execution=new HashMap<String,Double>();var seen=new HashSet<Long>();
        int gc=0,stalls=0;double alloc=0,qioAlloc=0;
        try(var file=new RecordingFile(Path.of(args[0]))) {
            while(file.hasMoreEvents()) {
                var e=file.readEvent();String type=e.getEventType().getName();
                if(type.equals("jdk.ZAllocationStall")) {
                    stalls++;String t=e.getThread().getJavaName();double ms=e.getDuration().toNanos()/1e6;
                    stallSum.merge(t,ms,Double::sum);stallMax.merge(t,ms,Math::max);stallCount.merge(t,1,Integer::sum);
                    if(t.equals("Client thread")||t.equals("Server thread"))stallStacks.merge(t+" | "+stack(e),ms,Double::sum);
                } else if(type.equals("jdk.GarbageCollection")){gc++;gcCauses.merge(e.getString("cause"),1,Integer::sum);}
                else if(type.equals("jdk.GCPhasePause"))gcPause.add(e.getDuration().toNanos()/1e6);
                else if(type.equals("jdk.GCHeapSummary")&&e.getString("when").equals("After GC"))afterHeap.add(e.getLong("heapUsed"));
                else if(type.equals("jdk.ObjectAllocationSample")) {
                    if(seen.add(e.getThread().getJavaThreadId()))continue;
                    double weight=e.getLong("weight");String s=stack(e);alloc+=weight;allocStacks.merge(s,weight,Double::sum);
                    if(s.toLowerCase(Locale.ROOT).contains("qio"))qioAlloc+=weight;else otherAllocs.merge(s,weight,Double::sum);
                } else if(type.equals("jdk.ExecutionSample")) {
                    String t=e.getThread("sampledThread").getJavaName();
                    if(t.equals("Client thread")||t.equals("Server thread"))execution.merge(t+" | "+stack(e),1.,Double::sum);
                }
            }
        }
        System.out.println("ZAllocationStall count: "+stalls+"; completed GC events: "+gc+"; GC causes: "+gcCauses);
        stallSum.entrySet().stream().sorted(Map.Entry.<String,Double>comparingByValue().reversed()).limit(25).forEach(e->System.out.printf(Locale.ROOT,"STALL THREAD %s: count=%d sum=%.3f s max=%.3f ms%n",e.getKey(),stallCount.get(e.getKey()),e.getValue()/1000,stallMax.get(e.getKey())));
        System.out.printf(Locale.ROOT,"GC pauses: count=%d sum=%.3f ms max=%.3f ms%n",gcPause.size(),gcPause.stream().mapToDouble(Double::doubleValue).sum(),gcPause.stream().mapToDouble(Double::doubleValue).max().orElse(0));
        System.out.printf(Locale.ROOT,"After GC used heap: count=%d min=%.3f GiB max=%.3f GiB mean=%.3f GiB%n",afterHeap.size(),afterHeap.stream().mapToLong(Long::longValue).min().orElse(0)/1073741824.,afterHeap.stream().mapToLong(Long::longValue).max().orElse(0)/1073741824.,afterHeap.stream().mapToLong(Long::longValue).average().orElse(0)/1073741824.);
        System.out.printf(Locale.ROOT,"Allocation sample estimated cumulative bytes %.3f GiB; QIO present in %.2f%% (stack-based, not ownership)%n",alloc/1073741824.,100*qioAlloc/alloc);
        top("MAIN THREAD ALLOCATION STALL STACKS (seconds; allocation site may be a victim)",stallStacks,1000);
        top("ALL ALLOCATION STACKS (cumulative estimated GiB, not retained RAM)",allocStacks,1073741824.);
        top("ALLOCATION STACKS WITHOUT QIO (cumulative estimated GiB)",otherAllocs,1073741824.);
        top("EXECUTION STACKS (sample counts)",execution,1);
    }
}
