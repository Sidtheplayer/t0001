package sid.base.skill.awakening;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import sid.base.skill.t0001SkillCategories;
import yesman.epicfight.skill.Skill;
import yesman.epicfight.skill.SkillBuilder;
import yesman.epicfight.skill.SkillContainer;
import yesman.epicfight.world.capabilities.EpicFightCapabilities;
import yesman.epicfight.world.capabilities.item.WeaponCategory;

import java.util.function.Function;

public abstract class AwakeningSkill extends Skill {


    public AwakeningSkill(SkillBuilder<?> builder) {
        super(builder);
    }


    public static SkillBuilder<?> createAwakeningSkillBuilder(Function<SkillBuilder<?>, ? extends AwakeningSkill> constructor) {
        return new SkillBuilder<>(constructor)
                .setCategory(t0001SkillCategories.AWAKENING)
                .setActivateType(ActivateType.TOGGLE)
                .setResource(Resource.NONE);

    }

    @Override
    public boolean canExecute(SkillContainer container) {
        ItemStack weapon = container.getExecutor().getOriginal().getMainHandItem();
        WeaponCategory weaponCategory = EpicFightCapabilities.getItemStackCapability(weapon).getWeaponCategory();
        if (container.getExecutor().isLogicalClient())
        {
            return super.canExecute(container);

        } else {
            return super.canExecute(container)
                    && container.getExecutor().getOriginal().getVehicle() == null && (!container.getExecutor().getSkill(this).isActivated() || this.activateType == ActivateType.TOGGLE);
        }
    }

    @Override
    public void executeOnServer(SkillContainer container, CompoundTag args) {
        super.executeOnServer(container, args);
    }





}
