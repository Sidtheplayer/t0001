package sid.base.world.entity;

import com.lowdragmc.lowdraglib2.syncdata.annotation.DescSynced;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.level.EntityBasedExplosionDamageCalculator;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Objects;

public class DemonCoredDrone extends PathfinderMob  {
//vfx
    public static final String ExplodePacketVFXID = "ExplodeNearEnemy";

    public DemonCoredDrone(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }


    @Override
    public boolean shouldShowName() {
        return true;
    }

    public void ExplodeNearEnemy(){
        LivingEntity target = this.getTargetFromBrain();
        boolean shouldExplode = this.distanceTo(Objects.requireNonNull(target)) <= 2;
        Level level = this.level();
        ExplosionDamageCalculator damageCalculator = new ExplosionDamageCalculator();
        Explosion explosion = new Explosion(level,this,5,5,5,5,true, Explosion.BlockInteraction.DESTROY_WITH_DECAY);
        if(shouldExplode){
            this.level().explode(this,this.damageSources().explosion(explosion),damageCalculator,this.position(),5.0F,true, Level.ExplosionInteraction.TNT);
        }
    }


}
