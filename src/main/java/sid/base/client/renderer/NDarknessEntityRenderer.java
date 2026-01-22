package sid.base.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import sid.base.client.model.darkness;
import sid.base.world.entity.DarknessEntity;


public class NDarknessEntityRenderer extends MobRenderer<DarknessEntity, darkness> {
    private static final ResourceLocation TEXTURE_LOCATION =  ResourceLocation.fromNamespaceAndPath("t0001", "textures/entity/darkness_entity.png");

    public NDarknessEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new darkness(context.bakeLayer(darkness.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull DarknessEntity pEntity) {
        return TEXTURE_LOCATION;
    }


}
