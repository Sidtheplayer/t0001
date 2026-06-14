package sid.base.gameasset.animations.collider;


import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import net.minecraft.resources.ResourceLocation;
import sid.base.main.t0001;
import yesman.epicfight.api.collider.Collider;
import yesman.epicfight.api.collider.MultiOBBCollider;

///ifykyk
@SuppressWarnings("unused")
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
    public static final Collider ULTIMATE_KNOCKBACK_BOX = registercollider(ResourceLocation.fromNamespaceAndPath(t0001.MODID,"ultimate_knockback_areabox"),
            new MultiOBBCollider(5, 10D, 3D, 10D, 0D, 2.25D, 0D)
    );

    //WeaponColliders
    public static final Collider DRAGON_GOD_SWORD_COLLIDER =
            registercollider(t0001.identifier("dragon_god_sword_collider"),
                    new MultiOBBCollider(9,0.25D, 0.25D, 0.9D,-0.05D, 0.0D , -1.29D));

     public static final Collider PHANTOM_SEVERANCE =
             registercollider(t0001.identifier("phantom_severance_collider"),
                     new MultiOBBCollider(12,2.0D, 1.0D, 5.0D, -0.5D, 0.25D, 0.0D)
                     );
}