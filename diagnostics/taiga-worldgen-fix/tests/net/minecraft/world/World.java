package net.minecraft.world;
import java.util.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
public final class World {
    public final Map<BlockPos,IBlockState> edits=new HashMap<>();
    public final List<String> writes=new ArrayList<>();
    public final List<Integer> flags=new ArrayList<>();
    public int reads;
    private final int seed, mode;
    public World(int seed,int mode) { this.seed=seed; this.mode=mode; }
    public IBlockState getBlockState(BlockPos p) {
        reads++;
        if(edits.containsKey(p))return edits.get(p);
        if(mode==1)return IBlockState.LAVA;
        if(mode==2)return IBlockState.AIR;
        int h=Math.floorMod(Objects.hash(p.x,p.y,p.z,seed),17);
        return h<2?IBlockState.LAVA:h<4?IBlockState.FLOWING_LAVA:h==4?IBlockState.BEDROCK:IBlockState.STONE;
    }
    public IBlockState func_180495_p(BlockPos p) { return getBlockState(p); }
    public boolean func_175656_a(BlockPos p,IBlockState s) { return setBlockState(p,s,3); }
    public boolean setBlockState(BlockPos p,IBlockState s,int f) {
        writes.add(p+"="+s); flags.add(f); edits.put(p,s); return true;
    }
}
