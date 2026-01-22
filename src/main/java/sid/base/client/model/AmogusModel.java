package sid.base.client.model;

// Made with Blockbench 4.12.6
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.HumanoidArm;
import org.jetbrains.annotations.NotNull;
import sid.base.world.entity.Amogus;

@SuppressWarnings({"FieldCanBeLocal", "unused"})
public class AmogusModel extends EntityModel<Amogus> implements HeadedModel, ArmedModel {
    public static final ModelLayerLocation LAYER_LOCATION =
            new ModelLayerLocation(ResourceLocation.fromNamespaceAndPath("t0001", "amogus"), "main");

    private final ModelPart Root;
    private final ModelPart Chest;
    private final ModelPart Leg_R;
    private final ModelPart Leg_L;

    public AmogusModel(ModelPart root) {
        this.Root = root.getChild("Root");
        this.Chest = this.Root.getChild("Chest");
        this.Leg_R = this.Root.getChild("Leg_R");
        this.Leg_L = this.Root.getChild("Leg_L");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Root = partdefinition.addOrReplaceChild("Root",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition Chest = Root.addOrReplaceChild("Chest",
                CubeListBuilder.create()
                        .texOffs(0, 0).addBox(-2.95F, -1.0F, -0.725F,
                                4.0F, 8.0F, 6.0F, new CubeDeformation(-1.2F))
                        .texOffs(0, 0).addBox(-0.15F, -0.8F, -0.725F,
                                4.0F, 8.0F, 6.0F, new CubeDeformation(0.0F))
                        .texOffs(0, 15).addBox(3.85F, 0.2F, 0.275F,
                                1.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-4.85F, -3.2F, -0.275F));

        PartDefinition Leg_R = Root.addOrReplaceChild("Leg_R",
                CubeListBuilder.create()
                        .texOffs(11, 15).addBox(-1.95F, 4.0F, 1.275F,
                                2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-2.05F, 0.0F, -1.6F));

        PartDefinition Leg_L = Root.addOrReplaceChild("Leg_L",
                CubeListBuilder.create()
                        .texOffs(20, 15).addBox(-1.95F, 4.0F, 4.05F,
                                2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)),
                PartPose.offset(-2.05F, 0.0F, -1.6F));

        return LayerDefinition.create(meshdefinition, 32, 32);
    }



    @Override
    public @NotNull ModelPart getHead() {
        return this.Chest;
    }

    @Override
    public void setupAnim(@NotNull Amogus entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) { // Animation logic here if needed
    }

    @Override
    public void translateToHand(@NotNull HumanoidArm humanoidArm, @NotNull PoseStack poseStack) {
        this.Chest.translateAndRotate(poseStack);
    }

    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack, @NotNull VertexConsumer vertexConsumer, int i, int i1, int i2) {
        Root.render(poseStack,vertexConsumer,i,i1,i2);

    }
}
