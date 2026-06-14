package sid.base.world;

import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.entity.Entity;

public class VerySpecialDamageSources {

    private static Holder<DamageType> getDamageTypeHolder(Entity entity, ResourceKey<DamageType> damageTypeKey) {
        return entity.level().registryAccess().registryOrThrow(Registries.DAMAGE_TYPE).getHolderOrThrow(damageTypeKey);
    }


}
