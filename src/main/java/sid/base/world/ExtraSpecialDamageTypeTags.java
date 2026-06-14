package sid.base.world;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageType;
import sid.base.main.t0001;

public interface ExtraSpecialDamageTypeTags {

    private static TagKey<DamageType> create(String tagName) {
        return TagKey.create(Registries.DAMAGE_TYPE, t0001.identifier(tagName));
    }
    /// Rag dolled players can be damaged if an attack has finisher DamageTypeTag
    TagKey<DamageType> RAG_DOLL_STUN = create("rag_doll_stun");
    /// Rag dolled players can be damaged if an attack has finisher DamageTypeTag
    TagKey<DamageType> RAG_DOLL_LAUNCH = create("rag_doll_stun_launch");
    /// Rag dolled players can be damaged if an attack has finisher DamageTypeTag
    TagKey<DamageType> RAG_DOLL_LAUNCH_UP = create("rag_doll_stun_launch_up");

}
