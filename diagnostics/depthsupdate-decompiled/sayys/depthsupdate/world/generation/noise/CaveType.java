package sayys.depthsupdate.world.generation.noise;

public enum CaveType {
   CHEESE("cheese"),
   SPAGHETTI("spaghetti"),
   NOODLE("noodle"),
   ENTRANCE("entrance");

   private final String label;

   private CaveType(String label) {
      this.label = label;
   }

   public String label() {
      return this.label;
   }
}
