package sid.t0001.client.model;
// Made with Blockbench 5.0.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import org.xame.t0001;
import sid.t0001.world.entity.DarknessEntity;


public class darkness extends EntityModel<DarknessEntity> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation( ResourceLocation.fromNamespaceAndPath(t0001.MODID, "darkness_entity"), "main");

    private final ModelPart Root;
	public darkness(ModelPart Root) {
        this.Root = Root.getChild("Root");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition Root = partdefinition.addOrReplaceChild("Root",
                CubeListBuilder.create(),
                PartPose.offset(0.0F, 24.0F, 0.0F));




        return LayerDefinition.create(meshdefinition, 16, 16);
	}


    @Override
    public void renderToBuffer(@NotNull PoseStack poseStack,
                               @NotNull VertexConsumer vertexConsumer,
                               int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        Root.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public void setupAnim(@NotNull DarknessEntity pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {

    }
}