package org.betterx.bclib.commands;

import org.betterx.bclib.BCLib;
import org.betterx.bclib.blocks.BaseStairsBlock;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import com.mojang.serialization.codecs.KeyDispatchCodec;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.Holder;
import net.minecraft.core.RegistryAccess;
import net.minecraft.resources.RegistryOps;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorList;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureProcessorType;
import net.minecraft.world.level.material.Fluid;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;

public class DumpDatapack {
    static int dumpDatapack(CommandContext<CommandSourceStack> ctx) {
        dumpDatapack(ctx.getSource().getLevel().registryAccess());
        return Command.SINGLE_SUCCESS;
    }

    public static void dumpDatapack(RegistryAccess registryAccess) {
        final RegistryOps<JsonElement> registryOps = RegistryOps.create(JsonOps.INSTANCE, registryAccess);
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder = gsonBuilder.setPrettyPrinting();
        Gson gson = gsonBuilder.create();
        registryAccess.registries().forEach(r -> dumpDatapack(r, registryOps, gson));
    }

    private static <T> void dumpDatapack(
            RegistryAccess.RegistryEntry<T> registry,
            RegistryOps<JsonElement> registryOps,
            Gson gson
    ) {
        File base = new File(System.getProperty("user.dir"), "bclib_datapack_dump");
        BCLib.LOGGER.info(registry.key().toString());
        // Tag Output
//        registry.value()
//                .getTagNames()
//                .map(tagKey -> registry.value().getTag(tagKey))
//                .filter(tag -> tag.isPresent())
//                .map(tag -> tag.get())
//                .forEach(tag -> {
//                    tag.stream()
//                       .map(holder -> holder.unwrapKey())
//                       .filter(k -> k.isPresent())
//                       .map(k -> k.get())
//                       .forEach(key -> {
//
//                       });
//                });

        registry
                .value()
                .entrySet()
                .stream()
                .map(e -> e.getKey()).map(key -> registry.value().getHolder(key).get())
                .forEach(holder -> {
                    File f1 = new File(base, holder.unwrapKey().get().location().getNamespace());
                    f1 = new File(f1, registry.key().location().getPath());
                    f1.mkdirs();
                    f1 = new File(f1, holder.unwrapKey().get().location().getPath() + ".json");

                    Codec[] codec = {null};

                    BCLib.LOGGER.info("   - " + f1);
                    Object obj = holder;

                    while (obj instanceof Holder<?>) {
                        obj = ((Holder<?>) obj).value();
                    }

                    if (obj instanceof Structure s) {
                        codec[0] = s.type().codec();
                    } else if (obj instanceof StructureProcessorList s) {
                        codec[0] = StructureProcessorType.LIST_OBJECT_CODEC;
                    } else if (obj instanceof GameEvent) {
                        return;
                    } else if (obj instanceof Fluid) {
                        return;
                    } else if (obj instanceof MobEffect) {
                        return;
                    } else if (obj instanceof BaseStairsBlock) {
                        return;
                    }

                    if (codec[0] == null) {
                        for (Method m : obj.getClass().getMethods()) {
                            if (!Modifier.isStatic(m.getModifiers())) {
                                m.setAccessible(true);
                                if (m.getParameterTypes().length == 0) {
                                    if (Codec.class.isAssignableFrom(m.getReturnType())) {
                                        try {
                                            codec[0] = (Codec) m.invoke(obj);
                                            BCLib.LOGGER.info("      Got Codec from " + m);
                                        } catch (Exception e) {
                                            BCLib.LOGGER.error("     !!! Unable to get Codec from " + m);
                                        }
                                    } else if (KeyDispatchCodec.class.isAssignableFrom(m.getReturnType())) {
                                        try {
                                            codec[0] = ((KeyDispatchCodec) m.invoke(obj)).codec();
                                            BCLib.LOGGER.info("      Got Codec from " + m);
                                        } catch (Exception e) {
                                            BCLib.LOGGER.error("     !!! Unable to get Codec from " + m);
                                        }
                                    } else if (KeyDispatchDataCodec.class.isAssignableFrom(m.getReturnType())) {
                                        try {
                                            codec[0] = ((KeyDispatchDataCodec) m.invoke(obj)).codec();
                                            BCLib.LOGGER.info("      Got Codec from " + m);
                                        } catch (Exception e) {
                                            BCLib.LOGGER.error("     !!! Unable to get Codec from " + m);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (codec[0] == null) {
                        //Try to find DIRECT_CODEC field
                        for (Field f : obj.getClass().getDeclaredFields()) {
                            if (Modifier.isStatic(f.getModifiers())) {
                                if ("DIRECT_CODEC".equals(f.getName())) {
                                    f.setAccessible(true);
                                    try {
                                        codec[0] = (Codec) f.get(null);
                                        BCLib.LOGGER.info("      Got Codec from " + f);
                                    } catch (Exception e) {
                                        BCLib.LOGGER.error("      !!! Unable to get Codec from " + f);
                                    }
                                }
                            }
                        }
                    }

                    //Try to find CODEC field
                    if (codec[0] == null) {
                        for (Field f : obj.getClass().getDeclaredFields()) {
                            if (Modifier.isStatic(f.getModifiers())) {
                                if ("CODEC".equals(f.getName())) {
                                    try {
                                        f.setAccessible(true);
                                        codec[0] = (Codec) f.get(null);
                                        BCLib.LOGGER.info("      Got Codec from " + f);
                                    } catch (Exception e) {
                                        BCLib.LOGGER.error("     !!! Unable to get Codec from " + f);
                                    }
                                }
                            }
                        }
                    }

                    //Try to find any Codec field
                    if (codec[0] == null) {
                        for (Field f : obj.getClass().getDeclaredFields()) {
                            if (Modifier.isStatic(f.getModifiers())) {
                                if (Codec.class.isAssignableFrom(f.getType())) {
                                    f.setAccessible(true);
                                    try {
                                        codec[0] = (Codec) f.get(null);
                                        BCLib.LOGGER.info("      Got Codec from " + f);
                                    } catch (Exception e) {
                                        BCLib.LOGGER.error("     !!! Unable to get Codec from " + f);
                                    }
                                }
                            }
                        }
                    }


                    if (codec[0] != null) {
                        try {
                            var o = codec[0]
                                    .encodeStart(registryOps, holder.value())
                                    .result()
                                    .orElse(new JsonObject());

                            String content = gson.toJson(o);
                            try {
                                Files.writeString(f1.toPath(), content, StandardCharsets.UTF_8);
                            } catch (IOException e) {
                                BCLib.LOGGER.error("      ->> Unable to WRITE: " + e.getMessage());
                            }
                        } catch (Exception e) {
                            BCLib.LOGGER.error("      ->> Unable to encode: " + e.getMessage());
                        }
                    } else {
                        BCLib.LOGGER.error("     !!! Could not determine Codec");
                    }
                });
    }
}
