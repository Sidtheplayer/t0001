package sid.base.utils;

import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.fml.util.ObfuscationReflectionHelper;
import org.jetbrains.annotations.Nullable;
import sid.base.events.global_events.GlobalEventHandlers;

import java.util.*;

public class GroundWaveUtil {

    private GroundWaveUtil() {}

    public enum WaveMode {
        CONE,
        CIRCLE,
        SQUARE
    }

    //fixed fallbacks
    private static final int    STARTUP_DELAY_TICKS =  10;
    private static final int    TICKS_PER_RING       = 2;
    private static final double MAX_RANGE            = 10.0;
    private static final double RING_HALF_THICKNESS  = 1.0;
    private static final double CONE_HALF_ANGLE_DEG  = 40.0;
    private static final float  DAMAGE_SCALE         = 0.5F;
    private static final float  LAUNCH_UP            = 0.29F;
    private static final float  LAUNCH_OUT           = 0.24F;


    public static void simple_wave(LivingEntity caster, boolean hurt_entities) {
        simple_wave(caster, hurt_entities, WaveMode.CONE);
    }

    public static void simple_wave(LivingEntity caster, boolean hurt_entities, WaveMode mode) {
        if (!(caster.level() instanceof ServerLevel level)) return;

        Vec3 look = caster.getLookAngle();
        Vec3 flat = new Vec3(look.x, 0, look.z);
        if (flat.lengthSqr() < 1.0e-6) return;

        Vec3 origin = caster.position();
        Vec3 forward = flat.normalize();
        Vec3 right = new Vec3(-forward.z, 0, forward.x).normalize();
        UUID casterId = caster.getUUID();

        MinecraftServer server = level.getServer();
        WaveContext ctx = new WaveContext(server, level, casterId, origin, forward, right, mode);
        scheduleDelayed(ctx.server, () -> expandRing(ctx, 4, hurt_entities), STARTUP_DELAY_TICKS);
    }

    public static void trigger_wave(@Nullable LivingEntity caster, Vec3 origin,
                                    int ring, double ring_half_thickness, double max_range, double cone_half_ang,
                                    int ticks_per_ring,
                                    boolean hurt_entities, int startup_delay) {
        trigger_wave(caster, origin, ring, ring_half_thickness, max_range, cone_half_ang,
                ticks_per_ring, hurt_entities, startup_delay, WaveMode.CONE);
    }

    public static void trigger_wave(@Nullable LivingEntity caster, Vec3 origin,
                                    int ring, double ring_half_thickness, double max_range, double cone_half_ang,
                                    int ticks_per_ring,
                                    boolean hurt_entities, int startup_delay, WaveMode mode) {
        if (caster == null || !(caster.level() instanceof ServerLevel level)) return;

        Vec3 look = caster.getLookAngle();
        Vec3 flat = new Vec3(look.x, 0, look.z);
        if (flat.lengthSqr() < 1.0e-6) return;

        Vec3 forward = flat.normalize();
        Vec3 right = new Vec3(-forward.z, 0, forward.x).normalize();
        UUID casterId = caster.getUUID();

        MinecraftServer server = level.getServer();
        WaveContext ctx = new WaveContext(server, level, casterId, origin, forward, right, mode);
        scheduleDelayed(ctx.server, () -> expandRing(ctx, ring, ring_half_thickness, max_range, cone_half_ang, ticks_per_ring, hurt_entities), startup_delay);
    }


    public static final class WaveContext {
        final MinecraftServer server;
        final ServerLevel level;
        final UUID casterId;
        final Vec3 origin;
        final Vec3 forward;
        final Vec3 right;
        final WaveMode mode;
        final Set<UUID> alreadyHit = new HashSet<>();
        final Set<BlockPos> alreadyPopped = new HashSet<>();

        WaveContext(MinecraftServer server, ServerLevel level, UUID casterId, Vec3 origin, Vec3 forward, Vec3 right, WaveMode mode) {
            this.server = server;
            this.level = level;
            this.casterId = casterId;
            this.origin = origin;
            this.forward = forward;
            this.right = right;
            this.mode = mode;
        }
    }

    public static void expandRing(WaveContext ctx, int ring, boolean hurtEntities) {
        expandRing(ctx, ring, RING_HALF_THICKNESS, MAX_RANGE, CONE_HALF_ANGLE_DEG, TICKS_PER_RING, hurtEntities);
    }


    public static void expandRing(WaveContext ctx, int ring, double ring_half_thickness, double max_range, double cone_half_ang,
                                  int ticks_per_ring,
                                  boolean hurt_entities) {
        Entity entity = ctx.level.getEntity(ctx.casterId);
        if (!(entity instanceof LivingEntity caster)) {
            return;
        }

        double min = Math.max(0, ring - ring_half_thickness);
        double max = ring + ring_half_thickness;

        AABB bounds = new AABB(
                ctx.origin.x - max, ctx.origin.y - 2, ctx.origin.z - max,
                ctx.origin.x + max, ctx.origin.y + 3, ctx.origin.z + max);

        if (hurt_entities) {
            for (LivingEntity target : ctx.level.getEntitiesOfClass(LivingEntity.class, bounds,
                    t -> t.isAlive() && t != caster)) {

                if (target.isAlliedTo(caster) || ctx.alreadyHit.contains(target.getUUID())) continue;

                if (!isWithinRing(ctx, target.getX(), target.getZ(), min, max, cone_half_ang)) continue;

                applyHit(caster, target, ctx);
                ctx.alreadyHit.add(target.getUUID());
            }
        }

        spawnGroundPop(ctx, ring, cone_half_ang);

        if (ring < (int) max_range) {
            scheduleDelayed(ctx.server, () -> expandRing(ctx, ring + 1, ring_half_thickness, max_range, cone_half_ang, ticks_per_ring, hurt_entities), ticks_per_ring);
        }
    }

    /**
     * "min <= radial distance <= max" ring band; they differ only in
     * how that radial distance is measured, and CONE additionally
     * restricts to an angular wedge in front of the caster.
     */
    private static boolean isWithinRing(WaveContext ctx, double worldX, double worldZ, double min, double max, double cone_half_ang) {
        double dx = worldX - ctx.origin.x;
        double dz = worldZ - ctx.origin.z;

        return switch (ctx.mode) {
            case CONE -> {
                Vec3 rel = new Vec3(dx, 0, dz);
                double along = rel.dot(ctx.forward);
                if (along < min || along > max || along <= 0.05) yield false;

                double side = Math.abs(rel.dot(ctx.right));
                double maxSide = Math.tan(Math.toRadians(cone_half_ang)) * along;
                yield side <= maxSide;
            }
            case CIRCLE -> {
                double dist = Math.sqrt(dx * dx + dz * dz);
                yield dist >= min && dist <= max;
            }
            case SQUARE -> {
                // Chebyshev distance -> square-shaped ring (expanding box outline)
                double dist = Math.max(Math.abs(dx), Math.abs(dz));
                yield dist >= min && dist <= max;
            }
        };
    }

    private static void applyHit(LivingEntity caster, LivingEntity target, WaveContext ctx) {
        float damage = Math.max(1.0F,
                (float) caster.getAttributeValue(Attributes.ATTACK_DAMAGE) * DAMAGE_SCALE);

        int savedInvuln = target.invulnerableTime;
        target.invulnerableTime = 0;
        try {
            target.hurt(damageSourceFor(caster), damage);
        } finally {
            target.invulnerableTime = savedInvuln;
        }

        Vec3 away = new Vec3(target.getX() - ctx.origin.x, 0, target.getZ() - ctx.origin.z);
        Vec3 push = away.lengthSqr() > 1e-6 ? away.normalize().scale(LAUNCH_OUT) : Vec3.ZERO;
        target.setDeltaMovement(push.x, LAUNCH_UP, push.z);
        target.hurtMarked = true;
    }

    private static DamageSource damageSourceFor(LivingEntity caster) {
        return caster instanceof Player p
                ? caster.damageSources().playerAttack(p)
                : caster.damageSources().mobAttack(caster);
    }

    private static void spawnGroundPop(WaveContext ctx, int ring, double cone_half_ang) {
        List<Vec3> points = switch (ctx.mode) {
            case CONE -> conePoints(ctx, ring, cone_half_ang);
            case CIRCLE -> circlePoints(ctx, ring);
            case SQUARE -> squarePoints(ctx, ring);
        };

        for (Vec3 point : points) {
            BlockPos surface = findTopSolidBlock(ctx.level, point.x, ctx.origin.y, point.z);
            if (surface == null || !ctx.alreadyPopped.add(surface)) continue;

            BlockState state = ctx.level.getBlockState(surface);
            if (state.getRenderShape() == RenderShape.INVISIBLE) continue;
            if (state.getBlock().getExplosionResistance() > 30.0f) continue;

            FallingBlockEntity fallingBlockEntity = new FallingBlockEntity(EntityType.FALLING_BLOCK, ctx.level);

            ObfuscationReflectionHelper.setPrivateValue(FallingBlockEntity.class, fallingBlockEntity, state, "blockState");

            fallingBlockEntity.setPos(surface.getX() - 0.15, surface.getY() - 0.15, surface.getZ() - 0.15);
            fallingBlockEntity.setStartPos(surface);

            Vec3 delta = fallingBlockEntity.getDeltaMovement();
            fallingBlockEntity.setDeltaMovement(delta.x, delta.y + 0.5D, delta.z);
            fallingBlockEntity.disableDrop();
            fallingBlockEntity.setInvulnerable(true);
            fallingBlockEntity.noPhysics = true;
            fallingBlockEntity.setHurtsEntities(0.25F, 3);

            ctx.level.addFreshEntity(fallingBlockEntity);
        }
    }

    private static List<Vec3> conePoints(WaveContext ctx, int ring, double cone_half_ang) {
        double width = Math.tan(Math.toRadians(cone_half_ang)) * ring;
        int segments = Math.max(2, (int) Math.ceil(width * 2));

        List<Vec3> points = new ArrayList<>(segments * 2 + 1);
        for (int i = -segments; i <= segments; i++) {
            double offset = (i / (double) segments) * width;
            points.add(ctx.origin.add(ctx.forward.scale(ring)).add(ctx.right.scale(offset)));
        }
        return points;
    }

    private static List<Vec3> circlePoints(WaveContext ctx, int ring) {
        // circumference-based sample count so spacing stays roughly constant as the ring grows
        int segments = Math.max(8, (int) Math.ceil(2 * Math.PI * ring));
        List<Vec3> points = new ArrayList<>(segments);
        for (int i = 0; i < segments; i++) {
            double angle = (2 * Math.PI * i) / segments;
            Vec3 dir = ctx.forward.scale(Math.cos(angle)).add(ctx.right.scale(Math.sin(angle)));
            points.add(ctx.origin.add(dir.scale(ring)));
        }
        return points;
    }

    private static List<Vec3> squarePoints(WaveContext ctx, int ring) {
        if (ring <= 0) {
            return List.of(ctx.origin);
        }

        // walk the perimeter of a (2*ring)x(2*ring) square, axis-aligned to forward/right
        List<Vec3> points = new ArrayList<>(8 * ring);
        for (int i = -ring; i <= ring; i++) {
            points.add(ctx.origin.add(ctx.forward.scale(ring)).add(ctx.right.scale(i)));   // +forward edge
            points.add(ctx.origin.add(ctx.forward.scale(-ring)).add(ctx.right.scale(i)));  // -forward edge
            if (i != -ring && i != ring) { // avoid duplicating corners
                points.add(ctx.origin.add(ctx.forward.scale(i)).add(ctx.right.scale(ring)));   // +right edge
                points.add(ctx.origin.add(ctx.forward.scale(i)).add(ctx.right.scale(-ring)));  // -right edge
            }
        }
        return points;
    }

    private static BlockPos findTopSolidBlock(ServerLevel level, double x, double y, double z) {
        int baseY = Mth.floor(y);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(Mth.floor(x), baseY + 1, Mth.floor(z));
        for (int dy = 0; dy < 6; dy++) {
            pos.setY(baseY + 1 - dy);
            BlockState state = level.getBlockState(pos);
            if (!state.isAir() && state.getRenderShape() != RenderShape.INVISIBLE) {
                return pos.immutable();
            }
        }
        return null;
    }

    private static void scheduleDelayed(MinecraftServer server, Runnable task, int delayTicks) {
        GlobalEventHandlers.DelayedTaskScheduler.schedule(server, delayTicks, task);
    }
}


