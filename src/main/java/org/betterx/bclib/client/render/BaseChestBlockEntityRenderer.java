package org.betterx.bclib.client.render;

import org.betterx.bclib.blockentities.BaseChestBlockEntity;
import org.betterx.bclib.client.models.BaseChestBlockModel;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.BrightnessCombiner;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.DoubleBlockCombiner.NeighborCombineResult;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;

import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;

import com.google.common.collect.Maps;
import it.unimi.dsi.fastutil.ints.Int2IntFunction;

import java.util.HashMap;

@Environment(EnvType.CLIENT)
public class BaseChestBlockEntityRenderer implements BlockEntityRenderer<BaseChestBlockEntity> {
    private static final HashMap<Block, RenderType[]> LAYERS = Maps.newHashMap();
    private static final RenderType[] RENDER_TYPES;

    private static final int ID_NORMAL = 0;
    private static final int ID_LEFT = 1;
    private static final int ID_RIGHT = 2;

    private final BaseChestBlockModel chestModel;

    public BaseChestBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        super();
        chestModel = new BaseChestBlockModel(BaseChestBlockModel.getTexturedModelData().bakeRoot());
    }

    public void render(
            BaseChestBlockEntity entity,
            float tickDelta,
            PoseStack matrices,
            MultiBufferSource vertexConsumers,
            int light,
            int overlay
    ) {
        Level world = entity.getLevel();
        boolean worldExists = world != null;
        BlockState blockState = worldExists ? entity.getBlockState() : Blocks.CHEST.defaultBlockState()
                                                                                   .setValue(
                                                                                           ChestBlock.FACING,
                                                                                           Direction.SOUTH
                                                                                   );
        ChestType chestType = blockState.hasProperty(ChestBlock.TYPE)
                ? blockState.getValue(ChestBlock.TYPE)
                : ChestType.SINGLE;
        Block block = blockState.getBlock();
        if (block instanceof AbstractChestBlock) {
            AbstractChestBlock<?> abstractChestBlock = (AbstractChestBlock<?>) block;
            boolean isDouble = chestType != ChestType.SINGLE;
            float f = blockState.getValue(ChestBlock.FACING).toYRot();
            NeighborCombineResult<? extends ChestBlockEntity> propertySource;

            matrices.pushPose();
            matrices.translate(0.5D, 0.5D, 0.5D);
            matrices.mulPose(Axis.YP.rotationDegrees(-f));
            matrices.translate(-0.5D, -0.5D, -0.5D);

            if (worldExists) {
                propertySource = abstractChestBlock.combine(blockState, world, entity.getBlockPos(), true);
            } else {
                propertySource = DoubleBlockCombiner.Combiner::acceptNone;
            }

            float pitch = propertySource.apply(ChestBlock.opennessCombiner(entity)).get(
                    tickDelta);
            pitch = 1.0F - pitch;
            pitch = 1.0F - pitch * pitch * pitch;
            @SuppressWarnings({
                    "unchecked",
                    "rawtypes"
            }) int blockLight = ((Int2IntFunction) propertySource.apply(new BrightnessCombiner())).applyAsInt(light);

            VertexConsumer vertexConsumer = getConsumer(vertexConsumers, block, chestType);

            if (isDouble) {
                if (chestType == ChestType.LEFT) {
                    renderParts(
                            matrices,
                            vertexConsumer,
                            chestModel.partLeftA,
                            chestModel.partLeftB,
                            chestModel.partLeftC,
                            pitch,
                            blockLight,
                            overlay
                    );
                } else {
                    renderParts(
                            matrices,
                            vertexConsumer,
                            chestModel.partRightA,
                            chestModel.partRightB,
                            chestModel.partRightC,
                            pitch,
                            blockLight,
                            overlay
                    );
                }
            } else {
                renderParts(
                        matrices,
                        vertexConsumer,
                        chestModel.partA,
                        chestModel.partB,
                        chestModel.partC,
                        pitch,
                        blockLight,
                        overlay
                );
            }

            matrices.popPose();
        }
    }

    private void renderParts(
            PoseStack matrices,
            VertexConsumer vertices,
            ModelPart modelPart,
            ModelPart modelPart2,
            ModelPart modelPart3,
            float pitch,
            int light,
            int overlay
    ) {
        modelPart.xRot = -(pitch * 1.5707964F);
        modelPart2.xRot = modelPart.xRot;
        modelPart.render(matrices, vertices, light, overlay);
        modelPart2.render(matrices, vertices, light, overlay);
        modelPart3.render(matrices, vertices, light, overlay);
    }

    private static RenderType getChestTexture(ChestType type, RenderType[] layers) {
        return switch (type) {
            case LEFT -> layers[ID_LEFT];
            case RIGHT -> layers[ID_RIGHT];
            default -> layers[ID_NORMAL];
        };
    }

    public static VertexConsumer getConsumer(MultiBufferSource provider, Block block, ChestType chestType) {
        RenderType[] layers = LAYERS.getOrDefault(block, RENDER_TYPES);
        return provider.getBuffer(getChestTexture(chestType, layers));
    }

    public static void registerRenderLayer(Block block) {
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
        String modId = blockId.getNamespace();
        String path = blockId.getPath();
        LAYERS.put(
                block,
                new RenderType[]{
                        RenderType.entityCutout(new ResourceLocation(modId, "textures/entity/chest/" + path + ".png")),
                        RenderType.entityCutout(new ResourceLocation(
                                modId,
                                "textures/entity/chest/" + path + "_left.png"
                        )),
                        RenderType.entityCutout(new ResourceLocation(
                                modId,
                                "textures/entity/chest/" + path + "_right.png"
                        ))
                }
        );
    }

    static {
        RENDER_TYPES = new RenderType[]{
                RenderType.entityCutout(new ResourceLocation("textures/entity/chest/normal.png")),
                RenderType.entityCutout(new ResourceLocation("textures/entity/chest/normal_left.png")),
                RenderType.entityCutout(new ResourceLocation("textures/entity/chest/normal_right.png"))
        };
    }
}
