package sid.t0001.gameasset;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.xame.t0001;

public class t0001Sounds {
    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, "t0001");

    public static final RegistryObject<SoundEvent> SLAM_SFX = registerSound("sfx.slam_sfx");
    public static final RegistryObject<SoundEvent> SMOOTH_DODGE = registerSound("sfx.smooth_dodge");
    @SuppressWarnings("unused")
    public static final RegistryObject<SoundEvent> WEAVE = registerSound("joke.weave");
    public static final RegistryObject<SoundEvent> HIT_BOOM = registerSound("sfx.hit_boom");
    public static final RegistryObject<SoundEvent> AMOGUS_AMBIENT = registerSound("joke.amogus_ambient");
    public static final RegistryObject<SoundEvent> AMOGUS_DEATH = registerSound("joke.amogus_death");







    private static RegistryObject<SoundEvent> registerSound(String name) {
        ResourceLocation res = ResourceLocation.fromNamespaceAndPath(t0001.MODID, name);
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(res));
    }
}
