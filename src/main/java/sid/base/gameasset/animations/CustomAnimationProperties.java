package sid.base.gameasset.animations;


import com.mojang.serialization.Codec;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.api.animation.property.AnimationProperty;
import yesman.epicfight.api.utils.TimePairList;

public class CustomAnimationProperties {

    public static class ProtectedHitAnimationProperty<T> extends AnimationProperty.ActionAnimationProperty<T> {

        public ProtectedHitAnimationProperty(){
            this(null,null);
        }

        public ProtectedHitAnimationProperty(String name, @Nullable Codec<T> codecs) {
            super(name, codecs);
        }

        public ProtectedHitAnimationProperty(String name) {
            super();
        }

        public static final ProtectedHitAnimationProperty<TimePairList> CUTSCENE_EXECUTION_HANDLE_TIME = new ProtectedHitAnimationProperty<> ();


    }

}
