import com.cleanroommc.flare.proto.FlareProtos;
import com.cleanroommc.flare.proto.FlareSamplerProtos;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class SparkProfileAnalyzer {
    private static final String[] TERRAIN_TERMS = {
        "chunk", "terrain", "worldgen", "worldgenerator", "generate", "populate",
        "decorate", "biome", "cave", "ravine", "structure", "feature", "oregen",
        "noise", "carver", "cascad"
    };

    private static FlareSamplerProtos.SamplerData data;
    private static FlareSamplerProtos.SamplerMetadata metadata;
    private static int selectedWindow = -1;

    private static final class Cost {
        double self;
        double inclusive;
    }

    private static final class NodeCost {
        final int index;
        final double inclusive;
        final double self;

        NodeCost(int index, double inclusive, double self) {
            this.index = index;
            this.inclusive = inclusive;
            this.self = self;
        }
    }

    public static void main(String[] args) throws Exception {
        if (args.length < 1 || args.length > 2) {
            System.err.println("Usage: SparkProfileAnalyzer <profile.sparkprofile> [window-index]");
            System.exit(2);
        }

        Path profile = Paths.get(args[0]);
        try (InputStream input = Files.newInputStream(profile)) {
            data = FlareSamplerProtos.SamplerData.parseFrom(input);
        }
        metadata = data.getMetadata();
        selectedWindow = args.length == 2 ? Integer.parseInt(args[1]) : data.getTimeWindowsCount() - 1;
        if (selectedWindow < 0 || selectedWindow >= data.getTimeWindowsCount()) {
            throw new IllegalArgumentException("window-index out of range: " + selectedWindow);
        }

        printMetadata(profile);
        printSources();
        List<FlareSamplerProtos.ThreadNode> threads = new ArrayList<>(data.getThreadsList());
        threads.sort(Comparator.comparingDouble(SparkProfileAnalyzer::threadTime).reversed());
        printThreads(threads);

        for (FlareSamplerProtos.ThreadNode thread : threads) {
            String name = thread.getName().toLowerCase(Locale.ROOT);
            if (name.equals("server thread") || name.contains("server thread") || name.contains("chunk")) {
                analyzeThread(thread);
            }
        }
    }

    private static void printMetadata(Path profile) {
        System.out.println("PROFILE");
        System.out.println("file=" + profile.toAbsolutePath());
        System.out.println("size_bytes=" + safeSize(profile));
        System.out.println("mode=" + metadata.getSamplerMode());
        System.out.println("interval=" + metadata.getInterval());
        System.out.println("start=" + instant(metadata.getStartTime()));
        System.out.println("end=" + instant(metadata.getEndTime()));
        System.out.printf(Locale.ROOT, "duration_seconds=%.3f%n",
            (metadata.getEndTime() - metadata.getStartTime()) / 1000.0);
        System.out.println("ticks=" + metadata.getNumberOfTicks());
        System.out.println("time_windows=" + data.getTimeWindowsCount());
        if (data.getTimeWindowsCount() > 0) {
            System.out.println("analysis_window_index=" + selectedWindow
                + " key=" + data.getTimeWindows(selectedWindow)
                + (selectedWindow == data.getTimeWindowsCount() - 1
                    ? " (latest/clean export window)"
                    : " (older window; repeated live exports inflate some descendant frames)"));
        }

        if (metadata.hasThreadDumper()) {
            FlareSamplerProtos.SamplerMetadata.ThreadDumper d = metadata.getThreadDumper();
            System.out.println("thread_dumper=" + d.getType() + " patterns=" + d.getPatternsList());
        }
        if (metadata.hasDataAggregator()) {
            FlareSamplerProtos.SamplerMetadata.DataAggregator a = metadata.getDataAggregator();
            System.out.println("aggregator=" + a.getType() + " grouper=" + a.getThreadGrouper()
                + " tick_threshold=" + a.getTickLengthThreshold()
                + " included_ticks=" + a.getNumberOfIncludedTicks());
        }
        if (metadata.hasPlatformStatistics()) {
            FlareProtos.PlatformStatistics p = metadata.getPlatformStatistics();
            if (p.hasTps()) {
                System.out.printf(Locale.ROOT, "tps_1m=%.3f tps_5m=%.3f tps_15m=%.3f%n",
                    p.getTps().getLast1M(), p.getTps().getLast5M(), p.getTps().getLast15M());
            }
            if (p.hasMspt()) {
                if (p.getMspt().hasLast1M()) {
                    printAverage("mspt_1m", p.getMspt().getLast1M());
                }
                if (p.getMspt().hasLast5M()) {
                    printAverage("mspt_5m", p.getMspt().getLast5M());
                }
            }
        }
        if (metadata.hasSystemStatistics() && metadata.getSystemStatistics().hasCpu()) {
            FlareProtos.SystemStatistics.Cpu cpu = metadata.getSystemStatistics().getCpu();
            System.out.println("cpu=" + cpu.getModelName() + " logical_threads=" + cpu.getThreads());
            if (cpu.hasProcessUsage()) {
                System.out.printf(Locale.ROOT, "process_cpu_1m=%.3f process_cpu_15m=%.3f%n",
                    cpu.getProcessUsage().getLast1M(), cpu.getProcessUsage().getLast15M());
            }
            if (cpu.hasSystemUsage()) {
                System.out.printf(Locale.ROOT, "system_cpu_1m=%.3f system_cpu_15m=%.3f%n",
                    cpu.getSystemUsage().getLast1M(), cpu.getSystemUsage().getLast15M());
            }
        }
    }

    private static void printAverage(String label, FlareProtos.RollingAverageValues a) {
        System.out.printf(Locale.ROOT, "%s mean=%.3f median=%.3f p95=%.3f max=%.3f min=%.3f%n",
            label, a.getMean(), a.getMedian(), a.getPercentile95(), a.getMax(), a.getMin());
    }

    private static void printSources() {
        System.out.println();
        System.out.println("SOURCES count=" + metadata.getSourcesCount()
            + " class_map=" + data.getClassSourcesCount()
            + " method_map=" + data.getMethodSourcesCount()
            + " line_map=" + data.getLineSourcesCount());
        metadata.getSourcesMap().entrySet().stream()
            .sorted(Map.Entry.comparingByKey())
            .limit(12)
            .forEach(e -> System.out.println("source " + e.getKey() + " = "
                + e.getValue().getName() + " " + e.getValue().getVersion()));
        data.getClassSourcesMap().entrySet().stream()
            .limit(8)
            .forEach(e -> System.out.println("class_source_sample " + e.getKey() + " -> " + e.getValue()));
    }

    private static void printThreads(List<FlareSamplerProtos.ThreadNode> threads) {
        System.out.println();
        System.out.println("THREADS (latest-window execution time; compare percentages within each thread)");
        for (FlareSamplerProtos.ThreadNode thread : threads) {
            double root = rootTime(thread);
            System.out.printf(Locale.ROOT, "%12.3f  roots=%12.3f  nodes=%7d  %s%n",
                threadTime(thread), root, thread.getChildrenCount(), thread.getName());
        }
    }

    private static void analyzeThread(FlareSamplerProtos.ThreadNode thread) {
        List<FlareSamplerProtos.StackTraceNode> nodes = thread.getChildrenList();
        double denominator = threadTime(thread);
        if (denominator <= 0) {
            denominator = rootTime(thread);
        }
        final double totalTime = denominator;

        List<NodeCost> nodeCosts = new ArrayList<>(nodes.size());
        Map<String, Cost> byMethod = new HashMap<>();
        Map<String, Cost> bySource = new HashMap<>();
        int[] parent = new int[nodes.size()];
        java.util.Arrays.fill(parent, -1);

        for (int i = 0; i < nodes.size(); i++) {
            FlareSamplerProtos.StackTraceNode node = nodes.get(i);
            double inclusive = sum(node.getTimesList());
            double childTotal = 0;
            for (int ref : node.getChildrenRefsList()) {
                if (ref >= 0 && ref < nodes.size()) {
                    childTotal += sum(nodes.get(ref).getTimesList());
                    parent[ref] = i;
                }
            }
            double self = Math.max(0, inclusive - childTotal);
            nodeCosts.add(new NodeCost(i, inclusive, self));

            String method = methodName(node);
            Cost methodCost = byMethod.computeIfAbsent(method, ignored -> new Cost());
            methodCost.self += self;
            methodCost.inclusive += inclusive;

            String source = sourceName(node.getClassName());
            Cost sourceCost = bySource.computeIfAbsent(source, ignored -> new Cost());
            sourceCost.self += self;
            sourceCost.inclusive += inclusive;
        }
        for (int ref : thread.getChildrenRefsList()) {
            if (ref >= 0 && ref < parent.length) {
                parent[ref] = -1;
            }
        }

        System.out.println();
        System.out.println("============================================================");
        System.out.println("THREAD ANALYSIS: " + thread.getName());
        System.out.printf(Locale.ROOT, "denominator=%.3f nodes=%d roots=%d%n",
            denominator, nodes.size(), thread.getChildrenRefsCount());

        System.out.println();
        System.out.println("TOP SOURCES BY SELF TIME");
        printCosts(bySource, totalTime, 35);

        System.out.println();
        System.out.println("TOP METHODS BY SELF TIME");
        printCosts(byMethod, totalTime, 50);

        System.out.println();
        System.out.println("TOP TERRAIN-RELATED FRAMES BY INCLUSIVE TIME");
        nodeCosts.stream()
            .filter(c -> isTerrain(nodes.get(c.index)))
            .sorted(Comparator.comparingDouble((NodeCost c) -> c.inclusive).reversed())
            .limit(80)
            .forEach(c -> {
                FlareSamplerProtos.StackTraceNode n = nodes.get(c.index);
                System.out.printf(Locale.ROOT, "%7.3f%% self=%7.3f%%  %-30s  %s%n",
                    percent(c.inclusive, totalTime), percent(c.self, totalTime),
                    sourceName(n.getClassName()), methodName(n));
            });

        System.out.println();
        System.out.println("EXPENSIVE SELF FRAMES WITH CALL PATHS");
        Set<Integer> shown = new HashSet<>();
        nodeCosts.stream()
            .filter(c -> c.self > 0)
            .sorted(Comparator.comparingDouble((NodeCost c) -> c.self).reversed())
            .limit(30)
            .forEach(c -> {
                if (!shown.add(c.index)) {
                    return;
                }
                FlareSamplerProtos.StackTraceNode n = nodes.get(c.index);
                System.out.printf(Locale.ROOT, "%nSELF %7.3f%% INCL %7.3f%%  %s  [%s]%n",
                    percent(c.self, totalTime), percent(c.inclusive, totalTime),
                    methodName(n), sourceName(n.getClassName()));
                System.out.println("  " + callPath(nodes, parent, c.index));
            });

        System.out.println();
        System.out.println("ROOT CALL TREE (branches >= 2% of thread)");
        List<Integer> roots = new ArrayList<>(thread.getChildrenRefsList());
        roots.sort(Comparator.comparingDouble((Integer i) -> sum(nodes.get(i).getTimesList())).reversed());
        for (int root : roots) {
            printTree(nodes, root, denominator, 0, 2.0, 24);
        }
    }

    private static void printCosts(Map<String, Cost> costs, double denominator, int limit) {
        costs.entrySet().stream()
            .sorted(Comparator.comparingDouble((Map.Entry<String, Cost> e) -> e.getValue().self).reversed())
            .limit(limit)
            .forEach(e -> System.out.printf(Locale.ROOT, "%8.3f%% self  %8.3f%% incl  %s%n",
                percent(e.getValue().self, denominator),
                percent(e.getValue().inclusive, denominator), e.getKey()));
    }

    private static void printTree(List<FlareSamplerProtos.StackTraceNode> nodes, int index,
                                  double denominator, int depth, double thresholdPercent, int maxDepth) {
        if (index < 0 || index >= nodes.size() || depth > maxDepth) {
            return;
        }
        FlareSamplerProtos.StackTraceNode node = nodes.get(index);
        double inclusive = sum(node.getTimesList());
        double pct = percent(inclusive, denominator);
        if (pct < thresholdPercent) {
            return;
        }
        System.out.printf(Locale.ROOT, "%s%7.3f%%  %-24s %s%n",
            "  ".repeat(depth), pct, sourceName(node.getClassName()), methodName(node));
        List<Integer> children = new ArrayList<>(node.getChildrenRefsList());
        children.sort(Comparator.comparingDouble((Integer i) -> sum(nodes.get(i).getTimesList())).reversed());
        for (int child : children) {
            printTree(nodes, child, denominator, depth + 1, thresholdPercent, maxDepth);
        }
    }

    private static String callPath(List<FlareSamplerProtos.StackTraceNode> nodes, int[] parent, int index) {
        List<String> path = new ArrayList<>();
        int current = index;
        int guard = 0;
        while (current >= 0 && current < nodes.size() && guard++ < 128) {
            FlareSamplerProtos.StackTraceNode node = nodes.get(current);
            path.add(shortName(node.getClassName()) + "." + node.getMethodName());
            current = parent[current];
        }
        java.util.Collections.reverse(path);
        return String.join(" -> ", path);
    }

    private static boolean isTerrain(FlareSamplerProtos.StackTraceNode node) {
        String text = (node.getClassName() + "." + node.getMethodName()).toLowerCase(Locale.ROOT);
        for (String term : TERRAIN_TERMS) {
            if (text.contains(term)) {
                return true;
            }
        }
        return false;
    }

    private static String methodName(FlareSamplerProtos.StackTraceNode node) {
        return node.getClassName() + "." + node.getMethodName() + ":" + node.getLineNumber();
    }

    private static String shortName(String className) {
        int dot = className.lastIndexOf('.');
        return dot >= 0 ? className.substring(dot + 1) : className;
    }

    private static String sourceName(String className) {
        String key = data.getClassSourcesOrDefault(className, "");
        if (key.isEmpty()) {
            return "unknown";
        }
        FlareSamplerProtos.SamplerMetadata.SourceMetadata source = metadata.getSourcesMap().get(key);
        if (source == null) {
            return key;
        }
        String version = source.getVersion();
        return version.isEmpty() ? source.getName() : source.getName() + "@" + version;
    }

    private static double threadTime(FlareSamplerProtos.ThreadNode thread) {
        return sum(thread.getTimesList());
    }

    private static double rootTime(FlareSamplerProtos.ThreadNode thread) {
        double total = 0;
        for (int ref : thread.getChildrenRefsList()) {
            if (ref >= 0 && ref < thread.getChildrenCount()) {
                total += sum(thread.getChildren(ref).getTimesList());
            }
        }
        return total;
    }

    private static double sum(List<Double> values) {
        return selectedWindow < 0 || selectedWindow >= values.size() ? 0 : values.get(selectedWindow);
    }

    private static double percent(double part, double whole) {
        return whole <= 0 ? 0 : part * 100.0 / whole;
    }

    private static String instant(long epochMillis) {
        return epochMillis <= 0 ? Long.toString(epochMillis) : Instant.ofEpochMilli(epochMillis).toString();
    }

    private static long safeSize(Path path) {
        try {
            return Files.size(path);
        } catch (Exception ignored) {
            return -1;
        }
    }
}
