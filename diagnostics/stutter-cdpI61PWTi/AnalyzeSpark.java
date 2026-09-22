import com.cleanroommc.flare.proto.FlareSamplerProtos.*;
import java.nio.file.*;
import java.io.*;
import java.time.*;
import java.util.*;

class AnalyzeSpark {
    static PrintWriter rows;
    static List<StackTraceNode> pool;
    static double total;
    static Map<String,double[]> methods;
    static int nodes;
    static int window=-1, violations;
    static double maxExcess;
    static String csv(Object x) { return "\"" + String.valueOf(x).replace("\"", "\"\"").replace("\n", " ").replace("\r", " ") + "\""; }
    static double time(StackTraceNode node) { return window<0 ? node.getTimesList().stream().mapToDouble(Double::doubleValue).sum() : node.getTimes(window); }
    static List<StackTraceNode> children(StackTraceNode node) {
        return node.getChildrenRefsList().stream().map(pool::get).toList();
    }
    static void walk(String thread, StackTraceNode node, String parent, Set<String> ancestors, int depth) {
        if(depth>500) throw new IllegalStateException("Unexpected stack depth");
        double inc=time(node);
        var child=children(node);
        double childTime=child.stream().mapToDouble(AnalyzeSpark::time).sum();
        if(childTime>inc+0.01){violations++;maxExcess=Math.max(maxExcess,childTime-inc);}
        double self=Math.max(0,inc-childTime);
        String method=node.getClassName()+"."+node.getMethodName();
        String path=parent.isEmpty()?method:parent+" > "+method;
        rows.printf(Locale.ROOT,"%s,%s,%s,%.3f,%.3f,%s%n",csv(thread),csv(node.getClassName()),csv(node.getMethodName()),inc,self,csv(path));
        double[] metric=methods.computeIfAbsent(method,k->new double[2]);
        if(!ancestors.contains(method))metric[0]+=inc;
        metric[1]+=self;
        var next=new HashSet<>(ancestors);next.add(method);
        nodes++;
        for(var c:child)walk(thread,c,path,next,depth+1);
    }
    public static void main(String[] args) throws Exception {
        var data=SamplerData.parseFrom(Files.readAllBytes(Path.of(args[0])));
        if(args.length>2)window=Integer.parseInt(args[2]);
        var meta=data.getMetadata();
        var output=Path.of(args[1]);Files.createDirectories(output);
        System.out.println("Start: "+Instant.ofEpochMilli(meta.getStartTime()));
        System.out.println("End: "+Instant.ofEpochMilli(meta.getEndTime()));
        System.out.println("Duration ms: "+(meta.getEndTime()-meta.getStartTime()));
        System.out.println("Interval: "+meta.getInterval()+"; mode: "+meta.getSamplerMode());
        System.out.println("Thread selector: "+meta.getThreadDumper());
        System.out.println("Aggregator: "+meta.getDataAggregator());
        System.out.println("Platform: "+meta.getPlatformMetadata());
        System.out.println("Memory: "+meta.getPlatformStatistics().getMemory());
        System.out.println("TPS: "+meta.getPlatformStatistics().getTps());
        System.out.println("MSPT: "+meta.getPlatformStatistics().getMspt());
        System.out.println("GC: "+meta.getPlatformStatistics().getGcMap());
        System.out.println("CPU: "+meta.getSystemStatistics().getCpu());
        Files.writeString(output.resolve("windows.txt"),data.getTimeWindowStatisticsMap().toString());
        try(var writer=new PrintWriter(Files.newBufferedWriter(output.resolve("stacks.csv")))) {
            rows=writer;
            rows.println("thread,class,method,inclusive_ms,self_ms,path");
            for(var thread:data.getThreadsList()) {
                total=window<0 ? thread.getTimesList().stream().mapToDouble(Double::doubleValue).sum() : thread.getTimes(window);
                pool=thread.getChildrenList();methods=new HashMap<>();nodes=0;violations=0;maxExcess=0;
                var roots=thread.getChildrenRefsCount()>0 ? thread.getChildrenRefsList().stream().map(pool::get).toList() : pool;
                System.out.println("RAW thread times: "+thread.getTimesList()+"; windows: "+data.getTimeWindowsList());
                for(var r:roots){System.out.println("RAW root "+r.getClassName()+"."+r.getMethodName()+" "+r.getTimesList());for(var c:children(r)){System.out.println("RAW child "+c.getClassName()+"."+c.getMethodName()+" "+c.getTimesList());for(var d:children(c))System.out.println("RAW grandchild "+d.getClassName()+"."+d.getMethodName()+" "+d.getTimesList());}}
                for(var root:roots)walk(thread.getName(),root,"",Set.of(),0);
                System.out.printf(Locale.ROOT,"%nTHREAD %s: %.3f ms; %d nodes; %.3f ms roots%n",thread.getName(),total,nodes,roots.stream().mapToDouble(AnalyzeSpark::time).sum());
                System.out.println("Window index: "+window+"; parent/child time inconsistencies: "+violations+"; max excess ms: "+maxExcess);
                if(violations>0)System.out.println("WARNING: timing totals are internally inconsistent; do not use the following percentages as reliable CPU attribution.");
                System.out.println("TOP INCLUSIVE (overlapping parent/child frames):");
                methods.entrySet().stream().sorted((a,b)->Double.compare(b.getValue()[0],a.getValue()[0])).limit(32).forEach(e->System.out.printf(Locale.ROOT,"%7.2f%% %10.3f ms SELF %9.3f ms %s%n",100*e.getValue()[0]/total,e.getValue()[0],e.getValue()[1],e.getKey()));
                System.out.println("TOP SELF:");
                methods.entrySet().stream().sorted((a,b)->Double.compare(b.getValue()[1],a.getValue()[1])).limit(24).forEach(e->System.out.printf(Locale.ROOT,"%7.2f%% %10.3f ms %s%n",100*e.getValue()[1]/total,e.getValue()[1],e.getKey()));
            }
        }
    }
}
