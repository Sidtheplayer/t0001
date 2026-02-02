package sid.base.client.renderer.weapon;

import com.google.gson.JsonElement;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import sid.base.gameasset.animations.DragonGodSwordAnimations;
import sid.base.world.item.t0001Items;
import yesman.epicfight.api.animation.types.DynamicAnimation;
import yesman.epicfight.api.animation.types.StaticAnimation;
import yesman.epicfight.api.utils.math.MathUtils;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.renderer.patched.item.RenderItemBase;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.*;

public class DragonGodSwordRenderer extends RenderItemBase {
    private final ItemStack sheathStack;
    private final ItemStack sheathStack2;
    private final ItemStack air = Items.AIR.getDefaultInstance();

    public DragonGodSwordRenderer(JsonElement jsonElement) {
        super(jsonElement);

        if (jsonElement.getAsJsonObject().has("sheath")) {
            this.sheathStack = new ItemStack(Objects.requireNonNull(BuiltInRegistries.ITEM.get(ResourceLocation.parse(jsonElement.getAsJsonObject().get("sheath").getAsString()))));
        } else {
            this.sheathStack = new ItemStack(t0001Items.DRAGON_GOD_SWORD_SHEATH.get());
        }

        if (jsonElement.getAsJsonObject().has("sheathed")) {
            this.sheathStack2 = new ItemStack(Objects.requireNonNull(BuiltInRegistries.ITEM.get(ResourceLocation.parse(jsonElement.getAsJsonObject().get("sheath").getAsString()))));
        } else {
            this.sheathStack2 = new ItemStack(t0001Items.DRAGON_GOD_SWORD_SHEATHED.get());
        }
    }

    @Override
    public void renderItemInHand(ItemStack stack, LivingEntityPatch<?> entitypatch, InteractionHand hand, OpenMatrix4f[] poses, MultiBufferSource buffer, PoseStack poseStack, int packedLight, float partialTicks) {
        super.renderItemInHand(stack, entitypatch, hand, poses, buffer, poseStack, packedLight, partialTicks);
        DynamicAnimation current_animation = Objects.requireNonNull(entitypatch.getAnimator().getPlayerFor(null)).getAnimation().get();
//        Map<LivingMotion, AssetAccessor<? extends StaticAnimation>> motionAssetAccessorMap = entitypatch.getAnimator().getLivingAnimations();
//       boolean is_living_motion =
//               motionAssetAccessorMap.values().stream().map(AssetAccessor::get).anyMatch(staticAnimation -> staticAnimation.getId() ==
//                current_animation.getId());
        Set<StaticAnimation> Predications = Set.of(
                DragonGodSwordAnimations.DGS_IDLE.get(),
                DragonGodSwordAnimations.DGS_RUN.get()
        );

        Set<StaticAnimation> AttackPredication = Set.of(
                DragonGodSwordAnimations.DGS_AUTO_1.get(),
                DragonGodSwordAnimations.DGS_AUTO_2.get(),
                DragonGodSwordAnimations.DGS_AUTO_1P2.get()
        );



        OpenMatrix4f modelMatrix = this.getCorrectionMatrix(entitypatch, InteractionHand.MAIN_HAND, poses);
        poseStack.pushPose();
        MathUtils.mulStack(poseStack, modelMatrix);
        itemRenderer.renderStatic(stack, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, null, 0);
        poseStack.popPose();


        OpenMatrix4f modelMatrix_offhand = this.getCorrectionMatrix(entitypatch, InteractionHand.OFF_HAND, poses);

        ItemStack DynamicStack;
        if (Predications.stream().anyMatch(a -> a == current_animation)) {
            DynamicStack = sheathStack2;
        } else if (AttackPredication.stream().anyMatch(staticAnimation -> staticAnimation == current_animation)){
            DynamicStack = sheathStack;
        }else { DynamicStack = air;}

        poseStack.pushPose();
        MathUtils.mulStack(poseStack, modelMatrix_offhand);
        itemRenderer.renderStatic(DynamicStack, ItemDisplayContext.THIRD_PERSON_LEFT_HAND, packedLight, OverlayTexture.NO_OVERLAY, poseStack, buffer, null, 0);
        poseStack.popPose();


    }

}
