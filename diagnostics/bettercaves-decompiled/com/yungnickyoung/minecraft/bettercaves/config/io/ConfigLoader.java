package com.yungnickyoung.minecraft.bettercaves.config.io;

import com.yungnickyoung.minecraft.bettercaves.BetterCaves;
import com.yungnickyoung.minecraft.bettercaves.config.util.ConfigHolder;
import com.yungnickyoung.minecraft.bettercaves.enums.RegionSize;
import com.yungnickyoung.minecraft.bettercaves.noise.FastNoise;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import net.minecraftforge.common.config.ConfigCategory;
import net.minecraftforge.common.config.Property;
import net.minecraftforge.common.config.Configuration.UnicodeInputStreamReader;
import net.minecraftforge.common.config.Property.Type;
import org.apache.commons.io.IOUtils;

public class ConfigLoader {
   private static final String ALLOWED_CHARS = "._-";
   private static final String DEFAULT_ENCODING = "UTF-8";

   public static ConfigHolder loadConfigFromFileForDimension(int dimensionID) {
      String fileName = "DIM" + dimensionID + "_config.cfg";
      File configFile = new File(BetterCaves.customConfigDir, fileName);
      if (!configFile.exists() || configFile.isDirectory()) {
         BetterCaves.LOGGER.info(String.format("Better Caves config file for dimension %d not found. Using global config...", dimensionID));
         return new ConfigHolder();
      } else if (!configFile.canRead()) {
         BetterCaves.LOGGER.warn(String.format("Better Caves config file for dimension %d not readable. Using global config...", dimensionID));
         return new ConfigHolder();
      } else {
         BetterCaves.LOGGER.info(String.format("Reading Better Caves config from file for dimension %d...", dimensionID));
         return parseConfigFromFile(configFile);
      }
   }

   private static ConfigHolder parseConfigFromFile(File file) {
      ConfigHolder config = new ConfigHolder();
      BufferedReader buffer = null;
      UnicodeInputStreamReader input = null;
      String fileName = file.getName();

      try {
         Map<String, ConfigCategory> categories = new TreeMap<>();
         input = new UnicodeInputStreamReader(new FileInputStream(file), "UTF-8");
         buffer = new BufferedReader(input);
         ConfigCategory currCategory = null;
         Type type = null;
         ArrayList<String> tmpList = null;
         int lineNum = 0;
         String name = null;

         label326:
         while (true) {
            lineNum++;
            String line = buffer.readLine();
            if (line == null) {
               return config;
            }

            int nameStart = -1;
            int nameEnd = -1;
            boolean skip = false;
            boolean quoted = false;
            boolean isTypeSpecified = false;
            boolean isFirstNonWhitespaceCharOnLine = true;
            int i = 0;

            while (i < line.length() && !skip) {
               if (!Character.isLetterOrDigit(line.charAt(i)) && "._-".indexOf(line.charAt(i)) == -1 && (!quoted || line.charAt(i) == '"')) {
                  label307:
                  if (!Character.isWhitespace(line.charAt(i))) {
                     switch (line.charAt(i)) {
                        case '"':
                           if (tmpList == null) {
                              if (quoted) {
                                 quoted = false;
                              }

                              if (!quoted && nameStart == -1) {
                                 quoted = true;
                              }
                           }
                           break;
                        case '#':
                           if (tmpList == null) {
                              skip = true;
                              break label307;
                           }
                           break;
                        case ':':
                           if (tmpList == null) {
                              String typeSubstr = line.substring(nameStart, nameEnd + 1);
                              type = Type.tryParse(typeSubstr.charAt(0));
                              nameEnd = -1;
                              nameStart = -1;
                              isTypeSpecified = true;
                           }
                           break;
                        case '<':
                           if (tmpList != null && i + 1 == line.length() || tmpList == null && i + 1 != line.length()) {
                              throw new RuntimeException(String.format("Malformed list property \"%s:%d\"", fileName, lineNum));
                           }

                           if (i + 1 == line.length()) {
                              name = line.substring(nameStart, nameEnd + 1);
                              if (currCategory == null) {
                                 throw new RuntimeException(String.format("'%s' has no scope (missing category?) in '%s:%d'", name, fileName, lineNum));
                              }

                              tmpList = new ArrayList<>();
                              skip = true;
                           }
                           break;
                        case '=':
                           if (tmpList == null) {
                              name = line.substring(nameStart, nameEnd + 1);
                              if (currCategory == null) {
                                 throw new RuntimeException(String.format("'%s' has no scope (missing category?) in '%s:%d'", name, fileName, lineNum));
                              }

                              if (!isTypeSpecified) {
                                 BetterCaves.LOGGER
                                    .warn(
                                       String.format(
                                          "Error in Better Caves config for %s (line %d): missing variable type specifier. Inferring String...",
                                          fileName,
                                          lineNum
                                       )
                                    );
                              }

                              Property prop = new Property(name, line.substring(i + 1), type, true);
                              String fullName = currCategory.getQualifiedName() + "." + name;
                              ConfigHolder.ConfigOption target = config.properties.get(fullName);
                              if (target == null) {
                                 BetterCaves.LOGGER.error(String.format("ERROR: INVALID PROPERTY %s in config %s. Skipping...", fullName, fileName));
                                 i = line.length();
                                 break label307;
                              }

                              switch (type) {
                                 case INTEGER:
                                    if (!target.type.equals(int.class) && !target.type.equals(Integer.class)) {
                                       BetterCaves.LOGGER.error(String.format("ERROR: WRONG TYPE for %s in config %s. Skipping...", fullName, fileName));
                                       i = line.length();
                                       break label307;
                                    }

                                    target.set(prop.getInt());
                                    break;
                                 case DOUBLE:
                                    if (!target.type.equals(double.class)
                                       && !target.type.equals(Double.class)
                                       && !target.type.equals(float.class)
                                       && !target.type.equals(Float.class)) {
                                       BetterCaves.LOGGER.error(String.format("ERROR: WRONG TYPE for %s in config %s. Skipping...", fullName, fileName));
                                       i = line.length();
                                       break label307;
                                    }

                                    if (target.type == Float.class) {
                                       target.set((float)prop.getDouble());
                                    } else {
                                       target.set(prop.getDouble());
                                    }
                                    break;
                                 case BOOLEAN:
                                    if (!target.type.equals(boolean.class) && !target.type.equals(Boolean.class)) {
                                       BetterCaves.LOGGER.error(String.format("ERROR: WRONG TYPE for %s in config %s. Skipping...", fullName, fileName));
                                       i = line.length();
                                       break label307;
                                    }

                                    if (!line.substring(i + 1).toLowerCase().equals("true") && !line.substring(i + 1).toLowerCase().equals("false")) {
                                       throw new RuntimeException(String.format("Invalid Boolean value for property '%s:%d'", fullName, lineNum));
                                    }

                                    target.set(prop.getBoolean());
                                    break;
                                 default:
                                    if (!target.type.equals(String.class)
                                       && !target.type.equals(RegionSize.class)
                                       && !target.type.equals(FastNoise.NoiseType.class)) {
                                       BetterCaves.LOGGER.error(String.format("ERROR: WRONG TYPE for %s in config %s. Skipping...", fullName, fileName));
                                       i = line.length();
                                       break label307;
                                    }

                                    if (target.type == RegionSize.class) {
                                       target.set(RegionSize.valueOf(prop.getString()));
                                    } else if (target.type == FastNoise.NoiseType.class) {
                                       target.set(FastNoise.NoiseType.valueOf(prop.getString()));
                                    } else {
                                       target.set(prop.getString());
                                    }
                              }

                              currCategory.put(name, prop);
                              BetterCaves.LOGGER.debug(String.format("%s: overriding config option: %s", fileName, fullName));
                              i = line.length();
                           }
                           break;
                        case '>':
                           if (tmpList == null) {
                              throw new RuntimeException(String.format("Malformed list property \"%s:%d\"", fileName, lineNum));
                           }

                           if (isFirstNonWhitespaceCharOnLine) {
                              currCategory.put(name, new Property(name, tmpList.toArray(new String[0]), type));
                              name = null;
                              tmpList = null;
                              type = null;
                           }
                           break;
                        case '{':
                           if (tmpList == null) {
                              name = line.substring(nameStart, nameEnd + 1);
                              name = name.toLowerCase(Locale.ENGLISH);
                              String qualifiedName = ConfigCategory.getQualifiedName(name, currCategory);
                              ConfigCategory category = categories.get(qualifiedName);
                              if (category == null) {
                                 currCategory = new ConfigCategory(name, currCategory);
                                 categories.put(qualifiedName, currCategory);
                              } else {
                                 currCategory = category;
                              }

                              name = null;
                           }
                           break;
                        case '}':
                           if (tmpList == null) {
                              if (currCategory == null) {
                                 throw new RuntimeException(
                                    String.format("Invalid config file: attempted to close too many categories '%s:%d'", fileName, lineNum)
                                 );
                              }

                              currCategory = currCategory.parent;
                           }
                           break;
                        case '~':
                           if (tmpList != null) {
                           }
                           break;
                        default:
                           if (tmpList == null) {
                              throw new RuntimeException(String.format("Unknown character '%s' in '%s:%d'", line.charAt(i), fileName, lineNum));
                           }
                     }

                     isFirstNonWhitespaceCharOnLine = false;
                  }
               } else {
                  if (nameStart == -1) {
                     nameStart = i;
                  }

                  nameEnd = i;
                  isFirstNonWhitespaceCharOnLine = false;
               }

               i++;
               continue;

               if (quoted) {
                  throw new RuntimeException(String.format("Unmatched quote in '%s:%d'", fileName, lineNum));
               }

               if (tmpList != null && !skip) {
                  tmpList.add(line.trim());
               }
               continue label326;
            }

            return config;
         }
      } catch (Exception var28) {
         BetterCaves.LOGGER.error(String.format("ERROR LOADING BETTER CAVES CONFIG %s: %s.", fileName, var28.toString()));
         BetterCaves.LOGGER.info("USING GLOBAL CONFIG FILE INSTEAD...");
         return new ConfigHolder();
      } finally {
         IOUtils.closeQuietly(buffer);
         IOUtils.closeQuietly(input);
      }
   }
}
