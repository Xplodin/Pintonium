package sayys.depthsupdate.api;

public interface HeightInfo {
   int minY();

   int maxY();

   int totalHeight();

   int seaLevel();

   int lavaLevel();

   int voidDamageLevel();

   boolean isExtended();

   boolean isInBounds(int var1);
}
