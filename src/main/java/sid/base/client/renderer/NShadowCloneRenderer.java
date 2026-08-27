package sid.base.client.renderer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import org.jetbrains.annotations.NotNull;
import sid.base.world.entity.ShadowCloneEntity;

public class NShadowCloneRenderer extends HumanoidMobRenderer<ShadowCloneEntity, HumanoidModel<ShadowCloneEntity>> {

    public NShadowCloneRenderer(EntityRendererProvider.Context context) {
        super(context, new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER)), 0.5f);
        this.addLayer(new HumanoidArmorLayer<>(
                this,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_INNER_ARMOR)),
                new HumanoidModel<>(context.bakeLayer(ModelLayers.PLAYER_OUTER_ARMOR)),
                context.getModelManager()));
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull ShadowCloneEntity shadowCloneEntity) {
        if(Minecraft.getInstance().level != null){

            Entity owner = shadowCloneEntity.getOwner();

            if(owner instanceof AbstractClientPlayer abstractClientPlayer){
                return abstractClientPlayer.getSkin().texture();
            }

            if(owner != null){
                EntityRenderer<?> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(owner);
                if(renderer instanceof HumanoidMobRenderer<?,?>){
                    return Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(owner).getTextureLocation(owner);
                }
            }
        }

        return  ResourceLocation.parse("minecraft:textures/entity/player/steve.png");


    }


}
