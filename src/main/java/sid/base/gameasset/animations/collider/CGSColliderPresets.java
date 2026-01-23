package sid.base.gameasset.animations.collider;


import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.resources.ResourceLocation;
import sid.base.main.t0001;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.collider.MultiOBBCollider;


public class CGSColliderPresets {
    public static final BiMap<ResourceLocation, Collider> PRESETS = HashBiMap.create();

    public static Collider registercollider(ResourceLocation id, Collider collider) {

        if (PRESETS.containsKey(id)) {
            throw new IllegalStateException("Collider named " + id + " already registered.");
        }
        PRESETS.put(id, collider);
        return collider;
    }

    public static final Collider ONE_INCH_COUNTER = registercollider(ResourceLocation.fromNamespaceAndPath(t0001.MODID,"one_inch_counter"),
            new MultiOBBCollider(3, 6D, 3D, 6D, 3D, 3D, 3D)
    );
    public static final Collider ULTIMATE_KNOCKBACK_AREABOX = registercollider(ResourceLocation.fromNamespaceAndPath(t0001.MODID,"ultimate_knockback_areabox"),
            new MultiOBBCollider(5, 10D, 5D, 10D, 6D, 5D, 6D)
    );
    //WeaponColliders
    public static final Collider DRAGON_GOD_SWORD_COLLIDER =
            registercollider(t0001.identifier("dragon_god_sword"),
                    new MultiOBBCollider(3,0.25D, 0.25D, 0.9D,-0.05D, 0.0D , -1.29D));

}