import java.io.*;
import java.lang.reflect.*;
import java.nio.file.*;
import java.util.*;
import java.util.jar.*;
import org.objectweb.asm.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.world.World;
import com.bean.beanutils.mixin.MixinTaigaDescendingGeneration;

public final class TaigaDescendingRegression {
    public static void main(String[] args) throws Exception {
        // Execute the installed mod's exact original method, without its unrelated classes.
        byte[] original;
        try(JarFile jar=new JarFile(args[0]);InputStream in=jar.getInputStream(jar.getJarEntry("com/sosnitzka/taiga/util/Generator.class"))) { original=in.readAllBytes(); }
        ClassWriter writer=new ClassWriter(0);
        writer.visit(Opcodes.V1_8,Opcodes.ACC_PUBLIC,"OriginalDescending",null,"java/lang/Object",null);
        final int[] found={0};
        new ClassReader(original).accept(new ClassVisitor(Opcodes.ASM9) {
            public MethodVisitor visitMethod(int access,String name,String descriptor,String signature,String[] exceptions) {
                if(!name.equals("generateOreDescending"))return null;
                found[0]++;return writer.visitMethod(access,name,descriptor,signature,exceptions);
            }
        },0);
        writer.visitEnd();
        if(found[0]!=1)throw new AssertionError("Unexpected TAIGA method count: "+found[0]);
        Class<?> originalClass=new ClassLoader(TaigaDescendingRegression.class.getClassLoader()) {
            Class<?> define() { byte[] b=writer.toByteArray();return defineClass("OriginalDescending",b,0,b.length); }
        }.define();
        Method baseline=originalClass.getDeclaredMethod("generateOreDescending",List.class,IBlockState.class,Random.class,int.class,int.class,World.class,int.class,int.class,int.class);
        int comparisons=0,placements=0;
        int[][] ranges={{0,64,12},{-128,-96,6},{-128,-118,3},{-64,64,1},{0,0,2},{-128,-128,2},{-128,96,0}};
        for(int seed=0;seed<200;seed++)for(int mode=0;mode<3;mode++)for(int[] range:ranges)for(boolean bedrock:new boolean[]{false,true}) {
            List<IBlockState> hosts=bedrock?List.of(IBlockState.BEDROCK):List.of(IBlockState.LAVA,IBlockState.FLOWING_LAVA);
            Random a=new Random(seed),b=new Random(seed); World before=new World(seed,mode),after=new World(seed,mode);
            int x=(seed%5-2)*16,z=(seed%7-3)*16;
            baseline.invoke(null,hosts,IBlockState.ORE,a,x,z,before,range[2],range[0],range[1]);
            MixinTaigaDescendingGeneration.generateOreDescending(hosts,IBlockState.ORE,b,x,z,after,range[2],range[0],range[1]);
            if(!before.writes.equals(after.writes)||!before.edits.equals(after.edits)||before.reads!=after.reads||a.nextLong()!=b.nextLong())throw new AssertionError("Placement/search/RNG mismatch at "+seed+" "+Arrays.toString(range));
            if(before.flags.stream().anyMatch(f->f!=3)||after.flags.stream().anyMatch(f->f!=18))throw new AssertionError("Unexpected placement flags");
            comparisons++;placements+=after.writes.size();
        }
        System.out.println("PASS: "+comparisons+" cases; "+placements+" placements; identical writes, reads and RNG state; flags 3 -> 18 only.");
        System.out.println("Scope: simulated block storage; real neighbor/fluid effects and runtime Mixin application require in-game validation.");
    }
}
