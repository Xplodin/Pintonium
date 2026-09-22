package com.kc.bcducompat;

import java.util.Collections;
import java.util.List;
import zone.rong.mixinbooter.ILateMixinLoader;

public final class BCDUCompatLateMixinLoader implements ILateMixinLoader {
   public List<String> getMixinConfigs() {
      return Collections.singletonList("mixins.bcducompat.json");
   }
}
