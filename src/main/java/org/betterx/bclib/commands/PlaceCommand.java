package org.betterx.bclib.commands;

import de.ambertation.wunderlib.math.Float3;
import org.betterx.bclib.api.v2.levelgen.structures.StructureNBT;
import org.betterx.bclib.commands.arguments.Float3ArgumentType;
import org.betterx.bclib.commands.arguments.PlacementDirections;
import org.betterx.bclib.commands.arguments.TemplatePlacementArgument;
import org.betterx.bclib.util.BlocksHelper;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.ResourceLocationArgument;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockStateArgument;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.commands.arguments.coordinates.Coordinates;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.CommandBlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.levelgen.structure.BoundingBox;

import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

class PlaceCommandBuilder {
    public static final String PATH = "path";
    public static final String NBT = "nbt";
    public static final String EMPTY = "empty";
    public static final String PLACEMENT = "placement";
    public static final String POS = "pos";
    public static final String SPAN = "span";
    public static final String BORDER = "border";

    public void register(
            CommandBuildContext ctx,
            LiteralArgumentBuilder<CommandSourceStack> command
    ) {
        final Supplier<RequiredArgumentBuilder<CommandSourceStack, ResourceLocation>> path = () -> Commands.argument(
                PATH,
                ResourceLocationArgument.id()
        );
        final Supplier<RequiredArgumentBuilder<CommandSourceStack, PlacementDirections>> placement = () -> Commands.argument(
                PLACEMENT,
                TemplatePlacementArgument.templatePlacement()
        );
        final Supplier<RequiredArgumentBuilder<CommandSourceStack, Coordinates>> pos = () -> Commands.argument(
                POS,
                BlockPosArgument.blockPos()
        );

        final var nbtTree = Commands.literal(NBT).then(
                path.get().then(
                        placement.get().then(
                                addOptionalsAndExecute(
                                        ctx,
                                        pos.get(),
                                        PlaceCommandBuilder::placeNBT
                                )
                        )
                )
        );

        final var emptyTree = Commands.literal(EMPTY).then(
                path.get().then(
                        placement.get().then(
                                pos.get().then(
                                        addOptionalsAndExecute(
                                                ctx,
                                                Commands.argument(SPAN, Float3ArgumentType.int3(0, 64)),
                                                PlaceCommandBuilder::placeEmpty
                                        )
                                )
                        )
                )
        );

        command
                .then(nbtTree)
                .then(emptyTree);
    }

    private <T> RequiredArgumentBuilder<CommandSourceStack, T> addOptionalsAndExecute(
            CommandBuildContext commandBuildContext,
            RequiredArgumentBuilder<CommandSourceStack, T> root,
            Executor runner
    ) {
        final Supplier<LiteralArgumentBuilder<CommandSourceStack>> addControllers = () -> Commands.literal("controller");
        final Supplier<RequiredArgumentBuilder<CommandSourceStack, BlockInput>> addBorder = () -> Commands.argument(
                BORDER,
                BlockStateArgument.block(commandBuildContext)
        );

        return root
                .executes(c -> runner.exec(c, false, false))
                .then(addBorder.get()
                               .executes(c -> runner.exec(c, true, false))
                               .then(addControllers.get()
                                                   .executes(c -> runner.exec(c, true, true)))
                )
                .then(addControllers.get().executes(c -> runner.exec(c, false, true)));
    }

    interface Executor {
        int exec(
                CommandContext<CommandSourceStack> ctx,
                boolean border,
                boolean controlBlocks
        ) throws CommandSyntaxException;
    }

    protected static int placeNBT(
            CommandContext<CommandSourceStack> ctx,
            boolean border,
            boolean controlBlocks
    ) throws CommandSyntaxException {
        final ResourceLocation id = ResourceLocationArgument.getId(ctx, PATH);
        final PlacementDirections searchDir = TemplatePlacementArgument.getPlacement(ctx, PLACEMENT);
        final BlockInput blockInput = border ? BlockStateArgument.getBlock(ctx, BORDER) : null;
        final StructureNBT structureNBT = StructureNBT.create(id);

        return PlaceCommand.placeBlocks(
                ctx.getSource(),
                BlockPosArgument.getLoadedBlockPos(ctx, POS),
                searchDir.getOffset(),
                blockInput,
                controlBlocks,
                structureNBT.location,
                (p) -> structureNBT.getBoundingBox(p, Rotation.NONE, Mirror.NONE),
                (level, p) -> structureNBT.generateAt(level, p, Rotation.NONE, Mirror.NONE)
        );
    }

    protected static int placeEmpty(
            CommandContext<CommandSourceStack> ctx,
            boolean border,
            boolean controlBlocks
    ) throws CommandSyntaxException {
        final ResourceLocation id = ResourceLocationArgument.getId(ctx, PATH);
        final PlacementDirections searchDir = TemplatePlacementArgument.getPlacement(ctx, PLACEMENT);
        final BlockInput blockInput = border ? BlockStateArgument.getBlock(ctx, BORDER) : null;
        final BlockPos span = Float3ArgumentType.getFloat3(ctx, SPAN).toBlockPos();

        return PlaceCommand.placeBlocks(
                ctx.getSource(),
                BlockPosArgument.getLoadedBlockPos(ctx, POS),
                searchDir == null || searchDir.dir == Float3.ZERO ? null : searchDir.dir.toBlockPos(),
                blockInput,
                controlBlocks,
                id,
                (p) -> BoundingBox.fromCorners(p, p.offset(span)),
                (level, p) -> {
                    var box = BoundingBox.fromCorners(p, p.offset(span));
                    PlaceCommand.fillStructureVoid(level, box);
                    if (blockInput != null) {
                        PlaceCommand.fill(
                                level,
                                new BoundingBox(
                                        box.minX(), box.minY() - 1, box.minZ(),
                                        box.maxX(), box.minY() - 1, box.maxZ()
                                ),
                                blockInput.getState()
                        );
                    }
                }
        );
    }
}

public class PlaceCommand {
    public PlaceCommand() {
    }

    public static LiteralArgumentBuilder<CommandSourceStack> register(
            LiteralArgumentBuilder<CommandSourceStack> bnContext,
            CommandBuildContext commandBuildContext
    ) {
        final var command = Commands
                .literal("place")
                .requires(commandSourceStack -> commandSourceStack.hasPermission(2));

        new PlaceCommandBuilder().register(commandBuildContext, command);

        return bnContext.then(command);
    }


    private static boolean isEmpty(Level level, BoundingBox bb) {
        for (int x = bb.minX(); x <= bb.maxX(); x++)
            for (int y = bb.minY(); y <= bb.maxY(); y++)
                for (int z = bb.minZ(); z <= bb.maxZ(); z++)
                    if (!level.isEmptyBlock(new BlockPos(x, y, z))) {
                        return false;
                    }

        return true;
    }

    private static void replaceAir(Level level, BoundingBox bb) {
        for (int x = bb.minX(); x <= bb.maxX(); x++)
            for (int y = bb.minY(); y <= bb.maxY(); y++)
                for (int z = bb.minZ(); z <= bb.maxZ(); z++) {
                    BlockPos bp = new BlockPos(x, y, z);
                    if (level.getBlockState(bp).is(Blocks.AIR)) {
                        level.setBlock(bp, Blocks.STRUCTURE_VOID.defaultBlockState(), BlocksHelper.SET_OBSERV);
                    }
                }
    }

    static void fill(Level level, BoundingBox bb, BlockState blockState) {
        for (int x = bb.minX(); x <= bb.maxX(); x++)
            for (int y = bb.minY(); y <= bb.maxY(); y++)
                for (int z = bb.minZ(); z <= bb.maxZ(); z++) {
                    BlockPos bp = new BlockPos(x, y, z);
                    level.setBlock(bp, blockState, BlocksHelper.SET_OBSERV);
                }
    }

    static void fillStructureVoid(Level level, BoundingBox bb) {
        fill(level, bb, Blocks.STRUCTURE_VOID.defaultBlockState());
    }

    //Draws a border around the bounding box
    private static void outline(Level level, BoundingBox bb, BlockState outlineState) {
        for (int x = bb.minX(); x <= bb.maxX(); x++) {
            level.setBlock(new BlockPos(x, bb.minY(), bb.minZ()), outlineState, BlocksHelper.SET_OBSERV);
            level.setBlock(new BlockPos(x, bb.maxY(), bb.minZ()), outlineState, BlocksHelper.SET_OBSERV);
            level.setBlock(new BlockPos(x, bb.minY(), bb.maxZ()), outlineState, BlocksHelper.SET_OBSERV);
            level.setBlock(new BlockPos(x, bb.maxY(), bb.maxZ()), outlineState, BlocksHelper.SET_OBSERV);
        }
        for (int y = bb.minY(); y <= bb.maxY(); y++) {
            level.setBlock(new BlockPos(bb.minX(), y, bb.minZ()), outlineState, BlocksHelper.SET_OBSERV);
            level.setBlock(new BlockPos(bb.maxX(), y, bb.minZ()), outlineState, BlocksHelper.SET_OBSERV);
            level.setBlock(new BlockPos(bb.minX(), y, bb.maxZ()), outlineState, BlocksHelper.SET_OBSERV);
            level.setBlock(new BlockPos(bb.maxX(), y, bb.maxZ()), outlineState, BlocksHelper.SET_OBSERV);
        }
        for (int z = bb.minZ(); z <= bb.maxZ(); z++) {
            level.setBlock(new BlockPos(bb.minX(), bb.minY(), z), outlineState, BlocksHelper.SET_OBSERV);
            level.setBlock(new BlockPos(bb.maxX(), bb.minY(), z), outlineState, BlocksHelper.SET_OBSERV);
            level.setBlock(new BlockPos(bb.minX(), bb.maxY(), z), outlineState, BlocksHelper.SET_OBSERV);
            level.setBlock(new BlockPos(bb.maxX(), bb.maxY(), z), outlineState, BlocksHelper.SET_OBSERV);
        }
    }

    private static BoundingBox adapt(BoundingBox bb, boolean border, boolean structureBlock) {
        if (border) {
            return bb.inflatedBy(1);
        } else if (structureBlock) {
            return new BoundingBox(
                    bb.minX() - 1,
                    bb.minY() - 1,
                    bb.minZ() - 1,
                    bb.maxX(),
                    bb.maxY(),
                    bb.maxZ()
            );
        }
        return bb;
    }

    private static void createControlBlocks(CommandSourceStack stack, ResourceLocation location, BoundingBox bbNBT) {
        BlockPos structureBlockPos = new BlockPos(bbNBT.minX() - 1, bbNBT.minY() - 1, bbNBT.minZ() - 1);
        BlockPos commandBlockPos = new BlockPos(bbNBT.minX() - 1, bbNBT.minY() - 1, bbNBT.minZ());
        BlockPos buttonBlockPos = new BlockPos(bbNBT.minX() - 1, bbNBT.minY(), bbNBT.minZ());
        BlockState state = Blocks.STRUCTURE_BLOCK
                .defaultBlockState()
                .setValue(StructureBlock.MODE, StructureMode.SAVE);

        stack.getLevel().setBlock(structureBlockPos, state, BlocksHelper.SET_OBSERV);
        if (stack.getLevel().getBlockEntity(structureBlockPos) instanceof StructureBlockEntity entity) {
            entity.setIgnoreEntities(false);
            entity.setShowAir(false);
            entity.setMirror(Mirror.NONE);
            entity.setRotation(Rotation.NONE);
            entity.setShowBoundingBox(true);
            entity.setStructureName(location);
            entity.setStructurePos(new BlockPos(1, 1, 1));
            entity.setStructureSize(new Vec3i(bbNBT.getXSpan(), bbNBT.getYSpan(), bbNBT.getZSpan()));
        }

        state = Blocks.COMMAND_BLOCK.defaultBlockState().setValue(CommandBlock.FACING, Direction.DOWN);
        stack.getLevel().setBlock(commandBlockPos, state, BlocksHelper.SET_OBSERV);
        if (stack.getLevel().getBlockEntity(commandBlockPos) instanceof CommandBlockEntity entity) {
            entity.setAutomatic(false);
            entity.setPowered(false);
            entity.onlyOpCanSetNbt();
            entity.getCommandBlock().shouldInformAdmins();
            entity.getCommandBlock()
                  .setCommand(
                          "fill ~1 ~1 ~"
                                  + " ~" + bbNBT.getXSpan() + " ~" + bbNBT.getYSpan() + " ~" + bbNBT.getZSpan()
                                  + " " + BuiltInRegistries.BLOCK.getKey(Blocks.STRUCTURE_VOID)
                                  + " replace " + BuiltInRegistries.BLOCK.getKey(Blocks.AIR)
                  );
        }

        state = Blocks.OAK_BUTTON.defaultBlockState()
                                 .setValue(ButtonBlock.FACING, Direction.EAST)
                                 .setValue(ButtonBlock.FACE, AttachFace.FLOOR);
        stack.getLevel().setBlock(buttonBlockPos, state, BlocksHelper.SET_OBSERV);
    }


    static int placeBlocks(
            CommandSourceStack stack,
            BlockPos pos,
            BlockPos searchDir,
            BlockInput blockInput,
            boolean structureBlock,
            ResourceLocation location,
            Function<BlockPos, BoundingBox> getBounds,
            BiConsumer<ServerLevel, BlockPos> generate
    ) {
        if (searchDir != null) {
            int tries = 16 * 16;
            while (tries > 0 && !isEmpty(
                    stack.getLevel(),
                    adapt(
                            getBounds.apply(pos),
                            blockInput != null,
                            structureBlock
                    )
            )) {

                pos = pos.offset(searchDir);
                tries--;
            }

            if (tries <= 0) {
                stack.sendFailure(Component.literal("Failed to find free space")
                                           .setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));
                return -1;
            }
        }

        final BoundingBox bbNBT = getBounds.apply(pos);
        if (blockInput != null) {
            final BoundingBox bb = bbNBT.inflatedBy(1);
            outline(stack.getLevel(), bb, blockInput.getState());
            stack.sendSuccess(() -> Component.literal("Placed border: " + bb.toString())
                                             .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)), true);
        }

        generate.accept(stack.getLevel(), pos);
        replaceAir(stack.getLevel(), bbNBT);

        stack.sendSuccess(() -> Component.literal("Placed NBT: " + bbNBT.toString())
                                         .setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)), true);

        if (structureBlock) {
            createControlBlocks(stack, location, bbNBT);
        }
        return Command.SINGLE_SUCCESS;
    }

}

/*

/bclib place nbt "betternether:city/city_building_01" find northOf 0 -59 0 border glass structureblock
/bclib place nbt "betternether:city/city_center_04" find northOf 32 -59 0 border glass structureblock
/bclib place nbt "betternether:city/city_enchanter_02" find northOf 32 -59 0 border glass structureblock
/bclib place nbt "betternether:city/city_library_03" find northOf 64 -59 0 border glass structureblock
/bclib place nbt "betternether:city/city_park_02" find northOf 64 -59 0 border glass structureblock
/bclib place nbt "betternether:city/city_tower_04" find northOf 96 -59 0 border glass structureblock
/bclib place nbt "betternether:city/road_end_02" find northOf 96 -59 0 border glass structureblock
 */
