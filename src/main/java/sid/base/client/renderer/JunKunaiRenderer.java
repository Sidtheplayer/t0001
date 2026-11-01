package sid.base.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import sid.base.client.model.kunai_model;
import sid.base.network.ClientSideDelegations;
import sid.base.world.entity.JunKunaiEntity;

public class JunKunaiRenderer extends EntityRenderer<JunKunaiEntity> {

    private final kunai_model model;

    public JunKunaiRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new kunai_model(context.bakeLayer(kunai_model.LAYER_LOCATION));
    }

    @Override
    public void render(@NotNull JunKunaiEntity kunai, float entityYaw, float partialTick, @NotNull PoseStack poseStack, @NotNull MultiBufferSource bufferSource, int packedLight) {
        super.render(kunai, entityYaw, partialTick, poseStack, bufferSource, packedLight);

        if(kunai.level().isClientSide){
            ClientSideDelegations.tickJunKunai(kunai, partialTick);
        }

    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull JunKunaiEntity junKunaiEntity) {
        return ResourceLocation.parse("null");
    }
}
