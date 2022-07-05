package org.betterx.bclib.util;

import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;

import com.google.common.collect.Sets;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Set;

public class TranslationHelper {
    /**
     * Print English translation file lines. Translation is "auto-beautified" text (example "strange_thing" -> "Strange Thing").
     *
     * @param modID {@link String} mod ID string.
     */
    public static void printMissingEnNames(String modID) {
        printMissingNames(modID, "en_us");
    }

    /**
     * Prints translation file lines for specified language.
     *
     * @param modID        {@link String} mod ID string;
     * @param languageCode {@link String} language code (example "en_us", "ru_ru").
     */
    public static void printMissingNames(String modID, String languageCode) {
        Set<String> missingNames = Sets.newHashSet();

        Gson gson = new Gson();
        InputStream inputStream = TranslationHelper.class.getResourceAsStream("/assets/" + modID + "/lang/" + languageCode + ".json");
        JsonObject translation = inputStream == null
                ? new JsonObject()
                : gson.fromJson(new InputStreamReader(inputStream), JsonObject.class);

        Registry.BLOCK.forEach(block -> {
            if (Registry.BLOCK.getKey(block).getNamespace().equals(modID)) {
                String name = block.getName().getString();
                if (!translation.has(name)) {
                    missingNames.add(name);
                }
            }
        });

        Registry.ITEM.forEach(item -> {
            if (Registry.ITEM.getKey(item).getNamespace().equals(modID)) {
                String name = item.getDescription().getString();
                if (!translation.has(name)) {
                    missingNames.add(name);
                }
            }
        });

        BuiltinRegistries.BIOME.forEach(biome -> {
            ResourceLocation id = BuiltinRegistries.BIOME.getKey(biome);
            if (id.getNamespace().equals(modID)) {
                String name = "biome." + modID + "." + id.getPath();
                if (!translation.has(name)) {
                    missingNames.add(name);
                }
            }
        });

        Registry.ENTITY_TYPE.forEach((entity) -> {
            ResourceLocation id = Registry.ENTITY_TYPE.getKey(entity);
            if (id.getNamespace().equals(modID)) {
                String name = "entity." + modID + "." + id.getPath();
                if (!translation.has(name)) {
                    missingNames.add(name);
                }
            }
        });

        if (!missingNames.isEmpty()) {

            System.out.println("========================================");
            System.out.println("		   MISSING NAMES LIST");

            if (!missingNames.isEmpty()) {
                if (languageCode.equals("en_us")) {
                    System.out.println("========================================");
                    System.out.println("	  AUTO ENGLISH BEAUTIFICATION");
                    System.out.println("========================================");
                    missingNames.stream().sorted().forEach(name -> {
                        System.out.println("	\"" + name + "\": \"" + fastTranslateEn(name) + "\",");
                    });
                } else {
                    System.out.println("========================================");
                    System.out.println("		   TEMPLATE: [" + languageCode + "]");
                    System.out.println("========================================");
                    missingNames.stream().sorted().forEach(name -> {
                        System.out.println("	\"" + name + "\": \"\",");
                    });
                }
            }

            System.out.println("========================================");
        }
    }

    /**
     * Simple fast text beautification (example "strange_thing" -> "Strange Thing").
     *
     * @param text {@link String} to process;
     * @return {@link String} result.
     */
    public static String fastTranslateEn(String text) {
        String[] words = text.substring(text.lastIndexOf('.') + 1).split("_");
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < words.length; i++) {
            String word = words[i];
            builder.append(Character.toUpperCase(word.charAt(0)));
            builder.append(word, 1, word.length());
            if (i < words.length - 1) {
                builder.append(' ');
            }
        }
        return builder.toString();
    }
}
