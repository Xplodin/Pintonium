import com.cleanroommc.flare.proto.FlareSamplerProtos;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

public final class ProfileProbe {
    public static void main(String[] args) throws Exception {
        FlareSamplerProtos.SamplerData data;
        try (InputStream in = Files.newInputStream(Paths.get(args[0]))) {
            data = FlareSamplerProtos.SamplerData.parseFrom(in);
        }
        System.out.println("windows=" + data.getTimeWindowsList());
        for (FlareSamplerProtos.ThreadNode thread : data.getThreadsList()) {
            System.out.println("thread=" + thread.getName() + " times=" + thread.getTimesList()
                + " refs=" + thread.getChildrenRefsList());
            List<FlareSamplerProtos.StackTraceNode> nodes = thread.getChildrenList();
            for (int root : thread.getChildrenRefsList()) {
                dump(nodes, root, 0);
            }
        }
    }

    private static void dump(List<FlareSamplerProtos.StackTraceNode> nodes, int index, int depth) {
        if (depth > 12) return;
        FlareSamplerProtos.StackTraceNode node = nodes.get(index);
        System.out.println("  ".repeat(depth) + "idx=" + index + " " + node.getClassName() + "."
            + node.getMethodName() + " times=" + node.getTimesList() + " refs=" + node.getChildrenRefsList());
        if (!node.getChildrenRefsList().isEmpty()) {
            dump(nodes, node.getChildrenRefs(0), depth + 1);
        }
    }
}
