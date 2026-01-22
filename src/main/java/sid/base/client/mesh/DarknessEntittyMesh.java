package sid.base.client.mesh;


import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
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
// THIS IS A PURE MESH ENTITY without a blocky java model that vanilla has, meaning we can have almost any
// shape we want for the entity, as long as we make the model in a 3D modeling software and export it to a json model
