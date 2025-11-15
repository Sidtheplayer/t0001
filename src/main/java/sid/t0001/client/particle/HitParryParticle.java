package sid.t0001.client.particle;

import com.lowdragmc.lowdraglib.gui.animation.Transform;
import com.lowdragmc.photon.client.fx.BlockEffect;
import com.lowdragmc.photon.client.fx.FX;
import com.lowdragmc.photon.client.fx.FXHelper;
import com.lowdragmc.photon.client.fx.FXRuntime;
import com.lowdragmc.photon.client.gameobject.IFXObject;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.client.particle.SpriteSet;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import yesman.epicfight.client.particle.HitParticle;


import java.util.List;
import java.util.logging.Logger;

@OnlyIn(Dist.CLIENT)
public class HitParryParticle extends HitParticle {

    public HitParryParticle(ClientLevel world, double x, double y, double z, double argX, double argY, double argZ, SpriteSet animatedSprite) {
        super(world, x, y, z, animatedSprite);

        this.xd = argX;
        this.yd = argY;
        this.zd = argZ;

        this.rCol = 1.0F;
        this.gCol = 1.0F;
        this.bCol = 1.0F;
        this.quadSize = 1.0F;
        this.lifetime = 2;


        BlockPos effectPos = new BlockPos((int) x, (int) y, (int) z); /*todo:make the photon particle follow the empty particle coordinates*/

        FX hitParryFX = FXHelper.getFX(ResourceLocation.parse("photon:clash3"));
        BlockEffect hitParryEffect = new BlockEffect(hitParryFX, this.level, effectPos);
        hitParryEffect.setScale(1, 1, 1);
        hitParryEffect.setOffset(-0.5, -0.5, -0.5); //subtract -0.5 to account for 0.5 offset minecraft normally puts
        hitParryEffect.setRotation(0, 0, 0);
        hitParryEffect.setAllowMulti(true);
        hitParryEffect.setCheckState(true);


        hitParryEffect.start();
        assert hitParryEffect.getRuntime() != null;
        FXRuntime runtime = hitParryEffect.getRuntime();

        if (runtime.root != null) {
            runtime.root.updatePos(new Vector3f((float) this.x, (float) this.y, (float) this.z));
        }
        List<IFXObject> testlist = hitParryEffect.getRuntime().fxData.objects();
        System.out.println("list of objs:" + testlist);

        AABB box = new AABB(
                this.x - 5, this.y - 5, this.z - 5,
                this.x + 5, this.y + 5, this.z + 5
        );
        List<Entity> collisions = this.level.getEntities(null, box);

        for (Entity collision : collisions) {
            LivingEntity target = collision instanceof LivingEntity
                    ? (LivingEntity) collision
                    : null;
            System.out.println("target(s): " + target);
        }



    }

    @OnlyIn(Dist.CLIENT)
    public static class Provider implements ParticleProvider<SimpleParticleType> {
        private final SpriteSet spriteSet;

        public Provider(SpriteSet spriteSet) {
            this.spriteSet = spriteSet;
        }

        @Override
        public Particle createParticle(@NotNull SimpleParticleType typeIn, @NotNull ClientLevel worldIn, double x, double y, double z, double xSpeed, double ySpeed, double zSpeed) {
            return new HitParryParticle(worldIn, x, y, z, xSpeed, ySpeed, zSpeed, this.spriteSet);
        }
    }
}
