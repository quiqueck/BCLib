package org.betterx.bclib.client.models;

import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class BaseChestBlockModel {
    public final ModelPart partA;
    public final ModelPart partC;
    public final ModelPart partB;
    public final ModelPart partRightA;
    public final ModelPart partRightC;
    public final ModelPart partRightB;
    public final ModelPart partLeftA;
    public final ModelPart partLeftC;
    public final ModelPart partLeftB;

    public static LayerDefinition getTexturedModelData() {
        MeshDefinition modelData = new MeshDefinition();
        PartDefinition modelPartData = modelData.getRoot();
        CubeDeformation deformation_partC = new CubeDeformation(0.0f);
        modelPartData.addOrReplaceChild(
                "partC",
                CubeListBuilder.create().texOffs(0, 19).addBox(1.0f, 0.0f, 1.0f, 14.0f, 9.0f, 14.0f, deformation_partC),
                PartPose.ZERO
        );

        CubeDeformation deformation_partA = new CubeDeformation(0.0f);
        modelPartData.addOrReplaceChild(
                "partA",
                CubeListBuilder.create().texOffs(0, 0).addBox(1.0f, 0.0f, 0.0f, 14.0f, 5.0f, 14.0f, deformation_partA),
                PartPose.offset(0.0f, 9.0f, 1.0f)
        );

        CubeDeformation deformation_partB = new CubeDeformation(0.0f);
        modelPartData.addOrReplaceChild(
                "partB",
                CubeListBuilder.create().texOffs(0, 0).addBox(7.0f, -1.0f, 15.0f, 2.0f, 4.0f, 1.0f, deformation_partB),
                PartPose.offset(0.0f, 8.0f, 0.0f)
        );

        CubeDeformation deformation_partRightC = new CubeDeformation(0.0f);
        modelPartData.addOrReplaceChild(
                "partRightC",
                CubeListBuilder.create()
                               .texOffs(0, 19)
                               .addBox(1.0f, 0.0f, 1.0f, 15.0f, 9.0f, 14.0f, deformation_partRightC),
                PartPose.ZERO
        );

        CubeDeformation deformation_partRightA = new CubeDeformation(0.0f);
        modelPartData.addOrReplaceChild(
                "partRightA",
                CubeListBuilder.create()
                               .texOffs(0, 0)
                               .addBox(1.0f, 0.0f, 0.0f, 15.0f, 5.0f, 14.0f, deformation_partRightA),
                PartPose.offset(0.0f, 9.0f, 1.0f)
        );

        CubeDeformation deformation_partRightB = new CubeDeformation(0.0f);
        PartDefinition partRightB = modelPartData.addOrReplaceChild(
                "partRightB",
                CubeListBuilder.create()
                               .texOffs(0, 0)
                               .addBox(15.0f, -1.0f, 15.0f, 1.0f, 4.0f, 1.0f, deformation_partRightB),
                PartPose.offset(0.0f, 8.0f, 0.0f)
        );

        CubeDeformation deformation_partLeftC = new CubeDeformation(0.0f);
        modelPartData.addOrReplaceChild(
                "partLeftC",
                CubeListBuilder.create()
                               .texOffs(0, 19)
                               .addBox(0.0f, 0.0f, 1.0f, 15.0f, 9.0f, 14.0f, deformation_partLeftC),
                PartPose.ZERO
        );

        CubeDeformation deformation_partLeftA = new CubeDeformation(0.0f);
        modelPartData.addOrReplaceChild(
                "partLeftA",
                CubeListBuilder.create()
                               .texOffs(0, 0)
                               .addBox(0.0f, 0.0f, 0.0f, 15.0f, 5.0f, 14.0f, deformation_partLeftA),
                PartPose.offset(0.0f, 9.0f, 1.0f)
        );

        CubeDeformation deformation_partLeftB = new CubeDeformation(0.0f);
        modelPartData.addOrReplaceChild(
                "partLeftB",
                CubeListBuilder.create()
                               .texOffs(0, 0)
                               .addBox(0.0f, -1.0f, 15.0f, 1.0f, 4.0f, 1.0f, deformation_partLeftB),
                PartPose.offset(0.0f, 8.0f, 0.0f)
        );

        return LayerDefinition.create(modelData, 64, 64);
    }

    public BaseChestBlockModel(ModelPart modelPart) {
        super();

        partC = modelPart.getChild("partC");
        partA = modelPart.getChild("partA");
        partB = modelPart.getChild("partB");
        partRightC = modelPart.getChild("partRightC");
        partRightA = modelPart.getChild("partRightA");
        partRightB = modelPart.getChild("partRightB");
        partLeftC = modelPart.getChild("partLeftC");
        partLeftA = modelPart.getChild("partLeftA");
        partLeftB = modelPart.getChild("partLeftB");
    }
}
