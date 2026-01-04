package sid.t0001.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import sid.t0001.client.model.darkness;
import sid.t0001.world.entity.DarknessEntity;

@SuppressWarnings("removal")
public class NDarknessEntityRenderer extends MobRenderer<DarknessEntity, darkness> {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("t0001", "textures/entity/darkness_entity.png");

    public NDarknessEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new darkness(context.bakeLayer(darkness.LAYER_LOCATION)), 0.5F);
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull DarknessEntity pEntity) {
        return TEXTURE_LOCATION;
    }


}
