import java.nio.file.*;
import java.util.*;
import jdk.jfr.consumer.*;

class CheckResourcePathSamples {
    public static void main(String[] args) throws Exception {
        for (String filename : args) {
            long samples = 0, path = 0, format = 0, builder = 0;
            Set<Long> threads = new HashSet<>();
            try (var file = new RecordingFile(Path.of(filename))) {
                while (file.hasMoreEvents()) {
                    var event = file.readEvent();
                    if (!event.getEventType().getName().equals("jdk.ObjectAllocationSample")) continue;
                    if (threads.add(event.getThread().getJavaThreadId())) continue;
                    samples++;
                    var trace = event.getStackTrace();
                    if (trace == null) continue;
                    boolean hasPath = false, hasFormat = false, hasBuilder = false;
                    for (var frame : trace.getFrames()) {
                        String name = frame.getMethod().getType().getName() + "." + frame.getMethod().getName();
                        hasPath |= name.equals("net.minecraft.client.resources.AbstractResourcePack.func_110592_c");
                        hasFormat |= name.equals("java.lang.String.format");
                        hasBuilder |= name.startsWith("java.lang.StringBuilder.") || name.startsWith("java.lang.AbstractStringBuilder.");
                    }
                    if (hasPath) { path++; if (hasFormat) format++; if (hasBuilder) builder++; }
                }
            }
            System.out.printf("%s%nTotal samples: %d%nResource-path samples: %d%nResource path with String.format: %d%nResource path with string builder: %d%n%n", filename, samples, path, format, builder);
        }
    }
}
