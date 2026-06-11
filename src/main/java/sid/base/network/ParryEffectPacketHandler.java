package sid.base.network;


import com.lowdragmc.photon.client.fx.EntityEffectExecutor;
import com.lowdragmc.photon.client.fx.FX;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;
import org.joml.Vector3f;
import sid.base.main.t0001;

import java.util.UUID;


@OnlyIn(value = Dist.CLIENT)
public class ParryEffectPacketHandler {
//Feel free to copy this whole thing if u want
    public static void handleParryEffect(String entityUUID, boolean isParried, double posX, double posY, double posZ) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null){
            System.out.println("THE LEVEL IS NULL");
            return;
        }

        Entity entity = minecraft.level.getPlayerByUUID(UUID.fromString(entityUUID));
        if (entity == null) {
            System.out.println("THE ENTITY IS NULL");
            return;
        }

        Vector3f offsetcoord = getVector3f(posX, posY, posZ);

        if (isParried) {
            // Parry effect: cgparry
            FX parryfx = t0001.getmodfx("cgparry");
            if(parryfx==null)return;
            EntityEffectExecutor parry_effect = new EntityEffectExecutor(parryfx, entity.level(), entity, EntityEffectExecutor.AutoRotate.XROT);

            parry_effect.setOffset(offsetcoord);
            parry_effect.setRotation(0, 0, 0);
            parry_effect.setScale(1, 1, 1);
            parry_effect.setDelay(0);
            parry_effect.setForcedDeath(false);
            parry_effect.setAllowMulti(true);

            parry_effect.start();
        } else {
            // Normal block effect
            FX blockfx = t0001.getmodfx("cgparry");
            if(blockfx == null)return;
            EntityEffectExecutor block_effect = new EntityEffectExecutor(blockfx, entity.level(), entity, EntityEffectExecutor.AutoRotate.XROT);

            block_effect.setOffset(0, 0.35, 0);
            block_effect.setScale(1.0, 1.0, 1.0);
            block_effect.setRotation(0,0,0);
            block_effect.setDelay(0);
            block_effect.setForcedDeath(false);
            block_effect.setAllowMulti(true);

            block_effect.start();
        }
    }

    private static @NotNull Vector3f getVector3f(double posX, double posY, double posZ) {
        BlockPos effectPos = new BlockPos((int) posX, (int) posY, (int) posZ);
        //currently not used originally meant for blockeffectexecutor
        // subtract 0.5 from offsets if using block effect instead
        // because Minecraft adds 0.5 offset to center of block
        double offsetX = posX - effectPos.getX() ;
        double offsetY = posY - effectPos.getY();
        double offsetZ = posZ - effectPos.getZ() ;

        return new Vector3f((float) offsetX, (float) offsetY + 0.75f, (float) offsetZ);
    }
}

