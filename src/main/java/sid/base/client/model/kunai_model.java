package sid.base.client.model;


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
import sid.base.main.t0001;
import sid.base.world.entity.JunKunaiEntity;

// Doesn't do shit because the body is going to be rendered with Photon Mod Instead

public  class kunai_model extends EntityModel<JunKunaiEntity> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation( ResourceLocation.fromNamespaceAndPath(t0001.MODID, "jun_kunai"), "main");

    private final ModelPart Root;
	public kunai_model(ModelPart Root) {
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
    public void setupAnim(@NotNull JunKunaiEntity pEntity, float pLimbSwing, float pLimbSwingAmount, float pAgeInTicks, float pNetHeadYaw, float pHeadPitch) {

    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, int color) {
        Root.render(poseStack,vertexConsumer,packedLight,packedOverlay,color);

    }
}