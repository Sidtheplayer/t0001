package sid.t0001.gameasset;


import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.AnimationManager.AnimationBuilder;
import yesman.epicfight.api.animation.AnimationManager.AnimationRegistryEvent;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.animation.property.AnimationEvent.InTimeEvent;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.animation.types.AttackAnimation;
import yesman.epicfight.api.animation.types.BasicAttackAnimation;
import yesman.epicfight.api.animation.types.DodgeAnimation;
import org.xame.t0001;
import yesman.epicfight.api.utils.HitEntityList;
import yesman.epicfight.api.utils.math.ValueModifier;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.Armatures;
import sid.t0001.gameasset.ReusableEvents;
import yesman.epicfight.gameasset.ColliderPreset;
import yesman.epicfight.gameasset.EpicFightSounds;
import yesman.epicfight.skill.weaponinnate.SimpleWeaponInnateSkill;
import yesman.epicfight.world.damagesource.StunType;


//this fucking took ages, fuck coding, thank god, I switched to intellij otherwise I would have died on VS Code

@Mod.EventBusSubscriber(modid = t0001.MODID, bus = Bus.MOD)
public class t0001Animations {
    public static AnimationAccessor<DodgeAnimation> ACCELERATE;
    public static AnimationAccessor<DodgeAnimation> ACCELERATE_BACK;
    public static AnimationAccessor<BasicAttackAnimation> FANG_DANCE;

    @SubscribeEvent
    public static void registerAnimations(AnimationRegistryEvent event) {
        event.newBuilder(t0001.MODID, t0001Animations::build);
    }

    public static void build(AnimationBuilder builder) {
        //  multiple afterimages (blur effect)
        ACCELERATE = builder.nextAccessor("biped/skill/accelerate_dodge", (accessor) ->
                new DodgeAnimation(0.1F, accessor, 0.2F, 0.4F, Armatures.BIPED)
                        // Spawn a trail of afterimages across the dodge
                        .addEvents(InTimeEvent.create(0.14F, ReusableEvents.AFTER_IMAGE, AnimationEvent.Side.CLIENT))
                        .addEvents(InTimeEvent.create(0.27F, ReusableEvents.AFTER_IMAGE, AnimationEvent.Side.CLIENT))
                        .addEvents(InTimeEvent.create(0.36F, ReusableEvents.AFTER_IMAGE, AnimationEvent.Side.CLIENT))
                        .addEvents(InTimeEvent.create(0.44F, ReusableEvents.AFTER_IMAGE, AnimationEvent.Side.CLIENT))
                        .addEvents(InTimeEvent.create(0.51F, ReusableEvents.AFTER_IMAGE, AnimationEvent.Side.CLIENT))
                        // Dodge sound
                        .addEvents(InTimeEvent.create(0.0F, Animations.ReusableSources.PLAY_SOUND, AnimationEvent.Side.CLIENT)
                                .params(t0001Sounds.SMOOTH_DODGE.get()))
        );


        ACCELERATE_BACK = builder.nextAccessor("biped/skill/accelerate_dodge_back", (accessor) ->
                new DodgeAnimation(0.1F, accessor, 0.4F, 0.8F, Armatures.BIPED)
                        .addEvents(InTimeEvent.create(0.0F, Animations.ReusableSources.PLAY_SOUND, AnimationEvent.Side.CLIENT)
                                .params(t0001Sounds.SLAM_SFX.get()))
                        .addEvents(InTimeEvent.create(0.0F, Animations.ReusableSources.FRACTURE_GROUND_SIMPLE, AnimationEvent.Side.SERVER)
                                .params(new yesman.epicfight.api.utils.math.Vec3f(0.0F, 0.0F, -0.01F),
                                        Armatures.BIPED.get().legL, 1.5D, .15F))
                       // .addEvents(ReusableEvents.MyFxHelpers.blockFX(new ResourceLocation("photon:fireline"),0.0F))
        );


        FANG_DANCE = builder.nextAccessor("biped/skill/jun_take_43", (accessor) ->
                new BasicAttackAnimation(0.0F, accessor, Armatures.BIPED,

                        //  - Right hand Stun strike
                        new AttackAnimation.Phase(0.0F, 0.0F, 0.0F, 0.54F, 0.0F, 0.0F, Armatures.BIPED.get().handR, ColliderPreset.FIST)
                                .addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.HOLD)
                                .addProperty(AnimationProperty.AttackPhaseProperty.IMPACT_MODIFIER, ValueModifier.multiplier(2.5F))
                                .addProperty(AnimationProperty.AttackPhaseProperty.HIT_SOUND, EpicFightSounds.NEUTRALIZE_MOBS.get()),

                        // Left leg stun kick should be a Knee but mc model limitations
                        new AttackAnimation.Phase(0.0F, 2.0F, 1.60F, 2.50F, 0.0F, 2.0F, Armatures.BIPED.get().legL, ColliderPreset.FIST)
                                .addProperty(AnimationProperty.AttackPhaseProperty.DAMAGE_MODIFIER, ValueModifier.multiplier(0.5F))
                                .addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.HOLD)
                                .addProperty(AnimationProperty.AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLUNT_HIT_HARD.get()),


                        //  elbowL follow-up
                        new AttackAnimation.Phase(0.35F, 2.0F, 2.50F, 3.0F, 3.0F, 3.0F, Armatures.BIPED.get().elbowL, ColliderPreset.FIST)
                                .addProperty(AnimationProperty.AttackPhaseProperty.STUN_TYPE, StunType.KNOCKDOWN)
                                .addProperty(AnimationProperty.AttackPhaseProperty.ARMOR_NEGATION_MODIFIER, ValueModifier.adder(0.2F))
                                .addProperty(AnimationProperty.AttackPhaseProperty.HIT_SOUND, EpicFightSounds.BLUNT_HIT_HARD.get())
                                .addProperty(AnimationProperty.AttackPhaseProperty.HIT_PRIORITY, HitEntityList.Priority.TARGET)
                )
        );




















    }






        //I'll finish this later (I'm probably never gonna)
       /* private static final AnimationEvent.E0 SLAM_GIN = (self, entitypatch, transformSheet) -> {

            HitResult hitResult = entitypatch.getOriginal().pick(50.0D, 1.0F, false);
            Vec3 to = hitResult.getLocation();
            Vec3 from = entitypatch.getOriginal().position();
            Vec3 correction = to.subtract(from).normalize().scale(5.0D);

            TransformSheet correctedCoord = self.getCoord().getCorrectedModelCoord(entitypatch, from, to.add(correction), 0, 2);
            transformSheet.readFrom(correctedCoord);
        };*/

}


