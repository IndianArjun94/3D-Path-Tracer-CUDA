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
            Material matBlueSphere = new Material(new Vec3(0, 225, 255), 0.0f, 0.3f, 1.0f, 1.3f, 1f);
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
            spheres.add(new Sphere(new Vec3(2.2f, -1.4f, -3.5f).add(positionOffset), 1.05f, matBlueSphere));    // Glossy Deep Blue (Right)
            spheres.add(new Sphere(new Vec3(-1.8f, 1.3f, -4.8f).add(positionOffset), 0.7f, matVioletSphere));  // Violet (Upper Left)
            spheres.add(new Sphere(new Vec3(1.8f, 1.3f, -4.8f).add(positionOffset), 0.7f, matYellowSphere));   // Yellow (Upper Right)


            // --- LIGHTS ---
            pointLights.add(new PointLight(new Vec3(0.0f, 3.8f, -4.5f).add(positionOffset), new Vec3(255, 255, 255), 20));
            pointLights.add(new PointLight(new Vec3(3.0f, 3.8f, -3.0f).add(positionOffset), new Vec3(255, 255, 255), 50));
            pointLights.add(new PointLight(new Vec3(-3.0f, 3.8f, -3.0f).add(positionOffset), new Vec3(255, 255, 255), 20));
        } else if (scene == 2) {
            Material matFloor   = new Material(new Vec3(205, 205, 205), 0f, 0.85f, 0f);
            Material matCeiling = new Material(new Vec3(205, 205, 205), 0f, 0.90f, 0f);
            Material matBack    = new Material(new Vec3(190, 190, 190), 0f, 0.60f, 0f);
            Material matLeft    = new Material(new Vec3(215,  65,  65), 0f, 0.50f, 0f); // red
            Material matRight   = new Material(new Vec3( 70, 200,  95), 0f, 0.50f, 0f); // green

            // --- OBJECT MATERIALS ---
            Material clearGlass  = new Material(new Vec3(255, 255, 255), 0f, 0.00f, 1.0f, 1.5f, 1.0f); // no tint (color^d = 1)
            Material amberGlass  = new Material(new Vec3(255, 190,  95), 0f, 0.00f, 1.0f, 1.5f, 1.0f); // absorbs blue/green -> amber
            Material blueGlass   = new Material(new Vec3(130, 175, 255), 0f, 0.00f, 1.0f, 1.5f, 1.0f); // absorbs red -> blue
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
            Material clearGlass = new Material(new Vec3(255, 255, 255), 0f, 0f, 1.0f, 1.5f, 1.0f);
            Material amberGlass = new Material(new Vec3(255, 200, 110), 0f, 0f, 1.0f, 1.5f, 1.0f);
            Material blueGlass  = new Material(new Vec3(140, 180, 255), 0f, 0f, 1.0f, 1.5f, 1.0f);
            Material roseGlass  = new Material(new Vec3(255, 150, 190), 0f, 0f, 1.0f, 1.3f, 1.0f);
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
        } else if (scene == 4) {
            // --- ROOM MATERIALS (opaque) ---
            Material matFloor   = new Material(new Vec3(125, 125, 125), 0f, 0.1f, 0f);
            Material matCeiling = new Material(new Vec3(200, 200, 200), 0f, 0.25f, 0f);
            Material matBack    = new Material(new Vec3(185, 185, 185), 0f, 0.3f, 0f);
            Material matLeft    = new Material(new Vec3(210,  70,  70), 0f, 0.50f, 0f);
            Material matRight   = new Material(new Vec3( 70, 195,  95), 0f, 0.50f, 0f);

            // --- GLASS MATERIALS (transmission = 1, tuned density per object size) ---
            // args: color, metallic, roughness, transmission, ior, density
            Material glassClear = new Material(new Vec3(255, 255, 255), 0f, 0f, 1.0f, 1.5f, 1.0f);
            Material glassAmber = new Material(new Vec3(255, 200, 110), 0f, 0f, 1.0f, 1.5f, 1.5f);
            Material glassBlue  = new Material(new Vec3(130, 180, 255), 0f, 0f, 1.0f, 1.5f, 1.5f);
            Material glassRose  = new Material(new Vec3(255, 150, 190), 0f, 0f, 1.0f, 1.4f, 1.5f);
            Material glassTeal  = new Material(new Vec3(120, 240, 220), 0f, 0f, 1.0f, 1.5f, 1.5f);
            Material glassLime  = new Material(new Vec3(190, 240, 120), 0f, 0f, 1.0f, 1.45f, 1.5f);
            Material glassViolet= new Material(new Vec3(200, 150, 255), 0f, 0f, 1.0f, 1.5f, 1.5f);

            // higher density for the small glass triangle boxes (short chords need it to show tint)
            Material glassBoxAmber = new Material(new Vec3(255, 195, 110), 0f, 0f, 1.0f, 1.5f, 2.5f);
            Material glassBoxBlue  = new Material(new Vec3(130, 180, 255), 0f, 0f, 1.0f, 1.5f, 2.5f);
            Material glassBoxGreen = new Material(new Vec3(150, 240, 150), 0f, 0f, 1.0f, 1.5f, 2.5f);

            // a couple of opaque accents so there's something solid to see through the glass
            Material gold   = new Material(new Vec3(255, 215, 120), 1f, 0.05f, 0f);
            Material chrome = new Material(new Vec3(250, 250, 250), 1f, 0.00f, 0f);
            Material matteMag = new Material(new Vec3(225, 90, 200), 0f, 0.85f, 0f);

            // --- ROOM ---
            addQuad(triangles, v(-6f,-3f,1f),  v(6f,-3f,1f),  v(6f,-3f,-11f),  v(-6f,-3f,-11f),  matFloor,   positionOffset);
            addQuad(triangles, v(-6f, 5f,-11f), v(6f,5f,-11f), v(6f,5f,1f),    v(-6f,5f,1f),     matCeiling, positionOffset);
            addQuad(triangles, v(6f,-3f,-11f), v(6f,5f,-11f), v(-6f,5f,-11f),  v(-6f,-3f,-11f),  matBack,    positionOffset);
            addQuad(triangles, v(-6f,-3f,-11f), v(-6f,5f,-11f), v(-6f,5f,1f),  v(-6f,-3f,1f),    matLeft,    positionOffset);
            addQuad(triangles, v(6f,-3f,1f),  v(6f,5f,1f),   v(6f,5f,-11f),   v(6f,-3f,-11f),   matRight,   positionOffset);

            // --- TRANSLUCENT TRIANGLE STRUCTURES (all CLOSED boxes = 2 surfaces per ray) ---
            // tall glass slab pillars (thin but closed -> ray still enters+exits)
            addBox(triangles, v(-5.3f,-3f,-9.5f), v(-4.5f,-0.2f,-8.7f), glassBoxAmber, positionOffset);
            addBox(triangles, v( 4.5f,-3f,-9.5f), v( 5.3f,-0.2f,-8.7f), glassBoxBlue,  positionOffset);
            // glass cubes on the floor
            addBox(triangles, v(-2.7f,-3f,-7.0f), v(-1.7f,-2.0f,-6.0f), glassBoxGreen, positionOffset);
            addBox(triangles, v( 1.7f,-3f,-7.0f), v( 2.7f,-2.0f,-6.0f), glassBoxAmber, positionOffset);
            // a closed glass triangular prism (still 2 surfaces along any ray)
//            addPrism(triangles, v(-0.9f,-3f,-8.2f), v(0.9f,-3f,-8.2f), 1.6f, 1.4f, glassBoxBlue, positionOffset);

            // --- OPAQUE ACCENTS behind/among the glass (things to refract) ---
            addBox(triangles, v(-0.6f,-3f,-9.8f), v(0.6f,-1.6f,-9.0f), matteMag, positionOffset);
            spheres.add(new Sphere(v(-4.6f,-2.55f,-6.0f).add(positionOffset), 0.45f, gold));
            spheres.add(new Sphere(v( 4.6f,-2.55f,-6.0f).add(positionOffset), 0.45f, chrome));

            // --- MANY TRANSLUCENT SPHERES ---
            // front showcase row (small, see-through)
            spheres.add(new Sphere(v(-2.2f,-2.45f,-2.2f).add(positionOffset), 0.55f, glassAmber));
            spheres.add(new Sphere(v(-0.75f,-2.45f,-1.7f).add(positionOffset), 0.55f, glassBlue));
            spheres.add(new Sphere(v( 0.75f,-2.45f,-1.7f).add(positionOffset), 0.55f, glassRose));
            spheres.add(new Sphere(v( 2.2f,-2.45f,-2.2f).add(positionOffset), 0.55f, glassTeal));

            // a big clear centerpiece (lens effect on everything behind it)
            spheres.add(new Sphere(v( 0.0f,-1.3f,-3.6f).add(positionOffset), 1.35f, glassClear));

            // mid ring of mixed-tint glass
            spheres.add(new Sphere(v(-3.3f,-2.35f,-3.6f).add(positionOffset), 0.65f, glassLime));
            spheres.add(new Sphere(v( 3.3f,-2.35f,-3.6f).add(positionOffset), 0.65f, glassViolet));
            spheres.add(new Sphere(v(-2.0f,-2.55f,-4.6f).add(positionOffset), 0.45f, glassBlue));
            spheres.add(new Sphere(v( 2.0f,-2.55f,-4.6f).add(positionOffset), 0.45f, glassAmber));

            // floating glass orbs (glass-behind-glass through the big lens)
            spheres.add(new Sphere(v(-1.4f, 1.1f,-6.2f).add(positionOffset), 0.6f, glassRose));
            spheres.add(new Sphere(v( 1.4f, 1.2f,-6.2f).add(positionOffset), 0.6f, glassTeal));
            spheres.add(new Sphere(v( 0.0f, 2.2f,-7.0f).add(positionOffset), 0.5f, glassClear));

            // --- LIGHTS ---
            pointLights.add(new PointLight(v( 0.0f, 4.8f, -5.0f).add(positionOffset), new Vec3(255, 255, 255), 65));
            pointLights.add(new PointLight(v( 4.0f, 4.8f, -2.5f).add(positionOffset), new Vec3(255, 245, 225), 35));
            pointLights.add(new PointLight(v(-4.0f, 4.8f, -2.5f).add(positionOffset), new Vec3(225, 240, 255), 35));
        } else if (scene == 5) {
            Material matFloor = new Material(new Vec3(255, 255, 255), 0, 0, 0);
            addQuad(triangles, v(-15.0f,-3f,-21f), v(15.0f,-3f,-21f), v(15.0f,-3f,1f), v(-15.0f,-3f,1f), matFloor, positionOffset);


            Material matSphere = new Material(new Vec3(255, 255, 255), 0, 0, 1f, 1.5f, 0.5f);
            spheres.add(new Sphere(v(-2.0f, -2.0f, -4.0f).add(positionOffset), 1.0f, matSphere));




            pointLights.add(new PointLight(v( 5.0f, 2f, -5.0f).add(positionOffset), new Vec3(255, 255, 255), 50));
        }

        if (scene == 6) {
            /* =====================================================================================
             * "SOLITAIRE" - a jewellery hero shot, not another box room.
             *
             * Structure (nothing here is a wall-and-spheres Cornell box):
             *   - an open floor plane, no ceiling and no side walls
             *   - a curved cyclorama sweeping around BEHIND the subject (no corners to catch shadow)
             *   - a bevelled circular pedestal the ring stands on
             *   - the ring itself: a tapered, superelliptical gold band with a true cylindrical bore
             *   - a real 57-facet round brilliant (table / star / bezel / upper-girdle / girdle /
             *     pavilion mains / lower-girdle), cut to GIA proportions, slightly blue
             *   - a platinum basket + six prongs actually gripping the girdle
             *   - white bounce cards parked BEHIND the camera (z > 0, so they can never be seen
             *     directly) purely so the metal has something bright and soft to reflect
             *
             * Nothing here interpenetrates: the basket rim and every prong sample are placed
             * against the stone's own solved outline plus a hair of clearance, because opaque
             * geometry buried inside a transmissive solid confuses the kernel's single inObject
             * flag and comes out as dark junk.
             *
             * Triangle budget lives in these constants - the band alone is ~3/4 of the scene.
             * Halve BAND_SEGMENTS/TUBE_SEGMENTS if the render is too slow; the shape survives.
             * ===================================================================================== */
            final int BAND_SEGMENTS   = 96; // around the finger hole
            final int TUBE_SEGMENTS   = 16; // around the band's cross-section
            final int GIRDLE_SEGMENTS = 64; // around the stone, must be a multiple of 16
            final int PEDESTAL_SIDES  = 64;

            // --- MATERIALS ---
            Material matFloor    = new Material(new Vec3( 24,  24,  28), 0f, 0.05f, 0f); // polished dark stone
            Material matCyc      = new Material(new Vec3(150, 152, 162), 0f, 0.85f, 0f); // matte grey sweep
            Material matPedestal = new Material(new Vec3( 38,  38,  44), 0f, 0.03f, 0f); // black granite
            Material matCard     = new Material(new Vec3(252, 252, 255), 0f, 0.95f, 0f); // studio bounce card

            Material gold     = new Material(new Vec3(255, 200, 120), 1f, 0.045f, 0f); // yellow gold shank
            Material platinum = new Material(new Vec3(238, 240, 245), 1f, 0.020f, 0f); // white head + prongs

            // slightly blue: near-white tint, real diamond IOR, low density so the blue only
            // builds up over the long total-internal-reflection paths inside the stone
            Material diamond = new Material(new Vec3(238, 246, 255), 0f, 0f, 1f, 2.417f, 0.5f);

            // --- LAYOUT ---
            final float FLOOR_Y    = -2.05f;
            final float PEDESTAL_Y = -1.40f; // top face: the ring rests exactly here
            final float STAGE_Z    = -3.00f;

            // --- SET ---
            addQuad(triangles, v(-16f, FLOOR_Y, 2f), v(16f, FLOOR_Y, 2f), v(16f, FLOOR_Y, -16f), v(-16f, FLOOR_Y, -16f), matFloor, positionOffset);

            // cyclorama: wraps from -88 deg to +88 deg so its edges fall outside the frustum
            addArcWall(triangles, v(0f, 0f, STAGE_Z), 6.0f, FLOOR_Y, FLOOR_Y + 13f, -88f, 88f, 32, matCyc, positionOffset);

            addBevelledCylinder(triangles, v(0f, PEDESTAL_Y, STAGE_Z), 1.50f, 0.65f, 0.07f, PEDESTAL_SIDES, matPedestal, positionOffset);

            // bounce cards, all at z > 0 (behind the camera). Primary rays always travel -z, so
            // these are invisible; they only ever show up as soft reflections in the metal.
            addQuad(triangles, v(-7f, -3f, 2.2f), v(7f, -3f, 2.2f), v(7f, 6f, 2.2f), v(-7f, 6f, 2.2f), matCard, positionOffset);
            addQuad(triangles, v(-5f, -2f, 0.2f), v(-5f, -2f, 5f), v(-5f, 5f, 5f), v(-5f, 5f, 0.2f), matCard, positionOffset);
            addQuad(triangles, v( 5f, -2f, 0.2f), v( 5f, -2f, 5f), v( 5f, 5f, 5f), v( 5f, 5f, 0.2f), matCard, positionOffset);

            // --- THE RING ---
            // Built upright in its own space (band in the XY plane, bore along Z, head at +Y),
            // then tilted and seated on the pedestal in one go.
            ArrayList<Triangle> ring = new ArrayList<>();

            final float BORE       = 0.95f;  // inner radius - stays a perfect cylinder as the band tapers
            final float THICK_TOP  = 0.085f, THICK_BOTTOM = 0.125f; // radial half-thickness
            final float WIDTH_TOP  = 0.115f, WIDTH_BOTTOM = 0.155f; // half-width along the bore

            addBand(ring, BORE, THICK_TOP, THICK_BOTTOM, WIDTH_TOP, WIDTH_BOTTOM, BAND_SEGMENTS, TUBE_SEGMENTS, gold);

            Brilliant cut = new Brilliant(0.66f);                 // girdle diameter, ~2ct proportions
            float shankTop = BORE + 2f * THICK_TOP;               // outer surface at the top of the shank
            float girdleY  = shankTop + 0.07f + cut.depth();      // culet clears the shank by 0.07

            addBrilliant(ring, v(0f, girdleY, 0f), cut, GIRDLE_SEGMENTS, diamond);

            // Basket: an open cone nesting the pavilion. Its base is narrower than the band is
            // wide so it stays buried in the shank, and its rim is sized off the stone's outline
            // at that exact height so the girdle overhangs it instead of cutting through it.
            float basketBase = shankTop - 0.05f;
            float basketTop  = girdleY - 0.045f;
            float basketRim  = cut.hullRadius(basketTop - girdleY) + 0.015f;
            float basketRise = basketTop - basketBase;
            addFrustum(ring, v(0f, basketBase, 0f), 0.10f, basketRim, basketRise, 24, platinum);

            // Six prongs: rise out of the basket, hug the girdle, fold onto the crown. Every
            // sample sits exactly one tube-radius plus CLEAR outside the stone's silhouette, and
            // the two girdle samples are split top/bottom so the straight run between them can't
            // chord its way back into the glass.
            final float CLEAR = 0.008f;
            float[] prongT  = { 0.050f, 0.046f, 0.043f, 0.041f, 0.034f };
            float[] prongDy = { -0.300f, -0.060f, -cut.girdleHalf, cut.girdleHalf, 0.055f };
            float[] prongR  = new float[prongT.length];

            // the lowest sample lands on the basket wall so the prong grows out of it
            prongR[0] = 0.10f + (basketRim - 0.10f) / basketRise * ((girdleY + prongDy[0]) - basketBase);
            for (int s = 1; s < prongR.length; s++) {
                prongR[s] = cut.hullRadius(prongDy[s]) + prongT[s] + CLEAR;
            }

            for (int p = 0; p < 6; p++) {
                double a = Math.PI * 2 * p / 6 + Math.toRadians(30);
                Vec3[] path = new Vec3[prongR.length];
                for (int s = 0; s < path.length; s++) {
                    path[s] = v((float) (prongR[s] * Math.cos(a)), girdleY + prongDy[s], (float) (prongR[s] * Math.sin(a)));
                }
                addTaperedTube(ring, path, prongT, 8, platinum);
            }

            // tip it towards the camera, swing it off-axis so the band reads as a solid, then drop
            // it until its lowest vertex is exactly on the pedestal
            orientAndSeat(ring, 12f, 22f, 0f, STAGE_Z, PEDESTAL_Y, positionOffset);
            triangles.addAll(ring);

            // three loose stones scattered on the pedestal
            spheres.add(new Sphere(v( 0.70f, PEDESTAL_Y + 0.05f, -2.20f).add(positionOffset), 0.05f, diamond));
            spheres.add(new Sphere(v(-0.55f, PEDESTAL_Y + 0.05f, -2.30f).add(positionOffset), 0.05f, diamond));
            spheres.add(new Sphere(v( 1.05f, PEDESTAL_Y + 0.05f, -2.45f).add(positionOffset), 0.05f, diamond));

            // --- LIGHTS (a full studio rig - each one costs a whole scene traversal per bounce) ---
            pointLights.add(new PointLight(v( 2.6f,  3.4f, -1.4f).add(positionOffset), new Vec3(255, 245, 230), 70)); // key, warm, high front-right
            pointLights.add(new PointLight(v(-3.2f,  1.2f, -0.8f).add(positionOffset), new Vec3(185, 210, 255), 45)); // fill, cool, low front-left
            pointLights.add(new PointLight(v(-1.6f,  2.4f, -6.2f).add(positionOffset), new Vec3(255, 255, 255), 55)); // rim, behind the stone -> fire
            pointLights.add(new PointLight(v( 3.6f, -0.9f, -5.2f).add(positionOffset), new Vec3(255,  80, 170), 26)); // magenta gel, grazing right
            pointLights.add(new PointLight(v(-3.8f, -0.9f, -5.0f).add(positionOffset), new Vec3( 70, 200, 255), 26)); // cyan gel, grazing left
            pointLights.add(new PointLight(v( 0.35f, 1.9f, -1.3f).add(positionOffset), new Vec3(255, 220, 165),  9)); // pin light, close in on the crown
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

    // closed triangular prism: a base edge from p0 to p1 along x, extruded in +z by 'depth', peaked at height 'h'.
    // 2 triangular caps + 3 rectangular sides = 8 triangles, fully closed.
    private static void addPrism(ArrayList<Triangle> triangles, Vec3 p0, Vec3 p1, float h, float depth, Material m, Vec3 off) {
        float apexX = (p0.x + p1.x) * 0.5f;
        // front face (z = p0.z)
        Vec3 fa = v(p0.x, p0.y, p0.z), fb = v(p1.x, p1.y, p0.z), fap = v(apexX, p0.y + h, p0.z);
        // back face (z = p0.z + depth)
        Vec3 ba = v(p0.x, p0.y, p0.z + depth), bb = v(p1.x, p1.y, p0.z + depth), bap = v(apexX, p0.y + h, p0.z + depth);
        // caps
        triangles.add(new Triangle(fa.add(off), fb.add(off), fap.add(off), m));
        triangles.add(new Triangle(ba.add(off), bb.add(off), bap.add(off), m));
        // three rectangular sides (as quads)
        addQuad(triangles, fa, fb, bb, ba, m, off);   // bottom
        addQuad(triangles, fa, fap, bap, ba, m, off); // left slope
        addQuad(triangles, fb, fap, bap, bb, m, off); // right slope
    }


    // ============================ scene 6: the ring ============================

    private static final Vec3 ORIGIN = new Vec3(0, 0, 0); // for builders that work in their own space

    private static float tan(float degrees) { return (float) Math.tan(Math.toRadians(degrees)); }

    private static float lerp(float a, float b, float t) { return a + (b - a) * t; }

    /* A round brilliant solved from GIA proportions, everything measured from the middle of the
     * girdle. The star tips and lower-girdle junctions are placed ON their neighbouring facet
     * planes rather than guessed, which is what keeps the bezel and pavilion kites from creasing. */
    private static class Brilliant {
        static final float TABLE_RADIUS = 0.285f; // table octagon, centre to corner
        static final float GIRDLE_HALF  = 0.015f; // half the girdle thickness
        static final float CROWN_ANGLE  = 34.50f;
        static final float PAV_ANGLE    = 40.75f;
        static final float STAR_LENGTH  = 0.50f;  // star facets reach half way out to the girdle
        static final float LOWER_LENGTH = 0.77f;  // lower halves reach 77% of the way to the culet

        final float girdleRadius, girdleHalf;
        final float tableRadius, yTable;
        final float starRadius, yStar;
        final float lowerRadius, yLower;
        final float yCulet;

        Brilliant(float diameter) {
            float cosHalf = (float) Math.cos(Math.PI / 8); // the 22.5 deg between a bezel and a star
            girdleRadius = diameter * 0.5f;
            girdleHalf   = diameter * GIRDLE_HALF;
            tableRadius  = diameter * TABLE_RADIUS;

            yTable = girdleHalf + (girdleRadius - tableRadius) * tan(CROWN_ANGLE);
            yCulet = -girdleHalf - girdleRadius * tan(PAV_ANGLE);

            // how far out the star tip reaches, then the height that puts it on the bezel plane
            float starReach = tableRadius + STAR_LENGTH * (girdleRadius - tableRadius);
            starRadius = starReach / cosHalf;
            yStar      = girdleHalf + (girdleRadius - starReach) * tan(CROWN_ANGLE);

            // same trick against the pavilion main plane
            float lowerReach = girdleRadius * (1f - LOWER_LENGTH);
            lowerRadius = lowerReach / cosHalf;
            yLower      = -girdleHalf - (girdleRadius - lowerReach) * tan(PAV_ANGLE);
        }

        // how far the culet sits below the middle of the girdle
        float depth() { return -yCulet; }

        /* The widest the stone ever gets at a given height: girdle -> star tip -> table corner
         * going up, girdle -> lower junction -> culet going down. A prong or a basket rim laid
         * against this can never cut into the glass, whatever azimuth it sits at. */
        float hullRadius(float dy) {
            if (dy <= girdleHalf && dy >= -girdleHalf) return girdleRadius;
            if (dy > 0) {
                if (dy <= yStar)  return lerp(girdleRadius, starRadius, (dy - girdleHalf) / (yStar - girdleHalf));
                if (dy <= yTable) return lerp(starRadius, tableRadius, (dy - yStar) / (yTable - yStar));
                return tableRadius;
            }
            if (dy >= yLower) return lerp(girdleRadius, lowerRadius, (dy + girdleHalf) / (yLower + girdleHalf));
            if (dy >= yCulet) return lerp(lowerRadius, 0f, (dy - yLower) / (yCulet - yLower));
            return 0f;
        }
    }

    // a point at (radius, angle) around a vertical axis through centre, lifted by dy
    private static Vec3 polar(Vec3 centre, float radius, double angle, float dy) {
        return v(centre.x + radius * (float) Math.cos(angle), centre.y + dy, centre.z + radius * (float) Math.sin(angle));
    }

    // squares off a circular cross-section a little: |c|^0.8 pushes the profile towards a rounded
    // rectangle, which is what a real band's edges look like
    private static float squarish(double c) {
        return (float) (Math.signum(c) * Math.pow(Math.abs(c), 0.8));
    }

    /* Ring band, built upright: the hole faces +z, the head sits at +y.
     * Two things make this read as jewellery rather than a doughnut:
     *   - the bore stays a perfect cylinder (the tube centre moves outward as the metal thickens),
     *     exactly how a shank is actually made
     *   - the shank tapers, thin under the head and heaviest at the bottom
     * uSegments x vSegments quads = 2 x that many triangles. */
    private static void addBand(ArrayList<Triangle> out, float bore, float thickTop, float thickBottom,
                                float widthTop, float widthBottom, int uSegments, int vSegments, Material m) {
        Vec3[][] grid = new Vec3[uSegments][vSegments];

        for (int i = 0; i < uSegments; i++) {
            double u = 2 * Math.PI * i / uSegments;
            float taper = (float) (0.5 - 0.5 * Math.sin(u)); // 0 at the top of the shank, 1 at the bottom
            float thick = thickTop + (thickBottom - thickTop) * taper;
            float width = widthTop + (widthBottom - widthTop) * taper;

            float centreRadius = bore + thick;
            float cu = (float) Math.cos(u), su = (float) Math.sin(u);

            for (int j = 0; j < vSegments; j++) {
                double vAngle = 2 * Math.PI * j / vSegments;
                float radius = centreRadius + thick * squarish(Math.cos(vAngle)); // radial: thickness
                grid[i][j] = v(radius * cu, radius * su, width * squarish(Math.sin(vAngle))); // along the bore: width
            }
        }

        for (int i = 0; i < uSegments; i++) {
            int i2 = (i + 1) % uSegments;
            for (int j = 0; j < vSegments; j++) {
                int j2 = (j + 1) % vSegments;
                addQuad(out, grid[i][j], grid[i2][j], grid[i2][j2], grid[i][j2], m, ORIGIN);
            }
        }
    }

    /* A full round brilliant, axis +y, girdle plane centred on girdleCentre. The eight-fold facet
     * structure is the real one - table, 8 stars, 8 bezel kites, 16 upper girdles, the girdle,
     * 8 pavilion mains, 16 lower girdles - but the girdle is bruted into girdleSegments instead of
     * 16, so the stone's outline reads as a circle rather than a coarse polygon. The girdle facets
     * fan onto that finer ring; everything else stays exactly planar.
     * -> 6 * girdleSegments + 48 triangles. */
    private static void addBrilliant(ArrayList<Triangle> out, Vec3 girdleCentre, Brilliant cut, int girdleSegments, Material m) {
        final int MAINS = 8;
        final double HALF_STEP = Math.PI / MAINS; // 22.5 deg
        int perGap = girdleSegments / MAINS;      // girdle segments between one bezel and the next

        Vec3[] table = new Vec3[MAINS], star = new Vec3[MAINS], lower = new Vec3[MAINS];
        for (int k = 0; k < MAINS; k++) {
            double a = 2 * Math.PI * k / MAINS;
            table[k] = polar(girdleCentre, cut.tableRadius, a, cut.yTable);
            star[k]  = polar(girdleCentre, cut.starRadius,  a + HALF_STEP, cut.yStar);
            lower[k] = polar(girdleCentre, cut.lowerRadius, a + HALF_STEP, cut.yLower);
        }

        Vec3[] girdleTop = new Vec3[girdleSegments], girdleBottom = new Vec3[girdleSegments];
        for (int j = 0; j < girdleSegments; j++) {
            double a = 2 * Math.PI * j / girdleSegments;
            girdleTop[j]    = polar(girdleCentre, cut.girdleRadius, a,  cut.girdleHalf);
            girdleBottom[j] = polar(girdleCentre, cut.girdleRadius, a, -cut.girdleHalf);
        }

        Vec3 tableCentre = v(girdleCentre.x, girdleCentre.y + cut.yTable, girdleCentre.z);
        Vec3 culet       = v(girdleCentre.x, girdleCentre.y + cut.yCulet, girdleCentre.z);

        for (int k = 0; k < MAINS; k++) {
            int next = (k + 1) % MAINS, prev = (k + MAINS - 1) % MAINS;
            int g = k * perGap; // the girdle point this bezel and pavilion main come down to

            out.add(new Triangle(tableCentre, table[k], table[next], m)); // table, fanned (all coplanar)
            out.add(new Triangle(table[k], table[next], star[k], m));     // star facet

            addQuad(out, table[k], star[prev], girdleTop[g], star[k], m, ORIGIN);   // bezel kite
            addQuad(out, culet, lower[prev], girdleBottom[g], lower[k], m, ORIGIN); // pavilion main

            for (int s = 0; s < perGap; s++) { // the two upper/lower girdle facets, fanned onto the round girdle
                int j0 = (g + s) % girdleSegments, j1 = (g + s + 1) % girdleSegments;
                out.add(new Triangle(star[k],  girdleTop[j0],    girdleTop[j1], m));
                out.add(new Triangle(lower[k], girdleBottom[j0], girdleBottom[j1], m));
            }
        }

        for (int j = 0; j < girdleSegments; j++) { // the girdle itself
            int j2 = (j + 1) % girdleSegments;
            addQuad(out, girdleTop[j], girdleTop[j2], girdleBottom[j2], girdleBottom[j], m, ORIGIN);
        }
    }

    // open cone (no caps), sitting on base, used for the setting's basket
    private static void addFrustum(ArrayList<Triangle> out, Vec3 base, float bottomRadius, float topRadius,
                                   float height, int sides, Material m) {
        for (int i = 0; i < sides; i++) {
            double a0 = 2 * Math.PI * i / sides, a1 = 2 * Math.PI * (i + 1) / sides;
            addQuad(out,
                    polar(base, bottomRadius, a0, 0f), polar(base, bottomRadius, a1, 0f),
                    polar(base, topRadius,    a1, height), polar(base, topRadius, a0, height), m, ORIGIN);
        }
    }

    /* Sweeps an n-gon along a path of centres with a per-sample radius and rounds off the far end.
     * Cross-sections stay horizontal, which is fine for something as near-vertical as a prong.
     * sides * (samples-1) * 2 + sides triangles. */
    private static void addTaperedTube(ArrayList<Triangle> out, Vec3[] centres, float[] radii, int sides, Material m) {
        Vec3[][] rings = new Vec3[centres.length][sides];
        for (int s = 0; s < centres.length; s++) {
            for (int i = 0; i < sides; i++) {
                rings[s][i] = polar(centres[s], radii[s], 2 * Math.PI * i / sides, 0f);
            }
        }

        for (int s = 0; s + 1 < centres.length; s++) {
            for (int i = 0; i < sides; i++) {
                int i2 = (i + 1) % sides;
                addQuad(out, rings[s][i], rings[s][i2], rings[s + 1][i2], rings[s + 1][i], m, ORIGIN);
            }
        }

        // rounded tip: one apex carried on past the last ring, along the path's own direction
        Vec3 last = centres[centres.length - 1], before = centres[centres.length - 2];
        float dx = last.x - before.x, dy = last.y - before.y, dz = last.z - before.z;
        float len = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
        float tip = radii[radii.length - 1];
        Vec3 apex = v(last.x + dx / len * tip, last.y + dy / len * tip, last.z + dz / len * tip);

        for (int i = 0; i < sides; i++) {
            out.add(new Triangle(rings[centres.length - 1][i], rings[centres.length - 1][(i + 1) % sides], apex, m));
        }
    }

    // cylinder with a chamfered top edge, open underneath (it never faces the camera)
    private static void addBevelledCylinder(ArrayList<Triangle> out, Vec3 top, float radius, float height,
                                            float bevel, int sides, Material m, Vec3 off) {
        Vec3 bottom = v(top.x, top.y - height, top.z);
        Vec3 centre = v(top.x, top.y, top.z);

        for (int i = 0; i < sides; i++) {
            double a0 = 2 * Math.PI * i / sides, a1 = 2 * Math.PI * (i + 1) / sides;

            addQuad(out, polar(bottom, radius, a0, 0f), polar(bottom, radius, a1, 0f),
                         polar(top, radius, a1, -bevel), polar(top, radius, a0, -bevel), m, off); // wall
            addQuad(out, polar(top, radius, a0, -bevel), polar(top, radius, a1, -bevel),
                         polar(top, radius - bevel, a1, 0f), polar(top, radius - bevel, a0, 0f), m, off); // chamfer
            out.add(new Triangle(centre.add(off), polar(top, radius - bevel, a0, 0f).add(off),
                                 polar(top, radius - bevel, a1, 0f).add(off), m)); // top face
        }
    }

    // a curved backdrop sweeping around behind the subject, angles measured off the -z axis
    private static void addArcWall(ArrayList<Triangle> out, Vec3 centre, float radius, float y0, float y1,
                                   float startDeg, float endDeg, int segments, Material m, Vec3 off) {
        for (int i = 0; i < segments; i++) {
            double a0 = Math.toRadians(startDeg + (endDeg - startDeg) * i / (double) segments);
            double a1 = Math.toRadians(startDeg + (endDeg - startDeg) * (i + 1) / (double) segments);

            Vec3 p0 = v(centre.x + radius * (float) Math.sin(a0), y0, centre.z - radius * (float) Math.cos(a0));
            Vec3 p1 = v(centre.x + radius * (float) Math.sin(a1), y0, centre.z - radius * (float) Math.cos(a1));

            addQuad(out, p0, p1, v(p1.x, y1, p1.z), v(p0.x, y1, p0.z), m, off);
        }
    }

    /* Tilts a sub-assembly built in its own space (pitch about x tips the top towards the camera,
     * yaw about y swings it off-axis), then drops it so its lowest vertex lands exactly on restY.
     * Measuring the low point after rotating is what lets the ring sit on the pedestal instead of
     * hovering or sinking, whatever angle it ends up at. */
    private static void orientAndSeat(ArrayList<Triangle> parts, float pitchDeg, float yawDeg,
                                      float x, float z, float restY, Vec3 off) {
        float pitch = (float) Math.toRadians(pitchDeg), yaw = (float) Math.toRadians(yawDeg);
        float lowest = Float.MAX_VALUE;

        for (Triangle t : parts) {
            t.p1 = spin(t.p1, pitch, yaw);
            t.p2 = spin(t.p2, pitch, yaw);
            t.p3 = spin(t.p3, pitch, yaw);
            lowest = Math.min(lowest, Math.min(t.p1.y, Math.min(t.p2.y, t.p3.y)));
        }

        Vec3 shift = v(x, restY - lowest, z).add(off);
        for (Triangle t : parts) {
            t.p1 = t.p1.add(shift);
            t.p2 = t.p2.add(shift);
            t.p3 = t.p3.add(shift);
        }
    }

    private static Vec3 spin(Vec3 p, float pitch, float yaw) {
        float cp = (float) Math.cos(pitch), sp = (float) Math.sin(pitch);
        float y = p.y * cp - p.z * sp;
        float z = p.y * sp + p.z * cp;

        float cy = (float) Math.cos(yaw), sy = (float) Math.sin(yaw);
        return v(p.x * cy + z * sy, y, -p.x * sy + z * cy);
    }
}