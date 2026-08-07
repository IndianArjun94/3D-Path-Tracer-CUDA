package arjun.path_tracer_cuda;

import arjun.path_tracer_cuda.geometry.*;

import java.util.ArrayList;

public class PresetScenes {
    public static void loadScene(int scene, ArrayList<Triangle> triangles, ArrayList<Sphere> spheres, ArrayList<PointLight> pointLights, Vec3 positionOffset) {
        if (scene == 1) {
            // --- MATERIALS ---
            Material matFloor = new Material(new Vec3(220, 220, 220), 0f, 0.7f, 0);
            Material matBackWall = new Material(new Vec3(160, 160, 160), 0f, 0.5f,0);
            Material matLeftWall = new Material(new Vec3(255, 65, 65), 1f, 0.4f, 0);
            Material matRightWall = new Material(new Vec3(65, 255, 65), 1f, 0.4f, 0);
            Material matCeiling = new Material(new Vec3(100, 100, 100), 0f, 0.9f, 0);

            Material matCenterSphere = new Material(new Vec3(255, 255, 255), 1f, 0.1f, 0f);
            Material matOrangeSphere = new Material(new Vec3(255, 165, 0), 1f, 0.8f, 0);
            Material matBlueSphere = new Material(new Vec3(0, 191, 255), 0.0f, 0.6f, 1f);
            Material matVioletSphere = new Material(new Vec3(238, 130, 238), 1f, 0.4f, 0);
            Material matYellowSphere = new Material(new Vec3(255, 255, 0), 1f, 0.2f, 0);

// --- PLANES (Converted to Triangles) ---

// Floor
            triangles.add(new Triangle(new Vec3(-4.5f, -3.0f, 0.0f).add(positionOffset), new Vec3(4.5f, -3.0f, 0.0f).add(positionOffset), new Vec3(4.5f, -3.0f, -7.0f).add(positionOffset), matFloor));
            triangles.add(new Triangle(new Vec3(-4.5f, -3.0f, 0.0f).add(positionOffset), new Vec3(4.5f, -3.0f, -7.0f).add(positionOffset), new Vec3(-4.5f, -3.0f, -7.0f).add(positionOffset), matFloor));

// Back Wall
            triangles.add(new Triangle(new Vec3(4.5f, -3.0f, -7.0f).add(positionOffset), new Vec3(4.5f, 4.0f, -7.0f).add(positionOffset), new Vec3(-4.5f, 4.0f, -7.0f).add(positionOffset), matBackWall));
            triangles.add(new Triangle(new Vec3(4.5f, -3.0f, -7.0f).add(positionOffset), new Vec3(-4.5f, 4.0f, -7.0f).add(positionOffset), new Vec3(-4.5f, -3.0f, -7.0f).add(positionOffset), matBackWall));

// Left Wall
            triangles.add(new Triangle(new Vec3(-4.5f, -3.0f, -7.0f).add(positionOffset), new Vec3(-4.5f, 4.0f, -7.0f).add(positionOffset), new Vec3(-4.5f, 4.0f, 0.0f).add(positionOffset), matLeftWall));
            triangles.add(new Triangle(new Vec3(-4.5f, -3.0f, -7.0f).add(positionOffset), new Vec3(-4.5f, 4.0f, 0.0f).add(positionOffset), new Vec3(-4.5f, -3.0f, 0.0f).add(positionOffset), matLeftWall));

// Right Wall
            triangles.add(new Triangle(new Vec3(4.5f, -3.0f, 0.0f).add(positionOffset), new Vec3(4.5f, 4.0f, 0.0f).add(positionOffset), new Vec3(4.5f, 4.0f, -7.0f).add(positionOffset), matRightWall));
            triangles.add(new Triangle(new Vec3(4.5f, -3.0f, 0.0f).add(positionOffset), new Vec3(4.5f, 4.0f, -7.0f).add(positionOffset), new Vec3(4.5f, -3.0f, -7.0f).add(positionOffset), matRightWall));

// Ceiling
            triangles.add(new Triangle(new Vec3(-4.5f, 4.0f, -7.0f).add(positionOffset), new Vec3(4.5f, 4.0f, -7.0f).add(positionOffset), new Vec3(4.5f, 4.0f, 0.0f).add(positionOffset), matCeiling));
            triangles.add(new Triangle(new Vec3(-4.5f, 4.0f, -7.0f).add(positionOffset), new Vec3(4.5f, 4.0f, 0.0f).add(positionOffset), new Vec3(-4.5f, 4.0f, 0.0f).add(positionOffset), matCeiling));


// --- SPHERES ---
            spheres.add(new Sphere(new Vec3(0.0f, -1.0f, -4.2f).add(positionOffset), 1.1f, matCenterSphere));
            spheres.add(new Sphere(new Vec3(-2.0f, -2.2f, -3.5f).add(positionOffset), 0.5f, matOrangeSphere)); // Glossy Orange (Left)
            spheres.add(new Sphere(new Vec3(2.0f, -1.4f, -3.5f).add(positionOffset), 1.05f, matBlueSphere));    // Glossy Deep Blue (Right)
            spheres.add(new Sphere(new Vec3(-1.8f, 1.3f, -4.8f).add(positionOffset), 0.7f, matVioletSphere));  // Violet (Upper Left)
            spheres.add(new Sphere(new Vec3(1.8f, 1.3f, -4.8f).add(positionOffset), 0.7f, matYellowSphere));   // Yellow (Upper Right)


// --- LIGHTS ---
            pointLights.add(new PointLight(new Vec3(0.0f, 3.8f, -4.5f).add(positionOffset), new Vec3(255, 255, 255), 20));
            pointLights.add(new PointLight(new Vec3(3.0f, 3.8f, -3.0f).add(positionOffset), new Vec3(255, 255, 255), 50));
            pointLights.add(new PointLight(new Vec3(-3.0f, 3.8f, -3.0f).add(positionOffset), new Vec3(255, 255, 255), 20));
        }
    }
}
