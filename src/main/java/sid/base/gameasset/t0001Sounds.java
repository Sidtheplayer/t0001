package sid.base.gameasset;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import sid.base.main.t0001;

@SuppressWarnings("unused")
public final class t0001Sounds {
    private t0001Sounds() {}

    public static final DeferredRegister<SoundEvent> REGISTRY =
            DeferredRegister.create(Registries.SOUND_EVENT, t0001.MODID);

    /*-SFX-*/

    public static final DeferredHolder<SoundEvent, SoundEvent> SLAM_SFX =
            registerVariableRangeSound("sfx.slam_sfx");

    public static final DeferredHolder<SoundEvent, SoundEvent> SMOOTH_DODGE =
            registerVariableRangeSound("sfx.smooth_dodge");


    public static final DeferredHolder<SoundEvent, SoundEvent> WEAVE =
            registerVariableRangeSound("joke.weave");

    public static final DeferredHolder<SoundEvent, SoundEvent> HIT_BOOM =
            registerVariableRangeSound("sfx.hit_boom");

    public static final DeferredHolder<SoundEvent, SoundEvent> TESTONE_INCH =
            registerVariableRangeSound("sfx.testsfx");

    /*-LIVING-*/

    public static final DeferredHolder<SoundEvent, SoundEvent> AMOGUS_AMBIENT =
            registerVariableRangeSound("joke.amogus_ambient");

    public static final DeferredHolder<SoundEvent, SoundEvent> AMOGUS_DEATH =
            registerVariableRangeSound("joke.amogus_death");

    //emotes and voicelines etc etc




    private static DeferredHolder<SoundEvent, SoundEvent> registerVariableRangeSound(String name) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(t0001.MODID, name);
        return REGISTRY.register(name, () -> SoundEvent.createVariableRangeEvent(id));
    }

    @SuppressWarnings("unused")
    private static DeferredHolder<SoundEvent, SoundEvent> registerFixedRangeSound(String name, float range) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath(t0001.MODID, name);
        return REGISTRY.register(name, () -> SoundEvent.createFixedRangeEvent(id, range));
    }

}
