package sid.base.client.renderer;

import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;
import sid.base.client.model.AmogusModel;
import sid.base.world.entity.Amogus;

//you don't need much
public class NAmogusRenderer extends MobRenderer<Amogus, AmogusModel> {
    private static final ResourceLocation TEXTURE_LOCATION =  ResourceLocation.fromNamespaceAndPath("t0001", "textures/entity/amogus.png");

    public NAmogusRenderer(EntityRendererProvider.Context context) {
        super(context, new AmogusModel(context.bakeLayer(AmogusModel.LAYER_LOCATION)), 0.5F);
        this.addLayer(new ItemInHandLayer<>(this, context.getItemInHandRenderer()));
    }


    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull Amogus pEntity) {
        return TEXTURE_LOCATION;
    }
}
