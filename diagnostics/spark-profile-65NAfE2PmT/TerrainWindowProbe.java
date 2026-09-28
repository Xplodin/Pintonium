import com.cleanroommc.flare.proto.FlareSamplerProtos;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public final class TerrainWindowProbe {
    private static final String[] TERMS = {
        "chunkproviderserver.func_186025_d", "chunkproviderserver.loadchunk",
        "chunkioexecutor.syncchunkload", "gameregistry.generateworld",
        "transformerhooks.othermodgenerate", "chunkgenerator", "worldgen",
        ".populate", ".decorate", "cavenoise", "cavegenerator", "ravine",
        "structuregenerator", "oregenerator", "oregen", "generatemeteor", "queuelightcheck"
    };

    private static final class Hit {
        final int index;
        final double value;
        Hit(int index, double value) { this.index = index; this.value = value; }
    }

    public static void main(String[] args) throws Exception {
        FlareSamplerProtos.SamplerData data;
        try (InputStream in = Files.newInputStream(Paths.get(args[0]))) {
            data = FlareSamplerProtos.SamplerData.parseFrom(in);
        }
        FlareSamplerProtos.ThreadNode thread = data.getThreadsList().stream()
            .filter(t -> t.getName().toLowerCase(Locale.ROOT).contains("server thread"))
            .findFirst().orElseThrow();
        List<FlareSamplerProtos.StackTraceNode> nodes = thread.getChildrenList();
        int[] parents = new int[nodes.size()];
        java.util.Arrays.fill(parents, -1);
        for (int i = 0; i < nodes.size(); i++) {
            for (int child : nodes.get(i).getChildrenRefsList()) {
                if (child >= 0 && child < parents.length) parents[child] = i;
            }
            if (nodes.get(i).getMethodName().toLowerCase(Locale.ROOT).contains("generatemeteor")) {
                FlareSamplerProtos.StackTraceNode n = nodes.get(i);
                System.out.println("METEOR_FRAME " + n.getClassName() + "." + n.getMethodName()
                    + " source=" + data.getClassSourcesOrDefault(n.getClassName(), "unknown")
                    + " times=" + n.getTimesList());
            }
        }

        for (int window = data.getTimeWindowsCount() - 1; window >= 0; window--) {
            double total = value(thread.getTimesList(), window);
            List<Hit> hits = new ArrayList<>();
            for (int i = 0; i < nodes.size(); i++) {
                FlareSamplerProtos.StackTraceNode node = nodes.get(i);
                if (interesting(node)) {
                    double v = value(node.getTimesList(), window);
                    if (v > 0) hits.add(new Hit(i, v));
                }
            }
            hits.sort(Comparator.comparingDouble((Hit h) -> h.value).reversed());
            System.out.printf(Locale.ROOT, "%nWINDOW %d key=%d server=%.0f (older windows have export inflation)%n",
                window, data.getTimeWindows(window), total);
            int printed = 0;
            for (Hit hit : hits) {
                FlareSamplerProtos.StackTraceNode node = nodes.get(hit.index);
                String source = data.getClassSourcesOrDefault(node.getClassName(), "unknown");
                System.out.printf(Locale.ROOT, "%9.1f%%  %-30s  %s.%s:%d%n",
                    total == 0 ? 0 : hit.value * 100.0 / total,
                    source, node.getClassName(), node.getMethodName(), node.getLineNumber());
                System.out.println("             " + path(nodes, parents, hit.index));
                if (++printed == 18) break;
            }
        }
    }

    private static boolean interesting(FlareSamplerProtos.StackTraceNode node) {
        String s = (node.getClassName() + "." + node.getMethodName()).toLowerCase(Locale.ROOT);
        for (String term : TERMS) if (s.contains(term)) return true;
        return false;
    }

    private static double value(List<Double> values, int i) {
        return i < values.size() ? values.get(i) : 0;
    }

    private static String path(List<FlareSamplerProtos.StackTraceNode> nodes, int[] parents, int index) {
        List<String> parts = new ArrayList<>();
        int current = index;
        for (int guard = 0; current >= 0 && current < nodes.size() && guard < 80; guard++) {
            FlareSamplerProtos.StackTraceNode n = nodes.get(current);
            String cls = n.getClassName();
            int dot = cls.lastIndexOf('.');
            parts.add((dot < 0 ? cls : cls.substring(dot + 1)) + "." + n.getMethodName());
            current = parents[current];
        }
        java.util.Collections.reverse(parts);
        return String.join(" -> ", parts);
    }
}
