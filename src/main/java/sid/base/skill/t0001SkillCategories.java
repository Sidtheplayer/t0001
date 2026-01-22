package sid.base.skill;

import net.minecraft.resources.ResourceLocation;
import yesman.epicfight.EpicFight;
import yesman.epicfight.main.EpicFightMod;
import yesman.epicfight.skill.SkillCategory;

public enum t0001SkillCategories implements SkillCategory {
    INNER_TRANSITION(true, true, true, EpicFight.identifier( "skillbook"));

    private final boolean shouldSave;
    private final boolean shouldSynchronize;
    private final boolean modifiable;
    private final int id;
    private final ResourceLocation bookIcon;

    t0001SkillCategories(boolean shouldSave, boolean shouldSynchronize, boolean modifiable, ResourceLocation bookIcon) {
        this.shouldSave = shouldSave;
        this.shouldSynchronize = shouldSynchronize;
        this.modifiable = modifiable;
        this.id = SkillCategory.ENUM_MANAGER.assign(this);
        this.bookIcon = bookIcon;
    }

    @Override
    public boolean shouldSave() {
        return this.shouldSave;
    }

    @Override
    public boolean shouldSynchronize() {
        return this.shouldSynchronize;
    }

    @Override
    public boolean learnable() {
        return this.modifiable;
    }

    @Override
    public ResourceLocation bookIcon() {
        return this.bookIcon == null ? SkillCategory.DEFAULT_BOOK_ICON : this.bookIcon;
    }

    @Override
    public int universalOrdinal() {
        return this.id;
    }
}
