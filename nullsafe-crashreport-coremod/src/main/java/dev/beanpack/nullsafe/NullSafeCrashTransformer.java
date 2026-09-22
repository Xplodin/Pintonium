package dev.beanpack.nullsafe;

import java.util.concurrent.atomic.AtomicInteger;

import net.minecraft.launchwrapper.IClassTransformer;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

/**
 * Makes CrashReportCategory's stack-frame comparison tolerate a null
 * StackTraceElement#getFileName() result.
 */
public final class NullSafeCrashTransformer implements IClassTransformer {
    private static final String TARGET_CLASS = "net.minecraft.crash.CrashReportCategory";
    private static final String TARGET_DESCRIPTOR =
            "(Ljava/lang/StackTraceElement;Ljava/lang/StackTraceElement;)Z";

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (basicClass == null || !isTargetClass(name, transformedName)) {
            return basicClass;
        }

        final AtomicInteger replacements = new AtomicInteger();
        final ClassReader reader = new ClassReader(basicClass);
        final ClassWriter writer = new ClassWriter(reader, 0);
        final ClassVisitor visitor = new ClassVisitor(Opcodes.ASM5, writer) {
            @Override
            public MethodVisitor visitMethod(int access, String methodName, String descriptor,
                    String signature, String[] exceptions) {
                MethodVisitor delegate = super.visitMethod(
                        access, methodName, descriptor, signature, exceptions);

                if (!TARGET_DESCRIPTOR.equals(descriptor)) {
                    return delegate;
                }

                return new MethodVisitor(Opcodes.ASM5, delegate) {
                    @Override
                    public void visitMethodInsn(int opcode, String owner, String invokedName,
                            String invokedDescriptor, boolean isInterface) {
                        if (opcode == Opcodes.INVOKEVIRTUAL
                                && "java/lang/String".equals(owner)
                                && "equals".equals(invokedName)
                                && "(Ljava/lang/Object;)Z".equals(invokedDescriptor)) {
                            // The operand stack already contains receiver + argument, which is
                            // exactly the argument layout expected by Objects.equals(Object,Object).
                            super.visitMethodInsn(
                                    Opcodes.INVOKESTATIC,
                                    "java/util/Objects",
                                    "equals",
                                    "(Ljava/lang/Object;Ljava/lang/Object;)Z",
                                    false);
                            replacements.incrementAndGet();
                            return;
                        }

                        super.visitMethodInsn(
                                opcode, owner, invokedName, invokedDescriptor, isInterface);
                    }
                };
            }
        };

        reader.accept(visitor, 0);

        if (replacements.get() == 3) {
            System.out.println(
                    "[BeanPack NullSafeCrash] Patched CrashReportCategory with 3 null-safe comparisons.");
            return writer.toByteArray();
        }

        System.err.println(
                "[BeanPack NullSafeCrash] Expected 3 String.equals calls but found "
                        + replacements.get() + "; leaving the class unchanged.");
        return basicClass;
    }

    private static boolean isTargetClass(String name, String transformedName) {
        return TARGET_CLASS.equals(name)
                || TARGET_CLASS.equals(transformedName)
                || "c".equals(name);
    }
}
