package sid.base.client.renderer;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import sid.base.client.model.kunai_model;
import sid.base.world.entity.JunKunaiEntity;

public class JunKunaiRenderer extends EntityRenderer<JunKunaiEntity> {

    private kunai_model model;

    public JunKunaiRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new kunai_model(context.bakeLayer(kunai_model.LAYER_LOCATION));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull JunKunaiEntity junKunaiEntity) {
        return ResourceLocation.parse("null");
    }
}
