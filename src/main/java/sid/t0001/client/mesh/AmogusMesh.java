package sid.t0001.client.mesh;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import yesman.epicfight.api.client.model.MeshPartDefinition;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.client.model.VertexBuilder;

import java.util.List;
import java.util.Map;

@OnlyIn(Dist.CLIENT)
public class AmogusMesh extends SkinnedMesh {

    public final SkinnedMeshPart body;

    public AmogusMesh(Map<String, Number[]> arrayMap, Map<MeshPartDefinition, List<VertexBuilder>> parts, SkinnedMesh parent, RenderProperties properties) {
        super(arrayMap, parts, parent, properties);

    this.body = this.getOrLogException(this.parts, "noGroups");
    }

}
