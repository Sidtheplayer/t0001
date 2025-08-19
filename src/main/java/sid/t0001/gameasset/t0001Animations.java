package sid.t0001.gameasset;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber.Bus;
import yesman.epicfight.api.animation.AnimationManager.AnimationAccessor;
import yesman.epicfight.api.animation.AnimationManager.AnimationBuilder;
import yesman.epicfight.api.animation.AnimationManager.AnimationRegistryEvent;
import yesman.epicfight.api.animation.TransformSheet;
import yesman.epicfight.api.animation.property.AnimationEvent;
import yesman.epicfight.api.animation.property.AnimationEvent.InTimeEvent;
import yesman.epicfight.api.animation.types.DodgeAnimation;
import yesman.epicfight.particle.EpicFightParticles;
import org.xame.t0001;
import yesman.epicfight.gameasset.Animations;
import yesman.epicfight.gameasset.Armatures;
import yesman.epicfight.gameasset.EpicFightSounds;

//this fucking took ages, fuck coding, thank god, I switched to intellij otherwise i would have died on vscode

@Mod.EventBusSubscriber(modid = t0001.MODID, bus = Bus.MOD)
public class t0001Animations {
    public static AnimationAccessor<DodgeAnimation> ACCELERATE;
    public static AnimationAccessor<DodgeAnimation> ACCELERATE_BACK;

    @SubscribeEvent
    public static void registerAnimations(AnimationRegistryEvent event) {
        event.newBuilder(t0001.MODID, t0001Animations::build);
    }

    public static void build(AnimationBuilder builder) {
        //  multiple afterimages (blur effect)
        ACCELERATE = builder.nextAccessor("biped/skill/accelerate_dodge", (accessor) ->
                new DodgeAnimation(0.1F, accessor, 0.2F, 0.4F, Armatures.BIPED)
                        // Spawn a trail of afterimages across the dodge
                        .addEvents(InTimeEvent.create(0.15F, ReusableEvents.AFTER_IMAGE, AnimationEvent.Side.CLIENT))
                        .addEvents(InTimeEvent.create(0.27F, ReusableEvents.AFTER_IMAGE, AnimationEvent.Side.CLIENT))
                        .addEvents(InTimeEvent.create(0.37F, ReusableEvents.AFTER_IMAGE, AnimationEvent.Side.CLIENT))
                        .addEvents(InTimeEvent.create(0.46F, ReusableEvents.AFTER_IMAGE, AnimationEvent.Side.CLIENT))
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

        );
    }

    public static class ReusableEvents {
        private static final AnimationEvent.E0 AFTER_IMAGE = (entitypatch, self, params) -> {
            LivingEntity entity = entitypatch.getOriginal();
            entity.level().addParticle(
                    EpicFightParticles.WHITE_AFTERIMAGE.get(),
                    entity.getX(),
                    entity.getY(),
                    entity.getZ(),
                    Double.longBitsToDouble(entity.getId()),
                    0,
                    0
            );


            };
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
}

