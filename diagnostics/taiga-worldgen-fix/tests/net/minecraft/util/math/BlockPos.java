package net.minecraft.util.math;
public final class BlockPos {
    public final int x, y, z;
    public BlockPos(int x, int y, int z) { this.x=x; this.y=y; this.z=z; }
    public BlockPos up() { return new BlockPos(x,y+1,z); }
    public BlockPos down() { return new BlockPos(x,y-1,z); }
    public int getY() { return y; }
    public BlockPos func_177984_a() { return up(); }
    public BlockPos func_177977_b() { return down(); }
    public int func_177956_o() { return y; }
    public boolean equals(Object o) { return o instanceof BlockPos && ((BlockPos)o).x==x && ((BlockPos)o).y==y && ((BlockPos)o).z==z; }
    public int hashCode() { return (x*31+y)*31+z; }
    public String toString() { return x+","+y+","+z; }
}
