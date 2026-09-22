package sayys.depthsupdate.util;

public record BlockSpec(String name, int meta) {
   public static final int NO_META = -1;

   public boolean hasMeta() {
      return this.meta != -1;
   }

   public static BlockSpec parse(String spec) {
      String trimmed = spec == null ? "" : spec.trim();
      int separator = trimmed.lastIndexOf(64);
      if (separator < 0 && trimmed.indexOf(58) != trimmed.lastIndexOf(58)) {
         separator = trimmed.lastIndexOf(58);
      }

      if (separator > 0 && separator != trimmed.length() - 1) {
         int meta;
         try {
            meta = Integer.parseInt(trimmed.substring(separator + 1));
         } catch (NumberFormatException var5) {
            return new BlockSpec(trimmed, -1);
         }

         return meta < 0 ? new BlockSpec(trimmed, -1) : new BlockSpec(trimmed.substring(0, separator), meta);
      } else {
         return new BlockSpec(trimmed, -1);
      }
   }
}
