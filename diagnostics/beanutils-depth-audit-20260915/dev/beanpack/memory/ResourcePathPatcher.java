package dev.beanpack.memory;

import java.util.ArrayList;
import java.util.List;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.IntInsnNode;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

public final class ResourcePathPatcher implements Opcodes {
   private ResourcePathPatcher() {
   }

   public static byte[] patch(byte[] original) {
      ClassNode node = new ClassNode(589824);
      new ClassReader(original).accept(node, 0);
      int changes = 0;

      for (MethodNode method : node.methods) {
         Type[] arguments = Type.getArgumentTypes(method.desc);
         if ((method.access & 8) != 0
            && arguments.length == 1
            && arguments[0].getSort() == 10
            && Type.getReturnType(method.desc).equals(Type.getType(String.class))
            && method.tryCatchBlocks.isEmpty()) {
            String resourceOwner = arguments[0].getInternalName();
            List<MethodInsnNode> getters = recognize(method, resourceOwner);
            if (getters != null) {
               InsnList code = new InsnList();
               code.add(new TypeInsnNode(187, "java/lang/StringBuilder"));
               code.add(new InsnNode(89));
               code.add(new LdcInsnNode("assets/"));
               code.add(new MethodInsnNode(183, "java/lang/StringBuilder", "<init>", "(Ljava/lang/String;)V", false));
               appendGetter(code, getters.get(0));
               code.add(new IntInsnNode(16, 47));
               code.add(new MethodInsnNode(182, "java/lang/StringBuilder", "append", "(C)Ljava/lang/StringBuilder;", false));
               appendGetter(code, getters.get(1));
               code.add(new MethodInsnNode(182, "java/lang/StringBuilder", "toString", "()Ljava/lang/String;", false));
               code.add(new InsnNode(176));
               method.instructions = code;
               method.localVariables = null;
               method.visibleLocalVariableAnnotations = null;
               method.invisibleLocalVariableAnnotations = null;
               method.maxStack = 3;
               method.maxLocals = 1;
               changes++;
            }
         }
      }

      if (changes != 1) {
         return original;
      } else {
         ClassWriter writer = new ClassWriter(0);
         node.accept(writer);
         return writer.toByteArray();
      }
   }

   private static List<MethodInsnNode> recognize(MethodNode method, String owner) {
      List<AbstractInsnNode> code = new ArrayList<>();

      for (AbstractInsnNode insn : method.instructions) {
         if (insn.getOpcode() >= 0) {
            code.add(insn);
         }
      }

      int[] expected = new int[]{18, 6, 189, 89, 3, 18, 83, 89, 4, 25, 182, 83, 89, 5, 25, 182, 83, 184, 176};
      if (code.size() != expected.length) {
         return null;
      } else {
         for (int i = 0; i < expected.length; i++) {
            if (code.get(i).getOpcode() != expected[i]) {
               return null;
            }
         }

         if ("%s/%s/%s".equals(((LdcInsnNode)code.get(0)).cst)
            && "java/lang/Object".equals(((TypeInsnNode)code.get(2)).desc)
            && "assets".equals(((LdcInsnNode)code.get(5)).cst)
            && ((VarInsnNode)code.get(9)).var == 0
            && ((VarInsnNode)code.get(14)).var == 0) {
            List<MethodInsnNode> getters = new ArrayList<>();

            for (int index : new int[]{10, 15}) {
               MethodInsnNode getter = (MethodInsnNode)code.get(index);
               if (!owner.equals(getter.owner) || !"()Ljava/lang/String;".equals(getter.desc) || getter.itf) {
                  return null;
               }

               getters.add(getter);
            }

            MethodInsnNode format = (MethodInsnNode)code.get(17);
            return "java/lang/String".equals(format.owner)
                  && "format".equals(format.name)
                  && !format.itf
                  && "(Ljava/lang/String;[Ljava/lang/Object;)Ljava/lang/String;".equals(format.desc)
               ? getters
               : null;
         } else {
            return null;
         }
      }
   }

   private static void appendGetter(InsnList code, MethodInsnNode getter) {
      code.add(new VarInsnNode(25, 0));
      code.add(new MethodInsnNode(getter.getOpcode(), getter.owner, getter.name, getter.desc, getter.itf));
      code.add(new MethodInsnNode(182, "java/lang/StringBuilder", "append", "(Ljava/lang/String;)Ljava/lang/StringBuilder;", false));
   }
}
