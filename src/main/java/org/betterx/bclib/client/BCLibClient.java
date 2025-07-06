package org.betterx.bclib.client;

import org.betterx.bclib.api.v2.ModIntegrationAPI;
import org.betterx.bclib.api.v2.PostInitAPI;
import org.betterx.bclib.api.v2.dataexchange.DataExchangeAPI;
import org.betterx.bclib.client.models.CustomModelBakery;
import org.betterx.bclib.client.textures.AtlasSetManager;
import org.betterx.bclib.client.textures.SpriteLister;
import org.betterx.bclib.registry.BaseBlockEntityRenders;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.model.loading.v1.ModelLoadingPlugin;

public class BCLibClient implements ClientModInitializer {
    private static CustomModelBakery modelBakery;

    public static CustomModelBakery lazyModelbakery() {
        if (modelBakery == null) {
            modelBakery = new CustomModelBakery();
        }
        return modelBakery;
    }

    @Override
    public void onInitializeClient() {
        modelBakery = new CustomModelBakery();

        ModIntegrationAPI.registerAll();
        BaseBlockEntityRenders.register();
        DataExchangeAPI.prepareClientside();
        PostInitAPI.postInit(true);
        ModelLoadingPlugin.register(BCLibClient::onInitializeModelLoader);

        AtlasSetManager.addSource(AtlasSetManager.VANILLA_BLOCKS, new SpriteLister("entity/chest"));
        AtlasSetManager.addSource(AtlasSetManager.VANILLA_BLOCKS, new SpriteLister("blocks"));
    }


    private static void onInitializeModelLoader(ModelLoadingPlugin.Context pluginContext) {
        modelBakery.registerBlockStateResolvers(pluginContext);

        //pluginContext.modifyModelOnLoad().register(BCLibClient::resolveModel);
//        pluginContext.modifyModelOnLoad().register(BCLibClient::modifyModelOnLoad);
    }

//    private static UnbakedModel resolveModel(UnbakedModel unbakedModel, ModelModifier.OnLoad.Context context) {
//        boolean isItem = context.id().getPath().startsWith("item/");

    /// /        if (ctx.id() instanceof ResourceLocation modelId && modelId.getVariant().equals("inventory")) {
    /// /            isItem = true;
    /// /        }
//
//        return isItem ? modelBakery.getItemModel(context.id()) : modelBakery.getBlockModel(context.id());
//    }
//    private static UnbakedModel modifyModelOnLoad(UnbakedModel model, ModelModifier.OnLoad.Context ctx) {
//        UnbakedModel res = null;
//        if (ctx.topLevelId() != null) {
//            res = ctx.topLevelId().getVariant().equals("inventory")
//                    ? modelBakery.getItemModel(ctx.topLevelId().id())
//                    : modelBakery.getBlockModel(ctx.topLevelId().id());
//        } else if (ctx.resourceId() != null) {
//            res = modelBakery.getBlockModel(ctx.resourceId());
//        }
//
//
//        if (res == null)
//            return model;
//        return res;
//    }
//    @Override
//    public @Nullable UnbakedModel loadModelResource(
//            ResourceLocation resourceId,
//            ModelProviderContext context
//    ) throws ModelProviderException {
//        return modelBakery.getBlockModel(resourceId);
//    }
//
//    @Override
//    public @Nullable UnbakedModel loadModelVariant(
//            ResourceLocation modelId,
//            ModelProviderContext context
//    ) throws ModelProviderException {
//        return modelId.getVariant().equals("inventory")
//                ? modelBakery.getItemModel(modelId)
//                : modelBakery.getBlockModel(modelId);
//    }


}
