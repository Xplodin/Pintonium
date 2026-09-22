package dev.beanpack.nullsafe;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

public final class TransformerSelfTest {
    private static final String TARGET_DESCRIPTOR =
            "(Ljava/lang/StackTraceElement;Ljava/lang/StackTraceElement;)Z";

    private TransformerSelfTest() {
    }

    public static void main(String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("Expected the Minecraft 1.12.2 JAR path");
        }

        byte[] original;
        try (ZipFile zip = new ZipFile(args[0])) {
            ZipEntry entry = zip.getEntry("c.class");
            if (entry == null) {
                throw new IllegalStateException("Could not find obfuscated CrashReportCategory (c.class)");
            }
            try (InputStream input = zip.getInputStream(entry)) {
                original = readFully(input);
            }
        }

        byte[] transformed = new NullSafeCrashTransformer().transform(
                "c", "net.minecraft.crash.CrashReportCategory", original);

        AtomicInteger unsafeCalls = new AtomicInteger();
        AtomicInteger safeCalls = new AtomicInteger();
        new ClassReader(transformed).accept(new ClassVisitor(Opcodes.ASM5) {
            @Override
            public MethodVisitor visitMethod(int access, String name, String descriptor,
                    String signature, String[] exceptions) {
                if (!TARGET_DESCRIPTOR.equals(descriptor)) {
                    return null;
                }
                return new MethodVisitor(Opcodes.ASM5) {
                    @Override
                    public void visitMethodInsn(int opcode, String owner, String invokedName,
                            String invokedDescriptor, boolean isInterface) {
                        if (opcode == Opcodes.INVOKEVIRTUAL
                                && "java/lang/String".equals(owner)
                                && "equals".equals(invokedName)) {
                            unsafeCalls.incrementAndGet();
                        }
                        if (opcode == Opcodes.INVOKESTATIC
                                && "java/util/Objects".equals(owner)
                                && "equals".equals(invokedName)
                                && "(Ljava/lang/Object;Ljava/lang/Object;)Z".equals(invokedDescriptor)) {
                            safeCalls.incrementAndGet();
                        }
                    }
                };
            }
        }, 0);

        if (unsafeCalls.get() != 0 || safeCalls.get() != 3) {
            throw new AssertionError(
                    "Unexpected transformed bytecode: unsafe=" + unsafeCalls.get()
                            + ", safe=" + safeCalls.get());
        }

        System.out.println("PASS: replaced all 3 String.equals calls with Objects.equals calls.");
    }

    private static byte[] readFully(InputStream input) throws Exception {
        ByteArrayOutputStream output = new ByteArrayOutputStream();
        byte[] buffer = new byte[8192];
        int read;
        while ((read = input.read(buffer)) >= 0) {
            output.write(buffer, 0, read);
        }
        return output.toByteArray();
    }
}
