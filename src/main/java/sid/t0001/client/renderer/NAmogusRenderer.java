package sid.t0001.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import sid.t0001.client.model.AmogusModel;
import sid.t0001.world.entity.Amogus;

public class NAmogusRenderer extends MobRenderer<Amogus, AmogusModel> {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation("t0001", "textures/entity/amogus.png");

    public NAmogusRenderer(EntityRendererProvider.Context p_174362_) {
        super(p_174362_, new AmogusModel(p_174362_.bakeLayer(AmogusModel.LAYER_LOCATION)), 0.8F);
    }


    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull Amogus pEntity) {
        return TEXTURE_LOCATION;
    }
}
