package sid.base.gameasset.animations;


import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.utils.TimePairList;

public class CustomAnimationProperties {

    public static class SSSpecialAnimationProperty<T> extends AnimationProperty.ActionAnimationProperty<T> {

        public SSSpecialAnimationProperty(){
            this(null,null);
        }

        public SSSpecialAnimationProperty(String name, @Nullable Codec<T> codecs) {
            super(name, codecs);
        }

        @SuppressWarnings("unused")
        public SSSpecialAnimationProperty(String name) {
            super();
        }

        public static final SSSpecialAnimationProperty<TimePairList> CUTSCENE_EXECUTION_HANDLE_TIME = new SSSpecialAnimationProperty<>();

        public static final SSSpecialAnimationProperty<TimePairList> NO_PHYSICS_TIME = new SSSpecialAnimationProperty<>();

    }

}
