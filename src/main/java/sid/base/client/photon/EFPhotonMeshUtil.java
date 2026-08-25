package sid.base.client.photon;

import com.lowdragmc.photon.client.gameobject.emitter.data.model.PhotonMesh;
import org.joml.Vector3f;
import org.joml.Vector4f;
import yesman.epicfight.api.client.model.SkinnedMesh;
import yesman.epicfight.api.client.model.VertexBuilder;
import yesman.epicfight.api.model.Armature;
import yesman.epicfight.api.utils.math.OpenMatrix4f;

import java.util.List;

public class EFPhotonMeshUtil {

    public static PhotonMesh bakeSkinnedMesh(SkinnedMesh mesh, Armature armature, OpenMatrix4f[] rawPoses) {
        OpenMatrix4f[] poses = combineWithOrigin(armature, rawPoses);
        PhotonMesh.Builder builder = new PhotonMesh.Builder();
        Vector4f positions = new Vector4f();
        Vector3f normals = new Vector3f();

        for (SkinnedMesh.SkinnedMeshPart part : mesh.getAllParts()) {
            if (part.isHidden()) continue;
            List<VertexBuilder> verts = part.getVertices();

            for (int i = 0; i + 2 < verts.size(); i += 3) {
                float[] a = vertexFloats(mesh, verts.get(i),     poses, positions, normals);
                float[] b = vertexFloats(mesh, verts.get(i + 1), poses, positions, normals);
                float[] c = vertexFloats(mesh, verts.get(i + 2), poses, positions, normals);
                builder.triangle(a, b, c);
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

    private static OpenMatrix4f[] combineWithOrigin(Armature armature, OpenMatrix4f[] poses) {
        OpenMatrix4f[] combined = OpenMatrix4f.allocateMatrixArray(poses.length);
        for (int i = 0; i < poses.length; i++) {
            combined[i].load(poses[i]);
            combined[i].mulBack(armature.searchJointById(i).getToOrigin());
        }
        return combined;
    }



}
