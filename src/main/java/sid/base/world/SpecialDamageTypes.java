package sid.base.world;


import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import sid.base.main.t0001;

public class SpecialDamageTypes {

    private SpecialDamageTypes() {}

    public static final ResourceKey<DamageType> SPECIAL_EXECUTION_FINISHER = ResourceKey.create(Registries.DAMAGE_TYPE, t0001.identifier("execution_finisher"));



}
