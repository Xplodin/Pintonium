package com.bean.beanutils;

import com.bean.beanutils.compat.bettercaves.FlooredCavernConfig;
import com.bean.beanutils.compat.hbmlootr.CompatConfig;
import com.bean.beanutils.compat.hbmlootr.HbmLootrHandler;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.config.Configuration;
import net.minecraftforge.fml.common.Loader;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.ModContainer;
import net.minecraftforge.fml.common.Mod.EventHandler;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import zone.rong.mixinbooter.ILateMixinLoader;

@Mod(
   modid = "beanutils",
   name = "BeanUtilities",
   version = "2.1.6",
   acceptableRemoteVersions = "*",
   dependencies = "required-after:mixinbooter@[10.7,);after:mcjtylib_ng;before:rftools;after:depthsupdate;after:bettercaves;after:taiga;after:cofhworld;after:hbm;after:lootr"
)
public class Beanutils implements ILateMixinLoader {
   public static final String MODID = "beanutils";
   public static final String NAME = "BeanUtilities";
   public static final String VERSION = "2.1.6";
   private CompatConfig hbmLootrConfig;

   @EventHandler
   public void preInit(FMLPreInitializationEvent event) {
      if (event.getSide().isClient()) {
         try {
            this.defineMcJtyAbstractWidget(event);
         } catch (IOException | LinkageError | ReflectiveOperationException var3) {
            event.getModLog().warn("Could not preload McJtyLib AbstractWidget", var3);
         }
      }

      if (Loader.isModLoaded("hbm")) {
         MinecraftForge.EVENT_BUS.register(new keyPatch());
      }

      if (Loader.isModLoaded("hbm") && Loader.isModLoaded("lootr")) {
         Configuration config = new Configuration(event.getSuggestedConfigurationFile());
         config.load();
         this.hbmLootrConfig = CompatConfig.load(config);
         if (config.hasChanged()) {
            config.save();
         }
      }
   }

   private void defineMcJtyAbstractWidget(FMLPreInitializationEvent event) throws ReflectiveOperationException, IOException {
      String className = "mcjty.lib.gui.widgets.AbstractWidget";
      ClassLoader loader = Beanutils.class.getClassLoader();

      try {
         Method isClassLoaded = loader.getClass().getMethod("isClassLoaded", String.class);
         if (!(Boolean)isClassLoaded.invoke(loader, "mcjty.lib.gui.widgets.AbstractWidget")) {
            byte[] bytes = this.readMcJtyClassBytes("mcjty.lib.gui.widgets.AbstractWidget");
            Method defineClass = loader.getClass().getMethod("defineClass", String.class, byte[].class);
            defineClass.invoke(loader, "mcjty.lib.gui.widgets.AbstractWidget", bytes);
         }
      } catch (NoSuchMethodException var7) {
         Class.forName("mcjty.lib.gui.widgets.AbstractWidget", false, loader);
      }

      event.getModLog().info("Defined McJtyLib AbstractWidget for Cleanroom compatibility");
   }

   private byte[] readMcJtyClassBytes(String className) throws IOException, ClassNotFoundException {
      ModContainer mcjty = (ModContainer)Loader.instance().getIndexedModList().get("mcjtylib_ng");
      if (mcjty == null) {
         throw new ClassNotFoundException("McJtyLib is not indexed");
      } else {
         String entryName = className.replace('.', '/') + ".class";
         File source = mcjty.getSource();
         if (source.isDirectory()) {
            File classFile = new File(source, entryName);
            InputStream input = new FileInputStream(classFile);

            byte[] var18;
            try {
               var18 = this.readFully(input);
            } catch (Throwable var13) {
               try {
                  input.close();
               } catch (Throwable var12) {
                  var13.addSuppressed(var12);
               }

               throw var13;
            }

            input.close();
            return var18;
         } else {
            JarFile jar = new JarFile(source);

            byte[] var8;
            try {
               JarEntry entry = jar.getJarEntry(entryName);
               if (entry == null) {
                  throw new ClassNotFoundException(className + " is absent from " + source);
               }

               InputStream input = jar.getInputStream(entry);

               try {
                  var8 = this.readFully(input);
               } catch (Throwable var14) {
                  if (input != null) {
                     try {
                        input.close();
                     } catch (Throwable var11) {
                        var14.addSuppressed(var11);
                     }
                  }

                  throw var14;
               }

               if (input != null) {
                  input.close();
               }
            } catch (Throwable var15) {
               try {
                  jar.close();
               } catch (Throwable var10) {
                  var15.addSuppressed(var10);
               }

               throw var15;
            }

            jar.close();
            return var8;
         }
      }
   }

   private byte[] readFully(InputStream input) throws IOException {
      ByteArrayOutputStream output = new ByteArrayOutputStream();
      byte[] buffer = new byte[8192];

      int read;
      while ((read = input.read(buffer)) >= 0) {
         output.write(buffer, 0, read);
      }

      return output.toByteArray();
   }

   @EventHandler
   public void init(FMLInitializationEvent event) {
      if (this.hbmLootrConfig != null) {
         MinecraftForge.EVENT_BUS.register(new HbmLootrHandler(this.hbmLootrConfig));
      }
   }

   public List<String> getMixinConfigs() {
      List<String> configs = new ArrayList<>();
      if (Loader.isModLoaded("cofhworld")) {
         configs.add("mixins.beanutils.cofh.json");
      }

      if (Loader.isModLoaded("taiga") && Loader.isModLoaded("depthsupdate")) {
         configs.add("mixins.beanutils.taiga.json");
      }

      if (Loader.isModLoaded("bettercaves") && Loader.isModLoaded("depthsupdate")) {
         FlooredCavernConfig.reload();
         configs.add("mixins.beanutils.bettercaves.json");
      }

      return configs;
   }
}
