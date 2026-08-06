package org.betterx.bclib.commands;

import org.betterx.bclib.util.BlocksHelper;
import de.ambertation.wover.tag.api.predefined.CommonBlockTags;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.core.BlockPos.MutableBlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.BlockItemTags;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;

import java.util.HashMap;
import java.util.Map;

public class CommandRegistry {
    public static void register() {
        CommandRegistrationCallback.EVENT.register(CommandRegistry::register);
    }

    private static void register(
            CommandDispatcher<CommandSourceStack> dispatcher,
            CommandBuildContext commandBuildContext,
            Commands.CommandSelection commandSelection
    ) {
        LiteralArgumentBuilder<CommandSourceStack> bnContext = Commands.literal("bclib")
                                                                       .requires(Commands.hasPermission(Commands.LEVEL_OWNERS));

        bnContext = PlaceCommand.register(bnContext, commandBuildContext);
        bnContext = PrintInfo.register(bnContext);
        bnContext = DumpMap.register(bnContext);

        dispatcher.register(
                bnContext
                        .then(Commands.literal("request_garbage_collection")
                                      .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                                      .executes(CommandRegistry::requestGC)
                        )
                        .then(Commands.literal("debug_ore")
                                      .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                                      .then(Commands
                                              .argument("rings", IntegerArgumentType.integer(0, 8))
                                              .executes(ctx -> CommandRegistry.revealOre(ctx, IntegerArgumentType.getInteger(ctx, "rings"))))
                                      .executes((cc) -> CommandRegistry.revealOre(cc, 0))
                        )
                        .then(Commands.literal("sliceZ")
                                      .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                                      .executes(ctx -> slice(ctx, true))
                        )
                        .then(Commands.literal("sliceX")
                                      .requires(Commands.hasPermission(Commands.LEVEL_OWNERS))
                                      .executes(ctx -> slice(ctx, false))
                        )
        );
    }

    private static int requestGC(CommandContext<CommandSourceStack> ctx) {
        System.gc();
        return Command.SINGLE_SUCCESS;
    }

    private static final Map<Holder<Biome>, BlockState> biomeMap = new HashMap<>();
    private static int biomeMapIdx = 0;
    // 26.2 folded the 16 per-colour Blocks constants into ColorCollection<Block> (Blocks.STAINED_GLASS,
    // Blocks.CONCRETE, ...), the same treatment WOOL/CARPET already had; pick(DyeColor) replaces the
    // individual fields and returns the identical blocks.
    private static final BlockState[] states = {
            Blocks.STAINED_GLASS.pick(DyeColor.RED).defaultBlockState(),
            Blocks.STAINED_GLASS.pick(DyeColor.BLUE).defaultBlockState(),
            Blocks.STAINED_GLASS.pick(DyeColor.YELLOW).defaultBlockState(),
            Blocks.STAINED_GLASS.pick(DyeColor.LIME).defaultBlockState(),
            Blocks.STAINED_GLASS.pick(DyeColor.PINK).defaultBlockState(),
            Blocks.STAINED_GLASS.pick(DyeColor.GREEN).defaultBlockState(),
            Blocks.STAINED_GLASS.pick(DyeColor.WHITE).defaultBlockState(),
            Blocks.STAINED_GLASS.pick(DyeColor.BLACK).defaultBlockState(),
            Blocks.STAINED_GLASS.pick(DyeColor.ORANGE).defaultBlockState(),
            Blocks.STAINED_GLASS.pick(DyeColor.LIGHT_BLUE).defaultBlockState()
    };
    private static final BlockState[] states2 = {
            Blocks.CONCRETE.pick(DyeColor.RED).defaultBlockState(),
            Blocks.CONCRETE.pick(DyeColor.BLUE).defaultBlockState(),
            Blocks.CONCRETE.pick(DyeColor.YELLOW).defaultBlockState(),
            Blocks.CONCRETE.pick(DyeColor.LIME).defaultBlockState(),
            Blocks.CONCRETE.pick(DyeColor.PINK).defaultBlockState(),
            Blocks.CONCRETE.pick(DyeColor.GREEN).defaultBlockState(),
            Blocks.CONCRETE.pick(DyeColor.WHITE).defaultBlockState(),
            Blocks.CONCRETE.pick(DyeColor.BLACK).defaultBlockState(),
            Blocks.CONCRETE.pick(DyeColor.ORANGE).defaultBlockState(),
            Blocks.CONCRETE.pick(DyeColor.LIGHT_BLUE).defaultBlockState()
    };

    private static int revealOre(CommandContext<CommandSourceStack> ctx, int chunks) throws CommandSyntaxException {
        final CommandSourceStack source = ctx.getSource();
        final ServerLevel level = source.getLevel();
        final Vec3 spos = source.getPosition();

        MutableBlockPos bp = new MutableBlockPos();
        BlockState state;
        BlockState fillState;
        final BlockState AIR = Blocks.AIR.defaultBlockState();

        for (int ox = -chunks; ox <= chunks; ox++) {
            for (int oz = -chunks; oz <= chunks; oz++) {
                final Vec3 pos = new Vec3(spos.x + ox * 64, spos.y, spos.z + oz * 64);
                source.getPlayer().teleportTo(pos.x, pos.y, pos.z);
                for (int y = 1; y < level.getHeight(); y++) {
                    bp.setY(y);
                    for (int x = -64; x < 64; x++) {
                        bp.setX((int) pos.x + x);
                        for (int z = -64; z < 64; z++) {
                            bp.setZ((int) pos.z + z);
                            if (y == 1) {
                                Holder<Biome> b = level.getBiome(bp);
                                fillState = biomeMap.computeIfAbsent(b, (bb) -> {
                                    biomeMapIdx = (biomeMapIdx + 1) % states.length;
                                    return states[biomeMapIdx];
                                });
                            } else {
                                fillState = AIR;
                            }

                            state = level.getBlockState(bp);
                            if (y == 1 || !state.is(Blocks.AIR)) {
                                // 26.2 dropped the BlockTags constants for the five ores below; the tags
                                // themselves are unchanged and still reachable through BlockItemTags,
                                // which is where BlockTags reads its surviving ore keys from as well
                                // (BlockTags.GOLD_ORES is literally BlockItemTags.GOLD_ORES.block()).
                                if (!(state.is(CommonBlockTags.NETHER_ORES)
                                        || state.is(CommonBlockTags.END_ORES)
                                        || state.is(BlockItemTags.COAL_ORES.block())
                                        || state.is(BlockTags.COPPER_ORES)
                                        || state.is(BlockItemTags.DIAMOND_ORES.block())
                                        || state.is(BlockItemTags.EMERALD_ORES.block())
                                        || state.is(BlockTags.GOLD_ORES)
                                        || state.is(BlockTags.IRON_ORES)
                                        || state.is(BlockItemTags.LAPIS_ORES.block())
                                        || state.is(BlockItemTags.REDSTONE_ORES.block())
                                        || state.is(Blocks.NETHER_QUARTZ_ORE)
                                        || state.is(Blocks.NETHER_GOLD_ORE)
                                        || state.is(Blocks.ANCIENT_DEBRIS))) {
                                    BlocksHelper.setWithoutUpdate(level, bp, fillState);
                                }
                            }
                        }
                    }
                }
            }
        }
        source.getPlayer().teleportTo(spos.x, spos.y, spos.z);
        return Command.SINGLE_SUCCESS;
    }

    private static int slice(CommandContext<CommandSourceStack> ctx, boolean constX) throws CommandSyntaxException {
        final CommandSourceStack source = ctx.getSource();
        final ServerLevel level = source.getLevel();
        final Vec3 pos = source.getPosition();

        BlockState AIR = Blocks.AIR.defaultBlockState();
        MutableBlockPos bp = new MutableBlockPos();
        BlockState state;
        BlockState fillState;


        for (int y = 1; y < level.getHeight(); y++) {
            bp.setY(y);
            for (int x = constX ? 0 : -64; x < 64; x++) {
                bp.setX((int) pos.x + x);
                for (int z = constX ? -64 : 0; z < 64; z++) {
                    bp.setZ((int) pos.z + z);
                    if (y == 1) {
                        Holder<Biome> b = level.getBiome(bp);
                        fillState = biomeMap.computeIfAbsent(b, (bb) -> {
                            biomeMapIdx = (biomeMapIdx + 1) % states.length;
                            return states[biomeMapIdx];
                        });
                    } else {
                        fillState = AIR;
                    }

                    BlocksHelper.setWithoutUpdate(level, bp, fillState);
                }
            }
        }
        return Command.SINGLE_SUCCESS;
    }

    private static int findSurface(CommandContext<CommandSourceStack> ctx) throws CommandSyntaxException {
        final CommandSourceStack source = ctx.getSource();
        final ServerPlayer player = source.getPlayerOrException();
        Vec3 pos = source.getPosition();
        final ServerLevel level = source.getLevel();
        MutableBlockPos mPos = new BlockPos((int) pos.x, (int) pos.y, (int) pos.z).mutable();
        System.out.println("Staring at: " + mPos + " -> " + level.getBlockState(mPos));
        boolean found = org.betterx.bclib.util.BlocksHelper.findSurroundingSurface(
                level,
                mPos,
                Direction.DOWN,
                12,
                state -> BlocksHelper.isTerrain(state)
        );
        System.out.println("Ending at: " + mPos + " -> " + level.getBlockState(mPos) + " = " + found);
        org.betterx.bclib.util.BlocksHelper.setWithoutUpdate(
                level,
                new BlockPos((int) pos.x, (int) pos.y, (int) pos.z),
                Blocks.CONCRETE.pick(DyeColor.YELLOW)
        );
        org.betterx.bclib.util.BlocksHelper.setWithoutUpdate(level, mPos, Blocks.CONCRETE.pick(DyeColor.LIGHT_BLUE));
        return Command.SINGLE_SUCCESS;
    }
}

