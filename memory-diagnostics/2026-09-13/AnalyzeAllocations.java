import java.nio.file.*;
import java.util.*;
import java.util.stream.*;
import jdk.jfr.consumer.*;

class AnalyzeAllocations {
    public static void main(String[] args) throws Exception {
        Map<String, Long> stacks = new HashMap<>();
        Map<String, Long> owners = new HashMap<>();
        Map<String, Long> counts = new HashMap<>();
        Map<String, Long> largest = new HashMap<>();
        Map<String, Long> groups = new LinkedHashMap<>();
        Map<String, Long> signatures = new LinkedHashMap<>();
        signatures.put("CachedResourcePath", 0L);
        signatures.put("fastHasResource", 0L);
        signatures.put("NonBlockingIdentityHashMap.initialize", 0L);
        signatures.put("java.lang.String.format", 0L);
        Set<Long> seenThreads = new HashSet<>();
        long total = 0, count = 0;
        try (var recording = new RecordingFile(Path.of(args[0]))) {
            while (recording.hasMoreEvents()) {
                var event = recording.readEvent();
                if (!event.getEventType().getName().equals("jdk.ObjectAllocationSample")) continue;
                long weight = event.getLong("weight");
                // Starting JFR mid-run can misattribute earlier thread allocations to its first sample.
                if (args.length > 1 && args[1].equals("skip-first") && seenThreads.add(event.getThread().getJavaThreadId())) continue;
                total += weight; count++;
                var trace = event.getStackTrace();
                if (trace == null) continue;
                var frames = trace.getFrames().stream().map(f -> f.getMethod().getType().getName() + "." + f.getMethod().getName() + ":" + f.getLineNumber()).toList();
                String all = String.join("\n", frames);
                for (String signature : signatures.keySet()) {
                    if (all.contains(signature)) signatures.put(signature, signatures.get(signature) + 1);
                }
                String group = all.contains("ActionReplaceAllOccurences") ? "CraftTweaker replaceAllOccurences"
                    : all.contains("registerRecipeOredictMelting") ? "Tinkers ore-dictionary melting registration"
                    : all.contains("MutableResourcePackBindings") || all.contains("stellar_core$attachOnMiss") ? "StellarCore resource discovery and binding"
                    : all.contains("StateMapperBase.handler") && all.contains("NonBlockingIdentityHashMap.initialize") ? "StellarCore preallocated state-mapper maps"
                    : all.contains("vintagefix") && (all.contains("texture") || all.contains("model") || all.contains("resource")) ? "Other VintageFix model/texture/resource paths"
                    : "Other allocations";
                groups.merge(group, weight, Long::sum);
                var key = event.getClass("objectClass").getName() + "\n  " + String.join("\n  ", frames.subList(0, Math.min(14, frames.size())));
                stacks.merge(key, weight, Long::sum);
                counts.merge(key, 1L, Long::sum);
                largest.merge(key, weight, Math::max);
                String owner = frames.stream().filter(f -> !f.startsWith("java.") && !f.startsWith("jdk.") && !f.startsWith("sun.") && !f.startsWith("com.sun.")).findFirst().orElse("JDK only");
                owners.merge(owner, weight, Long::sum);
            }
        }
        System.out.printf("Samples: %d; cumulative estimated allocations: %.2f GiB (not retained RAM)%n", count, total / 1073741824.0);
        final double sum = total;
        System.out.println("\nStack signatures (number of samples, not object counts):");
        signatures.forEach((key, value) -> System.out.printf("%8d %s%n", value, key));
        System.out.println("\nDisjoint stack categories (full sampled stack):");
        groups.entrySet().stream().sorted(Map.Entry.<String,Long>comparingByValue().reversed()).forEach(e -> System.out.printf("%6.2f%% %8.2f GiB cumulative: %s%n", 100 * e.getValue() / sum, e.getValue() / 1073741824.0, e.getKey()));
        System.out.println("\nFirst non-JDK frame:");
        owners.entrySet().stream().sorted(Map.Entry.<String,Long>comparingByValue().reversed()).limit(18).forEach(e -> System.out.printf("%6.2f%% %s%n", 100 * e.getValue() / sum, e.getKey()));
        System.out.println("\nAllocation stack groups:");
        stacks.entrySet().stream().sorted(Map.Entry.<String,Long>comparingByValue().reversed()).limit(14).forEach(e -> System.out.printf("\n%6.2f%% (%.2f GiB cumulative, %d samples, largest weight %.1f MiB) %s%n", 100 * e.getValue() / sum, e.getValue() / 1073741824.0, counts.get(e.getKey()), largest.get(e.getKey()) / 1048576.0, e.getKey()));
    }
}
