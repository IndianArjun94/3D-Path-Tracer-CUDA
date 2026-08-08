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
            Material matBlueSphere = new Material(new Vec3(255, 255, 255), 0.0f, 0.0f, 0.9f, 1.3f);
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
//            spheres.add(new Sphere(new Vec3(2.0f, -1.4f, -3.5f).add(positionOffset), 1.05f, matBlueSphere));    // Glossy Deep Blue (Right)
            spheres.add(new Sphere(new Vec3(0f, 0f, 2f).add(positionOffset), 1.05f, matBlueSphere));    // Glossy Deep Blue (Right)
            spheres.add(new Sphere(new Vec3(-1.8f, 1.3f, -4.8f).add(positionOffset), 0.7f, matVioletSphere));  // Violet (Upper Left)
            spheres.add(new Sphere(new Vec3(1.8f, 1.3f, -4.8f).add(positionOffset), 0.7f, matYellowSphere));   // Yellow (Upper Right)


// --- LIGHTS ---
            pointLights.add(new PointLight(new Vec3(0.0f, 3.8f, -4.5f).add(positionOffset), new Vec3(255, 255, 255), 20));
            pointLights.add(new PointLight(new Vec3(3.0f, 3.8f, -3.0f).add(positionOffset), new Vec3(255, 255, 255), 50));
            pointLights.add(new PointLight(new Vec3(-3.0f, 3.8f, -3.0f).add(positionOffset), new Vec3(255, 255, 255), 20));
        } else if (scene == 2) {
            // --- ROOM MATERIALS (all diffuse, 4-arg -> default ior) ---
            Material matFloor   = new Material(new Vec3(205, 205, 205), 0f, 0.85f, 0f);
            Material matCeiling = new Material(new Vec3(205, 205, 205), 0f, 0.90f, 0f);
            Material matBack    = new Material(new Vec3(190, 190, 190), 0f, 0.60f, 0f);
            Material matLeft    = new Material(new Vec3(215,  65,  65), 0f, 0.50f, 0f); // red
            Material matRight   = new Material(new Vec3( 70, 200,  95), 0f, 0.50f, 0f); // green

            // --- OBJECT MATERIALS ---
            Material clearGlass  = new Material(new Vec3(255, 255, 255), 0f, 0.00f, 1.0f, 1.5f); // no tint (color^d = 1)
            Material amberGlass  = new Material(new Vec3(255, 190,  95), 0f, 0.00f, 1.0f, 1.5f); // absorbs blue/green -> amber
            Material blueGlass   = new Material(new Vec3(130, 175, 255), 0f, 0.00f, 1.0f, 1.5f); // absorbs red -> blue
            Material gold        = new Material(new Vec3(255, 215, 120), 1f, 0.05f, 0f);         // near-mirror gold
            Material chrome      = new Material(new Vec3(250, 250, 250), 1f, 0.00f, 0f);         // perfect mirror
            Material copperRough = new Material(new Vec3(240, 150, 110), 1f, 0.20f, 0f);         // brushed copper
            Material glossyBlue  = new Material(new Vec3( 70, 120, 230), 0f, 0.08f, 0f);         // plastic (spec + diffuse)
            Material diffuseTeal = new Material(new Vec3( 60, 200, 175), 0f, 0.80f, 0f);
            Material diffuseMag  = new Material(new Vec3(225,  90, 200), 0f, 0.85f, 0f);
            Material purpleBox   = new Material(new Vec3(150, 110, 225), 0f, 0.70f, 0f);

            // --- ROOM (floor y=-3, ceiling y=4, walls x=+-4.5, back z=-7, front open z=0) ---
            addQuad(triangles, v(-4.5f,-3f,0f), v(4.5f,-3f,0f), v(4.5f,-3f,-7f), v(-4.5f,-3f,-7f), matFloor,   positionOffset);
            addQuad(triangles, v(-4.5f, 4f,-7f), v(4.5f,4f,-7f), v(4.5f,4f,0f),  v(-4.5f,4f,0f),   matCeiling, positionOffset);
            addQuad(triangles, v(4.5f,-3f,-7f), v(4.5f,4f,-7f), v(-4.5f,4f,-7f), v(-4.5f,-3f,-7f), matBack,    positionOffset);
            addQuad(triangles, v(-4.5f,-3f,-7f), v(-4.5f,4f,-7f), v(-4.5f,4f,0f), v(-4.5f,-3f,0f), matLeft,    positionOffset);
            addQuad(triangles, v(4.5f,-3f,0f), v(4.5f,4f,0f), v(4.5f,4f,-7f), v(4.5f,-3f,-7f),     matRight,   positionOffset);

            // --- STRUCTURES ---
            addBox(triangles, v(-4.2f,-3.0f,-6.7f), v(-3.0f,-1.5f,-5.5f), chrome,    positionOffset); // mirror cube, back-left
            addBox(triangles, v( 3.0f,-3.0f,-6.7f), v( 4.2f,-1.7f,-5.5f), purpleBox, positionOffset); // matte cube, back-right
            addPyramid(triangles, v(-0.8f,-3.0f,-6.6f), v(0.8f,-3.0f,-5.4f), -1.3f, gold, positionOffset); // gold pyramid, back-center

            // --- SPHERES (on floor: center.y = -3 + radius) ---
            spheres.add(new Sphere(v( 0.0f,-1.30f,-3.5f).add(positionOffset), 1.70f, clearGlass));   // big clear glass showcase
            spheres.add(new Sphere(v(-2.8f,-2.35f,-2.4f).add(positionOffset), 0.65f, amberGlass));   // amber glass (absorption)
            spheres.add(new Sphere(v( 2.8f,-2.35f,-2.4f).add(positionOffset), 0.65f, blueGlass));    // blue glass (absorption)
            spheres.add(new Sphere(v(-3.4f,-2.50f,-4.6f).add(positionOffset), 0.50f, gold));         // gold metal
            spheres.add(new Sphere(v( 3.4f,-2.50f,-4.6f).add(positionOffset), 0.50f, chrome));       // chrome mirror
            spheres.add(new Sphere(v(-1.9f,-2.55f,-1.3f).add(positionOffset), 0.45f, copperRough));  // brushed copper
            spheres.add(new Sphere(v( 1.9f,-2.55f,-1.3f).add(positionOffset), 0.45f, glossyBlue));   // glossy plastic
            spheres.add(new Sphere(v( 0.0f,-2.60f,-1.0f).add(positionOffset), 0.40f, diffuseTeal));  // matte teal
            spheres.add(new Sphere(v(-1.5f, 0.80f,-5.3f).add(positionOffset), 0.55f, clearGlass));   // floating clear glass
            spheres.add(new Sphere(v( 1.5f, 1.00f,-5.3f).add(positionOffset), 0.50f, diffuseMag));   // floating matte magenta

            // --- LIGHTS ---
            pointLights.add(new PointLight(v( 0.0f, 3.8f, -4.5f).add(positionOffset), new Vec3(255, 255, 255), 30));
            pointLights.add(new PointLight(v( 3.0f, 3.8f, -3.0f).add(positionOffset), new Vec3(255, 240, 220), 40));
            pointLights.add(new PointLight(v(-3.0f, 3.8f, -3.0f).add(positionOffset), new Vec3(220, 235, 255), 40));
        } else if (scene == 3) {
            // --- ROOM MATERIALS ---
            Material matFloor   = new Material(new Vec3(210, 210, 210), 0f, 0.85f, 0f);
            Material matCeiling = new Material(new Vec3(200, 200, 200), 0f, 0.90f, 0f);
            Material matBack    = new Material(new Vec3(185, 185, 185), 0f, 0.55f, 0f);
            Material matLeft    = new Material(new Vec3(210,  70,  70), 0f, 0.50f, 0f);
            Material matRight   = new Material(new Vec3( 70, 195,  95), 0f, 0.50f, 0f);

            // --- OBJECT MATERIALS ---
            Material clearGlass = new Material(new Vec3(255, 255, 255), 0f, 0f, 1.0f, 1.5f);
            Material amberGlass = new Material(new Vec3(255, 200, 110), 0f, 0f, 1.0f, 1.5f);
            Material blueGlass  = new Material(new Vec3(140, 180, 255), 0f, 0f, 1.0f, 1.5f);
            Material roseGlass  = new Material(new Vec3(255, 150, 190), 0f, 0f, 1.0f, 1.3f);
            Material gold       = new Material(new Vec3(255, 215, 120), 1f, 0.05f, 0f);
            Material chrome     = new Material(new Vec3(250, 250, 250), 1f, 0.00f, 0f);
            Material copper     = new Material(new Vec3(240, 150, 110), 1f, 0.15f, 0f);
            Material glossy     = new Material(new Vec3( 70, 120, 230), 0f, 0.08f, 0f);

            // small matte palette for the sphere grid
            Material[] mattes = new Material[] {
                    new Material(new Vec3(230, 100, 100), 0f, 0.8f, 0f),
                    new Material(new Vec3(100, 200, 150), 0f, 0.8f, 0f),
                    new Material(new Vec3(120, 140, 240), 0f, 0.8f, 0f),
                    new Material(new Vec3(240, 200,  90), 0f, 0.8f, 0f),
                    new Material(new Vec3(220, 120, 220), 0f, 0.8f, 0f),
                    new Material(new Vec3( 90, 210, 220), 0f, 0.8f, 0f),
            };

            // --- ROOM (wider & deeper so the grid is visible) ---
            addQuad(triangles, v(-6f,-3f,1f),  v(6f,-3f,1f),  v(6f,-3f,-10f),  v(-6f,-3f,-10f),  matFloor,   positionOffset);
            addQuad(triangles, v(-6f, 5f,-10f), v(6f,5f,-10f), v(6f,5f,1f),    v(-6f,5f,1f),     matCeiling, positionOffset);
            addQuad(triangles, v(6f,-3f,-10f), v(6f,5f,-10f), v(-6f,5f,-10f),  v(-6f,-3f,-10f),  matBack,    positionOffset);
            addQuad(triangles, v(-6f,-3f,-10f), v(-6f,5f,-10f), v(-6f,5f,1f),  v(-6f,-3f,1f),    matLeft,    positionOffset);
            addQuad(triangles, v(6f,-3f,1f),  v(6f,5f,1f),   v(6f,5f,-10f),   v(6f,-3f,-10f),   matRight,   positionOffset);

            // --- TRIANGLE-HEAVY CENTERPIECES (back, so they don't block) ---
            addZiggurat(triangles, v(0f,-3f,-7.5f), 2.6f, 0.55f, 4, gold, positionOffset);       // stepped pyramid = 4*10 tris
            addOctahedron(triangles, v(-3.5f,-1.9f,-6.0f), 0.9f, chrome, positionOffset);        // 8 tris, floating mirror
            addOctahedron(triangles, v( 3.5f,-1.9f,-6.0f), 0.9f, copper, positionOffset);        // 8 tris, floating copper
            addBox(triangles, v(-5.4f,-3f,-9.4f), v(-4.4f,-0.5f,-8.4f), glossy, positionOffset); // tall thin pillar L
            addBox(triangles, v( 4.4f,-3f,-9.4f), v( 5.4f,-0.5f,-8.4f), glossy, positionOffset); // tall thin pillar R

            // --- GLASS SHOWCASE (small, spread out front) ---
            spheres.add(new Sphere(v(-1.4f,-2.45f,-2.0f).add(positionOffset), 0.55f, clearGlass));
            spheres.add(new Sphere(v( 0.0f,-2.45f,-1.4f).add(positionOffset), 0.55f, amberGlass));
            spheres.add(new Sphere(v( 1.4f,-2.45f,-2.0f).add(positionOffset), 0.55f, blueGlass));
            spheres.add(new Sphere(v( 0.0f,-1.45f,-3.0f).add(positionOffset), 1.0f, roseGlass)); // one slightly bigger, still see-through

            // --- METAL ACCENTS ---
            spheres.add(new Sphere(v(-4.3f,-2.55f,-3.0f).add(positionOffset), 0.45f, gold));
            spheres.add(new Sphere(v( 4.3f,-2.55f,-3.0f).add(positionOffset), 0.45f, chrome));

            // --- MATTE SPHERE GRID (small, low, fills the floor) ---
            int gi = 0;
            for (int gx = -2; gx <= 2; gx++) {
                for (int gz = 0; gz <= 3; gz++) {
                    float px = gx * 1.15f;
                    float pz = -4.5f - gz * 1.15f;
                    // skip cells near the centerpiece so it doesn't overlap
                    if (Math.abs(px) < 1.6f && pz < -6.5f) continue;
                    float r = 0.28f;
                    spheres.add(new Sphere(v(px, -3f + r, pz).add(positionOffset), r, mattes[gi % mattes.length]));
                    gi++;
                }
            }

            // --- LIGHTS ---
            pointLights.add(new PointLight(v( 0.0f, 4.8f, -5.0f).add(positionOffset), new Vec3(255, 255, 255), 45));
            pointLights.add(new PointLight(v( 4.0f, 4.8f, -2.5f).add(positionOffset), new Vec3(255, 240, 220), 45));
            pointLights.add(new PointLight(v(-4.0f, 4.8f, -2.5f).add(positionOffset), new Vec3(220, 235, 255), 45));
        }
    }

    // small vertex shorthand
    private static Vec3 v(float x, float y, float z) { return new Vec3(x, y, z); }

    // one quad from 4 corners (winding-independent: the point-in-triangle test is self-consistent)
    private static void addQuad(ArrayList<Triangle> triangles, Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, Material m, Vec3 off) {
        triangles.add(new Triangle(p0.add(off), p1.add(off), p2.add(off), m));
        triangles.add(new Triangle(p0.add(off), p2.add(off), p3.add(off), m));
    }

    // axis-aligned box from min/max corners -> 12 triangles
    private static void addBox(ArrayList<Triangle> triangles, Vec3 min, Vec3 max, Material m, Vec3 off) {
        Vec3 a = v(min.x, min.y, min.z), b = v(max.x, min.y, min.z), c = v(max.x, min.y, max.z), d = v(min.x, min.y, max.z);
        Vec3 e = v(min.x, max.y, min.z), f = v(max.x, max.y, min.z), g = v(max.x, max.y, max.z), h = v(min.x, max.y, max.z);
        addQuad(triangles, a, b, c, d, m, off); // bottom
        addQuad(triangles, e, f, g, h, m, off); // top
        addQuad(triangles, a, b, f, e, m, off); // front  z=min
        addQuad(triangles, b, c, g, f, m, off); // right  x=max
        addQuad(triangles, c, d, h, g, m, off); // back   z=max
        addQuad(triangles, d, a, e, h, m, off); // left   x=min
    }

    // square pyramid: base rectangle on baseMin.y, apex centered at apexY
    private static void addPyramid(ArrayList<Triangle> triangles, Vec3 baseMin, Vec3 baseMax, float apexY, Material m, Vec3 off) {
        Vec3 a = v(baseMin.x, baseMin.y, baseMin.z), b = v(baseMax.x, baseMin.y, baseMin.z);
        Vec3 c = v(baseMax.x, baseMin.y, baseMax.z), d = v(baseMin.x, baseMin.y, baseMax.z);
        Vec3 apex = v((baseMin.x + baseMax.x) * 0.5f, apexY, (baseMin.z + baseMax.z) * 0.5f);
        addQuad(triangles, a, b, c, d, m, off); // base
        triangles.add(new Triangle(a.add(off), b.add(off), apex.add(off), m));
        triangles.add(new Triangle(b.add(off), c.add(off), apex.add(off), m));
        triangles.add(new Triangle(c.add(off), d.add(off), apex.add(off), m));
        triangles.add(new Triangle(d.add(off), a.add(off), apex.add(off), m));
    }

    // regular octahedron centered at c, "radius" = distance from center to each of the 6 points -> 8 triangles
    private static void addOctahedron(ArrayList<Triangle> triangles, Vec3 c, float r, Material m, Vec3 off) {
        Vec3 top = v(c.x, c.y + r, c.z), bot = v(c.x, c.y - r, c.z);
        Vec3 px = v(c.x + r, c.y, c.z), nx = v(c.x - r, c.y, c.z);
        Vec3 pz = v(c.x, c.y, c.z + r), nz = v(c.x, c.y, c.z - r);
        triangles.add(new Triangle(top.add(off), px.add(off), pz.add(off), m));
        triangles.add(new Triangle(top.add(off), pz.add(off), nx.add(off), m));
        triangles.add(new Triangle(top.add(off), nx.add(off), nz.add(off), m));
        triangles.add(new Triangle(top.add(off), nz.add(off), px.add(off), m));
        triangles.add(new Triangle(bot.add(off), pz.add(off), px.add(off), m));
        triangles.add(new Triangle(bot.add(off), nx.add(off), pz.add(off), m));
        triangles.add(new Triangle(bot.add(off), nz.add(off), nx.add(off), m));
        triangles.add(new Triangle(bot.add(off), px.add(off), nz.add(off), m));
    }

    // stepped pyramid: 'levels' stacked shrinking boxes on a base centered at (base.x, base.z), sitting on base.y
    private static void addZiggurat(ArrayList<Triangle> triangles, Vec3 base, float startHalf, float stepH, int levels, Material m, Vec3 off) {
        for (int l = 0; l < levels; l++) {
            float half = startHalf * (1f - (float) l / levels);
            float y0 = base.y + l * stepH;
            float y1 = y0 + stepH;
            addBox(triangles, v(base.x - half, y0, base.z - half), v(base.x + half, y1, base.z + half), m, off);
        }
    }
}