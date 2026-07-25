package sid.base.gameasset.animations;

import net.minecraft.network.codec.ByteBufCodecs;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import sid.base.main.t0001;
import yesman.epicfight.api.animation.SynchedAnimationVariableKey;
import yesman.epicfight.registry.EpicFightRegistries;

public class CustomSynchedAnimationVariablekeys {

    private CustomSynchedAnimationVariablekeys(){}

    public static final DeferredRegister<SynchedAnimationVariableKey<?>> REGISTRY = DeferredRegister.create(EpicFightRegistries.SYNCHED_ANIMATION_VARIABLE, t0001.MODID);
    /// killer say what?
    public static final DeferredHolder<SynchedAnimationVariableKey<?>, SynchedAnimationVariableKey.SynchedIndependentAnimationVariableKey<Integer>> KILLER_ENTITY =
            REGISTRY.register("killer_entity", () ->
                    SynchedAnimationVariableKey.independent(animator -> -1, true, ByteBufCodecs.INT)
            );

  //  public static final DeferredHolder<SynchedAnimationVariableKey<?>, SynchedAnimationVariableKey.SynchedSharedAnimationVariableKey<Integer>> X =
}
