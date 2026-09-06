package sid.base.client.photon;

import com.lowdragmc.photon.client.gameobject.emitter.data.model.PhotonMesh;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import org.joml.Vector3f;
import org.joml.Vector4f;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.client.model.VertexBuilder;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;
import yesman.epicfight.client.renderer.patched.layer.WearableItemLayer;
import yesman.epicfight.world.capabilities.entitypatch.LivingEntityPatch;

import java.util.List;

public class EFPhotonMeshUtil {

    public static PhotonMesh bakeEntityWithArmor(LivingEntityPatch<?> entityPatch, SkinnedMesh bodyMesh) {
        Armature armature = entityPatch.getArmature();
        OpenMatrix4f[] poses = armature.getPoseMatrices();
        PhotonMesh.Builder builder = new PhotonMesh.Builder();

        bakeMeshInto(builder, bodyMesh, armature, poses);

        for (ItemStack itemstack : entityPatch.getOriginal().getArmorSlots()) {
            if (!(itemstack.getItem() instanceof ArmorItem)) continue;
            SkinnedMesh armorMesh = WearableItemLayer.getCachedModel(itemstack.getItem());
            if (armorMesh != null) {
                bakeMeshInto(builder, armorMesh, armature, poses);
            }
        }

        return builder.build();
    }

    private static float[] vertexFloats(SkinnedMesh mesh, VertexBuilder vi, OpenMatrix4f[] poses,
                                        Vector4f posScratch, Vector3f normScratch) {
        mesh.getVertexPosition(vi.position, posScratch, poses);
        mesh.getVertexNormal(vi.position, vi.normal, normScratch, poses);
        float[] uvs = mesh.uvs();
        return new float[] {
                posScratch.x, posScratch.y, posScratch.z,
                uvs[vi.uv * 2], uvs[vi.uv * 2 + 1],
                normScratch.x, normScratch.y, normScratch.z
        };
    }

//    private static OpenMatrix4f[] combineWithOrigin(Armature armature, OpenMatrix4f[] poses) {
//        OpenMatrix4f[] combined = OpenMatrix4f.allocateMatrixArray(poses.length);
//        for (int i = 0; i < poses.length; i++) {
//            combined[i].load(poses[i]);
//            combined[i].mulBack(armature.searchJointById(i).getToOrigin());
//        }
//        return combined;
//    }

    public static void bakeMeshInto(PhotonMesh.Builder builder, SkinnedMesh mesh, Armature armature, OpenMatrix4f[] rawPoses) {
        OpenMatrix4f[] combined = OpenMatrix4f.allocateMatrixArray(rawPoses.length);
        Vector4f pos = new Vector4f();
        Vector3f norm = new Vector3f();

        for (SkinnedMesh.SkinnedMeshPart part : mesh.getAllParts()) {
            if (part.isHidden()) continue;

            OpenMatrix4f transform = part.getVanillaPartTransform();
            for (int i = 0; i < rawPoses.length; i++) {
                combined[i].load(rawPoses[i]);
                combined[i].mulBack(armature.searchJointById(i).getToOrigin());
                if (transform != null) {
                    combined[i].mulBack(transform);
                }
            }

            List<VertexBuilder> verts = part.getVertices();
            for (int i = 0; i + 2 < verts.size(); i += 3) {
                float[] a = vertexFloats(mesh, verts.get(i),     combined, pos, norm);
                float[] b = vertexFloats(mesh, verts.get(i + 1), combined, pos, norm);
                float[] c = vertexFloats(mesh, verts.get(i + 2), combined, pos, norm);
                builder.triangle(a, b, c);
            }
        }
    }


}
