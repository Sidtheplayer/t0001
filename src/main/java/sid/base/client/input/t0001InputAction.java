package sid.base.client.input;

import net.minecraft.client.KeyMapping;
import org.jetbrains.annotations.NotNull;
import yesman.epicfight.api.client.input.action.InputAction;
import yesman.epicfight.api.client.input.controller.ControllerBinding;

import java.util.Optional;

@SuppressWarnings("UnstableApiUsage")
public enum t0001InputAction implements InputAction {
    shadow_clone,
    kunai_throw,
    Awakening;

    final private int id;

    t0001InputAction() {
        this.id = InputAction.ENUM_MANAGER.assign(this);
    }

    @Override
   public @NotNull KeyMapping keyMapping() {
        return switch (this){
            case shadow_clone -> t0001KeyMappings.SHADOW_CLONE;
            case kunai_throw ->  t0001KeyMappings.DAGGER_THROW;
            case  Awakening -> t0001KeyMappings.SUPER_SKILL;
        };
    }



    @Override
   public @NotNull Optional<@NotNull ControllerBinding> controllerBinding() {
        return Optional.empty();
    }

    @Override
    public int universalOrdinal() {
        return this.id;
    }
}
