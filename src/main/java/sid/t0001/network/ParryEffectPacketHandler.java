package sid.t0001.network;

import com.lowdragmc.photon.client.fx.BlockEffect;
import com.lowdragmc.photon.client.fx.EntityEffect;
import com.lowdragmc.photon.client.fx.FX;
import com.lowdragmc.photon.client.fx.FXHelper;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;


@OnlyIn(Dist.CLIENT)
public class ParryEffectPacketHandler {

    public static void handleParryEffect(int entityId, boolean isParried, double posX, double posY, double posZ) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.level == null) return;

        Entity entity = minecraft.level.getEntity(entityId);
        if (entity == null) return;

        BlockPos effectPos = new BlockPos((int) posX, (int) posY, (int) posZ);

        double offsetX = posX - effectPos.getX() - 0.5; // subtract 0.5 because Minecraft adds 0.5
        double offsetY = posY - effectPos.getY() - 0.5;
        double offsetZ = posZ - effectPos.getZ() - 0.5;

        if (isParried) {
            // Parry effect: breakclash4
            FX breakclashfx = FXHelper.getFX(ResourceLocation.parse("photon:breakclash4"));
            BlockEffect parry_effect = new BlockEffect(breakclashfx, minecraft.level, effectPos);

            parry_effect.setOffset(offsetX, offsetY, offsetZ);
            parry_effect.setRotation(0, 0, 0);
            parry_effect.setScale(0.95, 0.95, 0.95);
            parry_effect.setDelay(0);
            parry_effect.setForcedDeath(false);
            parry_effect.setAllowMulti(true);

            parry_effect.start();
        } else {
            // Normal block effect - EntityEffect with entity-relative positioning
            FX blockfx = FXHelper.getFX(ResourceLocation.parse("photon:block"));
            EntityEffect block_effect = new EntityEffect(blockfx, minecraft.level, entity, EntityEffect.AutoRotate.XROT);

            block_effect.setOffset(0, 0.35, 0);
            block_effect.setScale(1.0, 1.0, 1.0);
            block_effect.setRotation(0,0,0);
            block_effect.setDelay(0);
            block_effect.setForcedDeath(false);
            block_effect.setAllowMulti(true);

            block_effect.start();
        }
    }
}

