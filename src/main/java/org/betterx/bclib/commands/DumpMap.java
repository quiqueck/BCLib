package org.betterx.bclib.commands;

import org.betterx.bclib.BCLib;
import org.betterx.wover.state.api.WorldState;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.QuartPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.phys.Vec3;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class DumpMap {
    //serverLevel.getChunkSource().randomState()

    public static LiteralArgumentBuilder<CommandSourceStack> register(LiteralArgumentBuilder<CommandSourceStack> bnContext) {
        return bnContext
                .then(Commands.literal("dump_maps")
                              .requires(source -> source.hasPermission(Commands.LEVEL_OWNERS))
                              .then(Commands.literal("png").executes(DumpMap::dumpImageMaps))
                              .then(Commands.literal("json").executes(DumpMap::dumpJsonMaps))
                );
    }

    static int dumpJsonMaps(CommandContext<CommandSourceStack> ctx) {
        final CommandSourceStack source = ctx.getSource();
        final ServerLevel serverLevel = source.getLevel();
        final Vec3 pos = source.getPosition();
        final var basePath = WorldState
                .storageAccess()
                .getLevelPath(LevelResource.ROOT)
                .resolve(BCLib.C.namespace)
                .resolve("export")
                .resolve(serverLevel.dimension().location().getPath())
                .normalize();

        final RandomState randomState = serverLevel.getChunkSource().randomState();
        final Climate.Sampler sampler = randomState.sampler();
        int x = QuartPos.fromBlock((int) pos.x);
        int z = QuartPos.fromBlock((int) pos.z);
        int minHeight = QuartPos.fromBlock(serverLevel.getMinBuildHeight());
        int maxHeight = QuartPos.fromBlock(serverLevel.getMaxBuildHeight());
        int maxOffset = 128;

        MutableComponent result = Component
                .literal("Wrote maps to " + basePath.toString() + ":\n")
                .setStyle(Style.EMPTY.withBold(true).withColor(ChatFormatting.BLUE));

        JsonObject root = new JsonObject();
        JsonObject header = new JsonObject();
        JsonObject center = new JsonObject();
        center.addProperty("x", (int) pos.x);
        center.addProperty("y", (int) pos.y);
        center.addProperty("z", (int) pos.z);

        JsonObject start = new JsonObject();
        start.addProperty("x", x - maxOffset);
        start.addProperty("y", minHeight);
        start.addProperty("z", z - maxOffset);

        JsonObject dimensions = new JsonObject();
        dimensions.addProperty("x", 2 * maxOffset + 1);
        dimensions.addProperty("y", maxHeight - minHeight);
        dimensions.addProperty("z", 2 * maxOffset + 1);

        header.add("center", center);
        header.add("start", start);
        header.add("size", dimensions);
        header.addProperty("dimension", serverLevel.dimension().location().toString());

        root.add("info", header);

        long minTemperature = Long.MAX_VALUE;
        long minHumidity = Long.MAX_VALUE;
        long minContinentalness = Long.MAX_VALUE;
        long minErosion = Long.MAX_VALUE;
        long minDepth = Long.MAX_VALUE;
        long minWeirdness = Long.MAX_VALUE;

        long maxTemperature = Long.MIN_VALUE;
        long maxHumidity = Long.MIN_VALUE;
        long maxContinentalness = Long.MIN_VALUE;
        long maxErosion = Long.MIN_VALUE;
        long maxDepth = Long.MIN_VALUE;
        long maxWeirdness = Long.MIN_VALUE;

        JsonArray samplesX = new JsonArray();
        for (int ox = -maxOffset; ox <= maxOffset; ox++) {
            JsonArray samplesZ = new JsonArray();
            for (int oz = -maxOffset; oz <= maxOffset; oz++) {
                JsonArray samplesY = new JsonArray();
                for (int y = minHeight; y <= maxHeight; y++) {
                    JsonArray samples = new JsonArray();
                    final Climate.TargetPoint t = sampler.sample(x + ox, y, z + oz);
                    samples.add(t.temperature());
                    samples.add(t.humidity());
                    samples.add(t.continentalness());
                    samples.add(t.erosion());
                    samples.add(t.depth());
                    samples.add(t.weirdness());
                    samplesY.add(samples);

                    if (t.temperature() < minTemperature) minTemperature = t.temperature();
                    if (t.humidity() < minHumidity) minHumidity = t.humidity();
                    if (t.continentalness() < minContinentalness) minContinentalness = t.continentalness();
                    if (t.erosion() < minErosion) minErosion = t.erosion();
                    if (t.depth() < minDepth) minDepth = t.depth();
                    if (t.weirdness() < minWeirdness) minWeirdness = t.weirdness();

                    if (t.temperature() > maxTemperature) maxTemperature = t.temperature();
                    if (t.humidity() > maxHumidity) maxHumidity = t.humidity();
                    if (t.continentalness() > maxContinentalness) maxContinentalness = t.continentalness();
                    if (t.erosion() > maxErosion) maxErosion = t.erosion();
                    if (t.depth() > maxDepth) maxDepth = t.depth();
                    if (t.weirdness() > maxWeirdness) maxWeirdness = t.weirdness();
                }
                samplesZ.add(samplesY);
            }
            samplesX.add(samplesZ);
        }

        JsonObject extrema = new JsonObject();
        setExtrema("temperature", minTemperature, maxTemperature, extrema);
        setExtrema("humidity", minHumidity, maxHumidity, extrema);
        setExtrema("continentalness", minContinentalness, maxContinentalness, extrema);
        setExtrema("erosion", minErosion, maxErosion, extrema);
        setExtrema("depth", minDepth, maxDepth, extrema);
        setExtrema("weirdness", minWeirdness, maxWeirdness, extrema);
        header.add("extrema", extrema);

        root.add("samples", samplesX);

        final File fJson = new File(basePath.toString() + "/samples.json");
        //Use Gson to write the json data to disc
        try {
            Gson gson = new Gson();
            String s = gson.toJson(root);
            java.nio.file.Files.writeString(fJson.toPath(), s);
        } catch (Exception e) {
            BCLib.C.LOG.error("Error while saving json: " + e.getMessage());
            result.append(Component.literal("Error while saving json: " + fJson.toString()));
        }

        ctx.getSource().sendSuccess(() -> result, false);
        return Command.SINGLE_SUCCESS;
    }

    private static void setExtrema(String property, long minTemperature, long maxTemperature, JsonObject root) {
        JsonObject jTemperature = new JsonObject();
        jTemperature.addProperty("min", minTemperature);
        jTemperature.addProperty("max", maxTemperature);
        root.add(property, jTemperature);
    }

    static int dumpImageMaps(CommandContext<CommandSourceStack> ctx) {
        final CommandSourceStack source = ctx.getSource();
        final ServerLevel serverLevel = source.getLevel();
        final Vec3 pos = source.getPosition();
        final var basePath = WorldState
                .storageAccess()
                .getLevelPath(LevelResource.ROOT)
                .resolve(BCLib.C.namespace)
                .resolve("export")
                .resolve(serverLevel.dimension().location().getPath())
                .normalize();

        final RandomState randomState = serverLevel.getChunkSource().randomState();
        final Climate.Sampler sampler = randomState.sampler();
        int x = QuartPos.fromBlock((int) pos.x);
        int y = QuartPos.fromBlock((int) pos.y);
        int z = QuartPos.fromBlock((int) pos.z);
        int maxOffset = 128;

        MutableComponent result = Component
                .literal("Wrote maps to " + basePath.toString() + ":\n")
                .setStyle(Style.EMPTY.withBold(true).withColor(ChatFormatting.BLUE));


        //find extrema
        long minTemperature = Long.MAX_VALUE;
        long minHumidity = Long.MAX_VALUE;
        long minContinentalness = Long.MAX_VALUE;
        long minErosion = Long.MAX_VALUE;
        long minDepth = Long.MAX_VALUE;
        long minWeirdness = Long.MAX_VALUE;

        long maxTemperature = Long.MIN_VALUE;
        long maxHumidity = Long.MIN_VALUE;
        long maxContinentalness = Long.MIN_VALUE;
        long maxErosion = Long.MIN_VALUE;
        long maxDepth = Long.MIN_VALUE;
        long maxWeirdness = Long.MIN_VALUE;

        for (int ox = -maxOffset; ox <= maxOffset; ox++)
            for (int oz = -maxOffset; oz <= maxOffset; oz++) {
                final Climate.TargetPoint t = sampler.sample(x + ox, y, z + oz);
                if (t.temperature() < minTemperature) minTemperature = t.temperature();
                if (t.humidity() < minHumidity) minHumidity = t.humidity();
                if (t.continentalness() < minContinentalness) minContinentalness = t.continentalness();
                if (t.erosion() < minErosion) minErosion = t.erosion();
                if (t.depth() < minDepth) minDepth = t.depth();
                if (t.weirdness() < minWeirdness) minWeirdness = t.weirdness();

                if (t.temperature() > maxTemperature) maxTemperature = t.temperature();
                if (t.humidity() > maxHumidity) maxHumidity = t.humidity();
                if (t.continentalness() > maxContinentalness) maxContinentalness = t.continentalness();
                if (t.erosion() > maxErosion) maxErosion = t.erosion();
                if (t.depth() > maxDepth) maxDepth = t.depth();
                if (t.weirdness() > maxWeirdness) maxWeirdness = t.weirdness();
            }
        if (minTemperature == maxTemperature) maxTemperature++;
        if (minHumidity == maxHumidity) maxHumidity++;
        if (minContinentalness == maxContinentalness) maxContinentalness++;
        if (minErosion == maxErosion) maxErosion++;
        if (minDepth == maxDepth) maxDepth++;
        if (minWeirdness == maxWeirdness) maxWeirdness++;

        //generate maps
        //create an image with the dimension of 2*maxOffset+1
        //for each pixel, sample the climate at the corresponding offset
        //scale the values to 0-255
        //write the pixel to the image

        BufferedImage iTemperature = new BufferedImage(2 * maxOffset + 1, 2 * maxOffset + 1, BufferedImage.TYPE_BYTE_GRAY);
        BufferedImage iHumidity = new BufferedImage(2 * maxOffset + 1, 2 * maxOffset + 1, BufferedImage.TYPE_BYTE_GRAY);
        BufferedImage iContinentalness = new BufferedImage(2 * maxOffset + 1, 2 * maxOffset + 1, BufferedImage.TYPE_BYTE_GRAY);
        BufferedImage iErosion = new BufferedImage(2 * maxOffset + 1, 2 * maxOffset + 1, BufferedImage.TYPE_BYTE_GRAY);
        BufferedImage iDepth = new BufferedImage(2 * maxOffset + 1, 2 * maxOffset + 1, BufferedImage.TYPE_BYTE_GRAY);
        BufferedImage iWeirdness = new BufferedImage(2 * maxOffset + 1, 2 * maxOffset + 1, BufferedImage.TYPE_BYTE_GRAY);

        for (int ox = -maxOffset; ox <= maxOffset; ox++)
            for (int oz = -maxOffset; oz <= maxOffset; oz++) {
                final Climate.TargetPoint t = sampler.sample(x + ox, y, z + oz);
                final int temperature = (int) ((t.temperature() - minTemperature) * 255 / (maxTemperature - minTemperature));
                final int humidity = (int) ((t.humidity() - minHumidity) * 255 / (maxHumidity - minHumidity));
                final int continentalness = (int) ((t.continentalness() - minContinentalness) * 255 / (maxContinentalness - minContinentalness));
                final int erosion = (int) ((t.erosion() - minErosion) * 255 / (maxErosion - minErosion));
                final int depth = (int) ((t.depth() - minDepth) * 255 / (maxDepth - minDepth));
                final int weirdness = (int) ((t.weirdness() - minWeirdness) * 255 / (maxWeirdness - minWeirdness));

                iTemperature.setRGB(ox + maxOffset, oz + maxOffset, new Color(temperature, temperature, temperature).getRGB());
                iHumidity.setRGB(ox + maxOffset, oz + maxOffset, new Color(humidity, humidity, humidity).getRGB());
                iContinentalness.setRGB(ox + maxOffset, oz + maxOffset, new Color(continentalness, continentalness, continentalness).getRGB());
                iErosion.setRGB(ox + maxOffset, oz + maxOffset, new Color(erosion, erosion, erosion).getRGB());
                iDepth.setRGB(ox + maxOffset, oz + maxOffset, new Color(depth, depth, depth).getRGB());
                iWeirdness.setRGB(ox + maxOffset, oz + maxOffset, new Color(weirdness, weirdness, weirdness).getRGB());
            }

        // Save the image to a file
        //create the basePath if it is missing
        File fBasePath = basePath.toFile();
        if (!fBasePath.exists()) {
            if (!fBasePath.mkdirs()) {
                BCLib.C.LOG.error("Error while creating directory: " + fBasePath.toString());
                //append error to the result output
                result.append(Component.literal("Error while creating directory: " + fBasePath.toString()));
                return Command.SINGLE_SUCCESS;
            }
        }

        write(iTemperature, new File(basePath.toString() + "/temperature_" + minTemperature + "_" + maxTemperature + ".png"), result);
        write(iHumidity, new File(basePath.toString() + "/humidity_" + minHumidity + "_" + maxHumidity + ".png"), result);
        write(iContinentalness, new File(basePath.toString() + "/continentalness_" + minContinentalness + "_" + maxContinentalness + ".png"), result);
        write(iErosion, new File(basePath.toString() + "/erosion_" + minErosion + "_" + maxErosion + ".png"), result);
        write(iDepth, new File(basePath.toString() + "/depth_" + minDepth + "_" + maxDepth + ".png"), result);
        write(iWeirdness, new File(basePath.toString() + "/weirdness_" + minWeirdness + "_" + maxWeirdness + ".png"), result);

        ctx.getSource().sendSuccess(() -> result, false);
        return Command.SINGLE_SUCCESS;
    }

    private static void write(BufferedImage iTemperature, File fTemperature, MutableComponent result) {
        try {
            ImageIO.write(iTemperature, "png", fTemperature);
        } catch (IOException e) {
            BCLib.C.LOG.error("Error while saving image: " + e.getMessage());
            result.append(Component.literal("Error while saving image: " + fTemperature.toString()));
        }
    }
}
