package sid.t0001.client.mesh;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.Nullable;
import yesman.epicfight.api.client.model.MeshPartDefinition;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.client.model.VertexBuilder;

import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class DarknessEntittyMesh extends SkinnedMesh {

    public final SkinnedMeshPart body;

    public DarknessEntittyMesh(@Nullable Map<String, Number[]> arrayMap, @Nullable Map<MeshPartDefinition, List<VertexBuilder>> partBuilders, @Nullable SkinnedMesh parent, RenderProperties properties) {
        super(arrayMap, partBuilders, parent, properties);

        this.body = this.getOrLogException(this.parts, "noGroups");
    }
}
