package org.betterx.bclib.client.render;

import org.betterx.bclib.blockentities.BaseSignBlockEntity;
import org.betterx.bclib.blocks.BaseSignBlock;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.blockentity.SignRenderer;
import net.minecraft.client.resources.model.Material;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SignBlock;
import net.minecraft.world.level.block.StandingSignBlock;
import net.minecraft.world.level.block.entity.SignText;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.phys.Vec3;

import com.google.common.collect.Maps;

import java.util.HashMap;
import java.util.List;

public class BaseSignBlockEntityRenderer implements BlockEntityRenderer<BaseSignBlockEntity> {
    private static final HashMap<Block, RenderType> RENDER_TYPES = Maps.newHashMap();
    private static final int OUTLINE_RENDER_DISTANCE = Mth.square(16);
    private static final RenderType RENDER_TYPE;
    private final SignRenderer.SignModel model;
    private final Font font;


    public BaseSignBlockEntityRenderer(BlockEntityRendererProvider.Context ctx) {
        super();
        this.font = ctx.getFont();
        model = new SignRenderer.SignModel(ctx.bakeLayer(ModelLayers.createSignModelName(WoodType.OAK)));
    }

    public void render(
            BaseSignBlockEntity signBlockEntity,
            float tickDelta,
            PoseStack matrixStack,
            MultiBufferSource provider,
            int light,
            int overlay
    ) {
        final SignText frontText = signBlockEntity.getFrontText();
        BlockState state = signBlockEntity.getBlockState();

        matrixStack.pushPose();


        matrixStack.translate(0.5D, 0.5D, 0.5D);
        float angle = -((float) (state.getValue(StandingSignBlock.ROTATION) * 360) / 16.0F);

        BlockState blockState = signBlockEntity.getBlockState();
        if (blockState.getValue(BaseSignBlock.FLOOR)) {
            matrixStack.mulPose(Axis.YP.rotationDegrees(angle));
            model.stick.visible = true;
        } else {
            matrixStack.mulPose(Axis.YP.rotationDegrees(angle + 180));
            matrixStack.translate(0.0D, -0.3125D, -0.4375D);
            model.stick.visible = false;
        }

        matrixStack.pushPose();
        matrixStack.scale(0.6666667F, -0.6666667F, -0.6666667F);
        VertexConsumer vertexConsumer = getConsumer(provider, state.getBlock());

        model.root.render(matrixStack, vertexConsumer, light, overlay);
        matrixStack.popPose();
        matrixStack.translate(0.0D, 0.3333333432674408D, 0.046666666865348816D);
        matrixStack.scale(0.010416667F, -0.010416667F, 0.010416667F);
        int m = frontText.getColor().getTextColor();
        int n = (int) (FastColor.ARGB32.red(m) * 0.4D);
        int o = (int) (FastColor.ARGB32.green(m) * 0.4D);
        int p = (int) (FastColor.ARGB32.blue(m) * 0.4D);
        int q = FastColor.ARGB32.color(0, p, o, n);

        FormattedCharSequence[] formattedCharSequences = frontText.getRenderMessages(
                Minecraft.getInstance()
                         .isTextFilteringEnabled(),
                (component) -> {
                    List<FormattedCharSequence> list = this.font.split(component, 90);
                    return list.isEmpty() ? FormattedCharSequence.EMPTY : list.get(0);
                }
        );
        int drawColor;
        boolean drawOutlined;
        int drawLight;
        if (frontText.hasGlowingText()) {
            drawColor = frontText.getColor().getTextColor();
            drawOutlined = isOutlineVisible(signBlockEntity, drawColor);
            drawLight = 15728880;
        } else {
            drawColor = m;
            drawOutlined = false;
            drawLight = light;
        }

        for (int s = 0; s < 4; ++s) {
            FormattedCharSequence formattedCharSequence = formattedCharSequences[s];
            float t = (float) (-this.font.width(formattedCharSequence) / 2);
            int marginHeight = 4 * signBlockEntity.getTextLineHeight() / 2;
            if (drawOutlined) {
                this.font.drawInBatch8xOutline(
                        formattedCharSequence,
                        t,
                        (float) (s * signBlockEntity.getTextLineHeight() - marginHeight),
                        drawColor,
                        m,
                        matrixStack.last().pose(),
                        provider,
                        drawLight
                );
            } else {
                this.font.drawInBatch(
                        formattedCharSequence,
                        t,
                        (float) (s * signBlockEntity.getTextLineHeight() - marginHeight),
                        drawColor,
                        false,
                        matrixStack.last().pose(),
                        provider,
                        Font.DisplayMode.NORMAL,
                        0,
                        drawLight
                );
            }
        }


        matrixStack.popPose();
    }


    private static boolean isOutlineVisible(BaseSignBlockEntity signBlockEntity, int i) {
        if (i == DyeColor.BLACK.getTextColor()) {
            return true;
        } else {
            Minecraft minecraft = Minecraft.getInstance();
            LocalPlayer localPlayer = minecraft.player;
            if (localPlayer != null && minecraft.options.getCameraType().isFirstPerson() && localPlayer.isScoping()) {
                return true;
            } else {
                Entity entity = minecraft.getCameraEntity();
                return entity != null && entity.distanceToSqr(Vec3.atCenterOf(signBlockEntity.getBlockPos())) < (double) OUTLINE_RENDER_DISTANCE;
            }
        }
    }

    public static WoodType getSignType(Block block) {
        WoodType signType2;
        if (block instanceof SignBlock) {
            signType2 = ((SignBlock) block).type();
        } else {
            signType2 = WoodType.OAK;
        }

        return signType2;
    }

    public static Material getModelTexture(Block block) {
        return Sheets.getSignMaterial(getSignType(block));
    }

    public static VertexConsumer getConsumer(MultiBufferSource provider, Block block) {
        return provider.getBuffer(RENDER_TYPES.getOrDefault(block, RENDER_TYPE));
    }

    public static void registerRenderLayer(Block block) {
        ResourceLocation blockId = BuiltInRegistries.BLOCK.getKey(block);
        RenderType layer = RenderType.entitySolid(new ResourceLocation(
                blockId.getNamespace(),
                "textures/entity/sign/" + blockId.getPath() + ".png"
        ));
        RENDER_TYPES.put(block, layer);
    }

    static {
        RENDER_TYPE = RenderType.entitySolid(new ResourceLocation("textures/entity/signs/oak.png"));
    }
}
