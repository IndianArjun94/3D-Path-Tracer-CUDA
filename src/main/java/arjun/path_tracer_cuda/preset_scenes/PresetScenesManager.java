package arjun.path_tracer_cuda.preset_scenes;

import arjun.path_tracer_cuda.geometry.*;
import arjun.path_tracer_cuda.preset_scenes.models.Boeing787;

import java.util.ArrayList;

public class PresetScenesManager {
    // scene 6 (the gaming PC): the colour every RGB light in the build is set to, 0-255 per channel
    public static final Vec3 PC_RGB = new Vec3(110, 70, 255);

    /* Builds preset 'scene' into the three lists.
     *
     * positionOffset moves the whole scene - geometry and lights together - relative to the kernel's
     * fixed camera, along the camera's own axes: +x right, +y up, +z towards the camera. So
     * (0, 0, -5) pushes everything 5 further away (as if the camera stepped back 5), and (2, 0, 0)
     * slides it 2 to the right. It's in each scene's own units, so the same numbers mean the same
     * kind of move whatever the scene's world scale:
     *   1, 2, 3  world units (1 board square is 2 in scene 3)
     *   4        case radii of the pocket watch (1 = 10 world units)
     *   5        metres in the glass gallery (1 = 10 world units)
     *   6        centimetres at the desk (1 = 1 world unit)
     *   7        metres on the apron (1 = 10 world units)
     * Scenes 4 to 7 add it in camera space before scaling up (toView / aimView); 1 to 3 have no view
     * transform - their world already is camera space - and add it straight on. */
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
        }

        if (scene == 2) {
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

        if (scene == 3) {
            /* =====================================================================================
             * "ENDGAME" - a turned Staunton chess set on a wooden board. A high primitive count
             * scene for exercising the BVH, built from real objects rather than scattered shapes.
             *
             *   - every piece is a surface of revolution (addLathe) swept from a Staunton profile:
             *     weighted base with a rounded shoulder and a bead, cove, waisted stem, collar, head
             *   - king: flared body, crown, box cross        queen: crown, coronet of 8 balls, finial
             *   - bishop: mitre + finial ball                rook: turret with 6 real crenellations
             *   - pawn: ball head
             *   - 64 tiled squares in a raised frame, a floor, a curved backdrop, and a bounce card
             *     behind the camera (same trick as scene 2: invisible, but the gloss has something
             *     bright to reflect)
             *
             * Layout is in SQUARE units, scaled by SQ. The scale is large on purpose: hitTriangle's
             * parallel-ray test compares against an unnormalised normal, so very small triangles
             * get rejected even head-on. At SQ = 2 the smallest facets stay clear of that. The balls
             * are real spheres for the same reason - a lathe pole fans into slivers.
             * No knights: a horse's head isn't a solid of revolution.
             *
             * Triangle budget lives in LATHE_SIDES (~85% of the scene is lathe facets).
             * ===================================================================================== */
            final int LATHE_SIDES = 32;
            final float SQ = 2.0f; // world size of one board square

            // --- MATERIALS ---
            Material matFloor    = new Material(new Vec3( 40,  40,  46), 0f, 0.50f, 0f);
            Material matWall     = new Material(new Vec3(120, 124, 134), 0f, 0.90f, 0f);
            Material matCard     = new Material(new Vec3(250, 250, 255), 0f, 0.95f, 0f);
            /* One high-index lacquer over the board and every piece. The kernel has no separate
             * coat layer: a dielectric is diffuse underneath with a Fresnel specular on top, and ior
             * sets that specular's strength (F0 = ((1-ior)/(1+ior))^2). So the coat is just a shared
             * ior + roughness: 2.0 reflects ~11% head-on vs ~4% for ordinary 1.5 varnish, and the
             * low roughness keeps the reflections crisp while the wood/colour stays in the diffuse. */
            final float COAT_IOR       = 2.0f;
            final float COAT_ROUGHNESS = 0.02f;

            // wood base colours - the grain shades each strip from these (see addWoodPanel)
            Vec3 maple     = new Vec3(226, 204, 160);
            Vec3 walnut    = new Vec3(100,  62,  38);
            Vec3 frameWood = new Vec3( 58,  34,  22);
            Material matFrame    = new Material(new Vec3(frameWood), 0f, COAT_ROUGHNESS, 0f, COAT_IOR, 1f); // the frame's end grain edges
            Material ivory       = new Material(new Vec3(238, 226, 204), 0f, COAT_ROUGHNESS, 0f, COAT_IOR, 1f); // boxwood
            Material ebony       = new Material(new Vec3( 26,  22,  22), 0f, COAT_ROUGHNESS, 0f, COAT_IOR, 1f);

            // --- LAYOUT ---
            final float BOARD_Y = -2.0f * SQ;          // top of the squares
            final float BOARD_Z = -6.3f * SQ;          // centre of the board
            final float HALF    = 4.0f * SQ;           // centre to the edge of the squares
            final float OUTER   = HALF + 0.43f * SQ;   // centre to the outside of the frame
            final float FLOOR_Y = BOARD_Y - 0.17f * SQ; // the board's thickness

            // built at the origin and offset at the end: neighbouring facets share vertex objects,
            // so they can't each be offset in place
            ArrayList<Triangle> set = new ArrayList<>();
            ArrayList<Sphere> balls = new ArrayList<>();

            // --- SET ---
            addQuad(set, v(-20f * SQ, FLOOR_Y, 3f * SQ), v(20f * SQ, FLOOR_Y, 3f * SQ), v(20f * SQ, FLOOR_Y, -20f * SQ), v(-20f * SQ, FLOOR_Y, -20f * SQ), matFloor, ORIGIN);

            // centred near the camera so the sweep's ends land outside the frustum
            addArcWall(set, v(0f, 0f, -2f * SQ), 13f * SQ, FLOOR_Y, FLOOR_Y + 20f * SQ, -88f, 88f, 32, matWall, ORIGIN);

            // bounce card behind the camera (z > 0): never seen directly, only in reflections
            addQuad(set, v(-10f * SQ, FLOOR_Y, 3f * SQ), v(10f * SQ, FLOOR_Y, 3f * SQ), v(10f * SQ, 10f * SQ, 3f * SQ), v(-10f * SQ, 10f * SQ, 3f * SQ), matCard, ORIGIN);

            // --- BOARD ---
            // Grain is geometry: every square is cut into GRAIN_STRIPS wandering strips along the
            // grain, each GRAIN_SEGMENTS long, all shaded individually (see addWoodPanel).
            // 2 * strips * segments triangles per square - this is now the bulk of the scene.
            final int GRAIN_STRIPS   = 24;
            final int GRAIN_SEGMENTS = 10;

            for (int file = 0; file < 8; file++) {
                for (int rank = 0; rank < 8; rank++) { // rank 0 is nearest the camera
                    float x0 = -HALF + file * SQ, z0 = BOARD_Z + HALF - rank * SQ;
                    boolean dark = (file + rank) % 2 == 0; // a1 is dark, as on a real board

                    // grain runs away from the camera; each square is its own offcut (own seed)
                    addWoodPanel(set, v(x0, BOARD_Y, z0), v(0f, 0f, -SQ), v(SQ, 0f, 0f), GRAIN_STRIPS, GRAIN_SEGMENTS,
                                 dark ? walnut : maple, dark ? 0.62f : 0.84f, 0.05f, SQ,
                                 COAT_ROUGHNESS, COAT_IOR, 1000 + file * 8 + rank);
                }
            }

            // frame: four strips flush with the squares, grain running along each one's length,
            // then plain outside edges down to the floor
            float zIn = BOARD_Z + HALF, zInFar = BOARD_Z - HALF, zOut = BOARD_Z + OUTER, zOutFar = BOARD_Z - OUTER;
            final int FRAME_STRIPS = 8, FRAME_SEGMENTS = 72;
            addWoodPanel(set, v(-OUTER, BOARD_Y, zOut),   v(2 * OUTER, 0f, 0f), v(0f, 0f, zIn - zOut),      FRAME_STRIPS, FRAME_SEGMENTS, frameWood, 0.6f, 0.03f, SQ, COAT_ROUGHNESS, COAT_IOR, 7001); // near strip
            addWoodPanel(set, v(-OUTER, BOARD_Y, zInFar), v(2 * OUTER, 0f, 0f), v(0f, 0f, zOutFar - zInFar), FRAME_STRIPS, FRAME_SEGMENTS, frameWood, 0.6f, 0.03f, SQ, COAT_ROUGHNESS, COAT_IOR, 7002); // far strip
            addWoodPanel(set, v(-OUTER, BOARD_Y, zIn),    v(0f, 0f, zInFar - zIn), v(OUTER - HALF, 0f, 0f),  FRAME_STRIPS, FRAME_SEGMENTS, frameWood, 0.6f, 0.03f, SQ, COAT_ROUGHNESS, COAT_IOR, 7003); // left strip
            addWoodPanel(set, v( HALF,  BOARD_Y, zIn),    v(0f, 0f, zInFar - zIn), v(OUTER - HALF, 0f, 0f),  FRAME_STRIPS, FRAME_SEGMENTS, frameWood, 0.6f, 0.03f, SQ, COAT_ROUGHNESS, COAT_IOR, 7004); // right strip
            addQuad(set, v(-OUTER, FLOOR_Y, zOut),    v(OUTER, FLOOR_Y, zOut),    v(OUTER, BOARD_Y, zOut),    v(-OUTER, BOARD_Y, zOut),    matFrame, ORIGIN); // near edge
            addQuad(set, v(-OUTER, FLOOR_Y, zOutFar), v(OUTER, FLOOR_Y, zOutFar), v(OUTER, BOARD_Y, zOutFar), v(-OUTER, BOARD_Y, zOutFar), matFrame, ORIGIN); // far edge
            addQuad(set, v(-OUTER, FLOOR_Y, zOut),    v(-OUTER, FLOOR_Y, zOutFar), v(-OUTER, BOARD_Y, zOutFar), v(-OUTER, BOARD_Y, zOut), matFrame, ORIGIN); // left edge
            addQuad(set, v( OUTER, FLOOR_Y, zOut),    v( OUTER, FLOOR_Y, zOutFar), v( OUTER, BOARD_Y, zOutFar), v( OUTER, BOARD_Y, zOut), matFrame, ORIGIN); // right edge

            // --- PIECES --- (file, rank), white nearest the camera
            addChessPiece(set, balls, 'K', chessSquare(4, 1, SQ, BOARD_Y, BOARD_Z), SQ, LATHE_SIDES, ivory);
            addChessPiece(set, balls, 'Q', chessSquare(3, 3, SQ, BOARD_Y, BOARD_Z), SQ, LATHE_SIDES, ivory);
            addChessPiece(set, balls, 'R', chessSquare(0, 2, SQ, BOARD_Y, BOARD_Z), SQ, LATHE_SIDES, ivory);
            addChessPiece(set, balls, 'B', chessSquare(5, 2, SQ, BOARD_Y, BOARD_Z), SQ, LATHE_SIDES, ivory);
            addChessPiece(set, balls, 'P', chessSquare(5, 1, SQ, BOARD_Y, BOARD_Z), SQ, LATHE_SIDES, ivory);
            addChessPiece(set, balls, 'P', chessSquare(6, 1, SQ, BOARD_Y, BOARD_Z), SQ, LATHE_SIDES, ivory);
            addChessPiece(set, balls, 'P', chessSquare(7, 2, SQ, BOARD_Y, BOARD_Z), SQ, LATHE_SIDES, ivory);

            addChessPiece(set, balls, 'K', chessSquare(6, 6, SQ, BOARD_Y, BOARD_Z), SQ, LATHE_SIDES, ebony);
            addChessPiece(set, balls, 'Q', chessSquare(2, 6, SQ, BOARD_Y, BOARD_Z), SQ, LATHE_SIDES, ebony);
            addChessPiece(set, balls, 'R', chessSquare(4, 5, SQ, BOARD_Y, BOARD_Z), SQ, LATHE_SIDES, ebony);
            addChessPiece(set, balls, 'B', chessSquare(1, 5, SQ, BOARD_Y, BOARD_Z), SQ, LATHE_SIDES, ebony);
            addChessPiece(set, balls, 'P', chessSquare(3, 5, SQ, BOARD_Y, BOARD_Z), SQ, LATHE_SIDES, ebony);
            addChessPiece(set, balls, 'P', chessSquare(5, 6, SQ, BOARD_Y, BOARD_Z), SQ, LATHE_SIDES, ebony);
            addChessPiece(set, balls, 'P', chessSquare(7, 5, SQ, BOARD_Y, BOARD_Z), SQ, LATHE_SIDES, ebony);

            for (Triangle t : set) { // fresh copies: shared vertices must only be offset once
                t.p1 = new Vec3(t.p1).add(positionOffset);
                t.p2 = new Vec3(t.p2).add(positionOffset);
                t.p3 = new Vec3(t.p3).add(positionOffset);
            }
            for (Sphere s : balls) {
                s.pos = new Vec3(s.pos).add(positionOffset);
            }
            triangles.addAll(set);
            spheres.addAll(balls);

            // --- LIGHTS --- (positions and falloff both scale with SQ, so the look survives resizing)
            pointLights.add(new PointLight(v( 4.3f * SQ, 3.6f * SQ,  -2.9f * SQ).add(positionOffset), new Vec3(255, 240, 220), 120f * SQ * SQ)); // key, warm, high front-right
            pointLights.add(new PointLight(v(-5.0f * SQ, 2.1f * SQ,  -2.1f * SQ).add(positionOffset), new Vec3(190, 210, 255),  70f * SQ * SQ)); // fill, cool, low left
            pointLights.add(new PointLight(v(-1.4f * SQ, 3.6f * SQ, -11.4f * SQ).add(positionOffset), new Vec3(255, 255, 255),  92f * SQ * SQ)); // rim, past the far side of the board
        }

        if (scene == 4) {
            /* =====================================================================================
             * "SKELETON" - an open-face skeleton pocket watch lying on a walnut table, its chain
             * trailing off to the right. The dial is cut back to a chapter ring so the movement
             * shows through:
             *   - gold case (domed back, grooved band, bezel), pendant, knurled crown, tilted bow
             *   - main plate finished with perlage, done in polygons: a fine polar grid where every
             *     cell takes the shade of whichever graining disc lies on top there
             *   - going train: barrel, centre, third, fourth and escape wheels with real cut teeth,
             *     curved crossings and meshing pinions, each wheel on its own level
             *   - lever escapement: pallet fork with ruby pallet stones, balance wheel with timing
             *     screws, and a 12-turn flat hairspring (~a fifth of the whole scene)
             *   - rhodium bridges and balance cock, ruby jewels in gold chatons, blued screws with
             *     slots, a ratchet wheel on the barrel bridge
             *   - enamel chapter ring: Roman numerals built from strokes, minute track, blued hands
             *   - a domed sapphire crystal over the whole front, seated on the bezel
             *   - the whole watch hovers LIFT above the table; its curb chain of alternating flat
             *     and upright links hangs from the bow, bends down onto the table and runs off
             *     along it
             *   - a table of flat-sawn walnut planks cut from simulated logs (addPlank) under a
             *     thick, very reflective lacquer, a dark wall behind it, and a softbox overhead,
             *     never seen directly, that all the metal reflects - metals get no direct light in
             *     this kernel, only reflections
             *
             * Built in "case radii" with the table at y = 0 and 12 o'clock pointing at -z, then the
             * whole set is scaled by S and turned in front of the kernel's fixed camera
             * (see toView) so the camera looks down at it from VIEW_PITCH degrees. At 45 the top
             * edge of the frame looks straight out, which is what the wall is for - any lower and
             * it would need to be taller.
             * S = 10 keeps the smallest parts (hairspring, numerals) well clear of hitTriangle's
             * minimum triangle size.
             * ===================================================================================== */
            final float S          = 10f;   // world units per case radius
            final float VIEW_PITCH = 45f;   // how steeply the camera looks down
            final float VIEW_DIST  = 1.85f; // camera to watch centre, in case radii
            final float LIFT       = 0.4f;  // how far the watch hovers above the table, in case radii
            final float WATCH_TURN = 90f;   // watch turned clockwise (seen from above): 12 o'clock to the right, so the bow and hanging chain face the side

            final int CASE_SIDES     = 192;
            final int PLATE_RINGS    = 80, PLATE_SECTORS = 420; // perlage resolution (2 * rings * sectors triangles)
            final int HAIRSPRING_TURNS = 12;
            final int CHAIN_SIDES    = 16;  // around the chain's wire
            final int CHAIN_BEND     = 20;  // steps round each end of a link (4 * CHAIN_BEND * CHAIN_SIDES triangles per link)
            final float WOOD_DETAIL  = 0.02f; // wood cell size per unit of distance from the camera (smaller = finer, more triangles)

            /* The lacquer: this kernel has no coat layer (a clear solid over the wood would block
             * every shadow ray, since shadow rays treat glass as opaque), so "thick and very
             * reflective" is a mirror-smooth surface with a high index: ior 2.3 reflects ~16%
             * head-on and climbs towards 100% at the grazing angles this camera sees the table at. */
            final float LACQUER_IOR = 2.3f, LACQUER_ROUGHNESS = 0.012f;

            // --- MATERIALS ---
            Material gold    = new Material(new Vec3(255, 205, 130), 1f, 0.06f, 0f);
            Material brass   = new Material(new Vec3(240, 195, 120), 1f, 0.12f, 0f); // train wheels
            Material rhodium = new Material(new Vec3(214, 217, 224), 1f, 0.10f, 0f); // bridges
            Material steel   = new Material(new Vec3(205, 208, 214), 1f, 0.05f, 0f); // arbors, pinions, ratchet, hairspring
            Material blued   = new Material(new Vec3( 40,  70, 170), 1f, 0.08f, 0f); // screws and hands
            Material ruby    = new Material(new Vec3(210,  25,  45), 0f, 0.00f, 0f, 1.77f, 1f);
            Material enamel  = new Material(new Vec3(246, 243, 235), 0f, 0.04f, 0f, 1.5f, 1f);
            Material ink     = new Material(new Vec3( 18,  18,  22), 0f, 0.10f, 0f);
            Material softbox = new Material(new Vec3(255, 255, 255), 0f, 1.00f, 0f);
            Material wall    = new Material(new Vec3( 34,  38,  36), 0f, 0.90f, 0f); // dark green-grey plaster
            Material shadow  = new Material(new Vec3( 10,   8,   6), 0f, 0.90f, 0f); // seen down the gaps between planks
            // sapphire: a bit denser than ordinary crystal glass (1.5), clear, no tint to speak of
            Material sapphire = new Material(new Vec3(255, 255, 255), 0f, 0f, 1f, 1.77f, 0.02f);
            Vec3 plateColor  = new Vec3(214, 217, 224);
            Vec3 walnut      = new Vec3(118,  74,  44);

            ArrayList<Triangle> w = new ArrayList<>();
            ArrayList<Sphere> b = new ArrayList<>();

            // where the camera will be (see INTO THE CAMERA below) - the table's detail follows it
            float pitch = (float) Math.toRadians(VIEW_PITCH);
            // aimed half way up the gap under the watch, so the hover reads
            Vec3 eye = v(0f, 0.5f * LIFT + VIEW_DIST * (float) Math.sin(pitch), VIEW_DIST * (float) Math.cos(pitch));

            // --- TABLE, WALL, SOFTBOX ---
            // five planks along x, each from its own log, with hairline gaps between them. Cells are
            // sized by distance so they come out roughly even on screen: fine on the planks in front,
            // coarse at the back, and finest down the middle (x = 0) where the camera is pointed.
            final float TABLE_NEAR = 2.5f, TABLE_FAR = -5f, PLANK = (TABLE_NEAR - TABLE_FAR) / 5f, GAP = 0.012f;
            for (int k = 0; k < 5; k++) {
                float z0 = TABLE_NEAR - PLANK * k, z1 = z0 - PLANK + GAP;
                float cell = WOOD_DETAIL * (float) Math.hypot(eye.y, (z0 + z1) / 2 - eye.z);
                addPlank(w, graded(-10f, 10f, cell, 1f), z0, z1, 0f, (int) Math.ceil(PLANK / (0.4f * cell)),
                         walnut, LACQUER_ROUGHNESS, LACQUER_IOR, 8100 + k);
            }
            addQuad(w, v(-10f, -0.02f, TABLE_NEAR), v(10f, -0.02f, TABLE_NEAR), v(10f, -0.02f, TABLE_FAR), v(-10f, -0.02f, TABLE_FAR), shadow, ORIGIN);
            addQuad(w, v(-12f, 0f, TABLE_FAR), v(12f, 0f, TABLE_FAR), v(12f, 5f, TABLE_FAR), v(-12f, 5f, TABLE_FAR), wall, ORIGIN);
            addQuad(w, v(-8f, 5f, TABLE_FAR), v(8f, 5f, TABLE_FAR), v(8f, 5f, 6f), v(-8f, 5f, 6f), softbox, ORIGIN); // above the camera: only ever reflected

            // everything from here to the chain is the watch, built sitting on y = 0 and lifted afterwards
            int watchStart = w.size();

            // --- CASE --- (lathe, the back dome at y = 0)
            Profile caseProfile = new Profile()
                    .to(0f, 0f)
                    .curve(0.60f, 0.0f, 0.90f, 0.08f, 10)          // domed caseback
                    .arc(0.90f, 0.18f, 0.10f, -90f, 0f, 6)         // rounded into the band
                    .to(1.0f, 0.28f).to(0.99f, 0.285f).to(0.99f, 0.295f).to(1.0f, 0.30f) // band with a groove
                    .to(1.0f, 0.36f)
                    .arc(0.96f, 0.36f, 0.04f, 0f, 90f, 6)          // bezel
                    .to(0.93f, 0.40f).to(0.92f, 0.39f)
                    .to(0.92f, 0.199f).to(0.90f, 0.199f);          // inner wall down to the plate
            addLathe(w, ORIGIN, 1f, caseProfile, CASE_SIDES, gold);

            // crystal: a closed domed lens over the whole front, sitting just clear of the bezel top
            // (0.40) so no opaque surface ends up inside the glass - the kernel's single inObject
            // flag can't cope with that. Underside rises 0.05 to the centre, 0.018 thick throughout.
            Profile crystal = new Profile();
            for (int i = 0; i <= 24; i++) { // underside, centre out
                float r = 0.95f * i / 24;
                crystal.to(r, 0.401f + 0.05f * (1f - (r / 0.95f) * (r / 0.95f)));
            }
            crystal.to(0.957f, 0.410f); // rounded edge
            for (int i = 24; i >= 0; i--) { // top, back to the centre
                float r = 0.95f * i / 24;
                crystal.to(r, 0.419f + 0.05f * (1f - (r / 0.95f) * (r / 0.95f)));
            }
            addLathe(w, ORIGIN, 1f, crystal, CASE_SIDES, sapphire);

            // pendant and knurled crown at 12 o'clock, on a horizontal axis
            Vec3 pendantBase = v(0f, 0.2f, -0.965f), outward = v(0f, 0f, -1f);
            addAxialLathe(w, pendantBase, outward, new Profile()
                    .to(0.05f, 0f).to(0.05f, 0.09f).to(0.068f, 0.098f).to(0.068f, 0.115f).to(0.05f, 0.125f)
                    .to(0.05f, 0.14f).to(0.10f, 0.14f), 48, 0, 0f, gold);
            addAxialLathe(w, pendantBase, outward, new Profile().to(0.088f, 0.14f).to(0.088f, 0.22f), 96, 24, 0.012f, gold); // knurl
            addAxialLathe(w, pendantBase, outward, new Profile().to(0.10f, 0.22f).curve(0.10f, 0.25f, 0f, 0.255f, 6), 48, 0, 0f, gold);

            // bow: a stadium loop round the crown, tipped down until its far end rests on the table
            float bowTilt = (float) Math.toRadians(35), bowBend = 0.13f, bowWire = 0.02f;
            Vec3 bowAlong = v(0f, -(float) Math.sin(bowTilt), -(float) Math.cos(bowTilt));
            float bowStraight = (0.2f - bowWire) / (float) Math.sin(bowTilt) - bowBend;
            Vec3 crownCentre = v(0f, 0.2f, -0.965f - 0.18f);
            addWireLoop(w, lin(crownCentre, bowAlong, bowStraight / 2), bowAlong, v(1f, 0f, 0f), bowStraight, bowBend, bowWire, 16, 12, gold);

            // --- MAIN PLATE --- (perlage, top face only - the case floor sits under it)
            addPerlagePlate(w, 0.905f, 0.20f, PLATE_RINGS, PLATE_SECTORS, plateColor, 0.14f);

            // --- GOING TRAIN --- wheel centres, each wheel on its own level so they can overlap in plan
            float[] C = {0f, 0f}, B = {-0.34f, -0.18f}, T = {0.22f, -0.19f}, F = {0.36f, 0f}, E = {0.40f, 0.19f};
            float[] BAL = {0.12f, 0.45f}, P = {0.34f, 0.29f};

            addWheel(w, B[0], B[1], 0.30f, 84, 0, 0.203f, 0.238f, 0, brass);  // barrel
            addWheel(w, C[0], C[1], 0.085f, 12, 0, 0.203f, 0.238f, 0, steel); // centre pinion, driven by the barrel
            addWheel(w, C[0], C[1], 0.24f, 72, 5, 0.243f, 0.253f, 0, brass);  // centre wheel
            addWheel(w, T[0], T[1], 0.055f, 10, 0, 0.243f, 0.253f, 0, steel); // third pinion
            addWheel(w, T[0], T[1], 0.18f, 64, 4, 0.261f, 0.271f, 0, brass);  // third wheel
            addWheel(w, F[0], F[1], 0.05f, 8, 0, 0.261f, 0.271f, 0, steel);   // fourth pinion
            addWheel(w, F[0], F[1], 0.16f, 60, 4, 0.225f, 0.235f, 0, brass);  // fourth wheel
            addWheel(w, E[0], E[1], 0.035f, 7, 0, 0.225f, 0.235f, 0, steel);  // escape pinion
            addWheel(w, E[0], E[1], 0.10f, 20, 4, 0.243f, 0.253f, 2, steel);  // escape wheel, club teeth

            for (float[] a : new float[][]{T, F, E, P, BAL}) { // arbors, plate to bridge
                addLathe(w, v(a[0], 0f, a[1]), 1f, new Profile().to(0.011f, 0.200f).to(0.011f, 0.300f), 16, steel);
            }

            // pallet fork, below the escape wheel's level, with ruby stones on its two arms
            addRibbon(w, new float[][]{{0.30f, 0.22f}, {0.34f, 0.29f}, {0.45f, 0.27f}}, 0.016f, 0.232f, 0.240f, steel);
            addRibbon(w, new float[][]{{0.34f, 0.29f}, {0.27f, 0.345f}, {0.21f, 0.39f}}, 0.014f, 0.232f, 0.240f, steel);
            b.add(new Sphere(v(0.30f, 0.236f, 0.22f), 0.009f, ruby));
            b.add(new Sphere(v(0.45f, 0.236f, 0.27f), 0.009f, ruby));

            // --- BALANCE --- rim, three arms, timing screws round the rim, flat hairspring
            addRing(w, BAL[0], BAL[1], 0.18f, 0.20f, 0.245f, 0.265f, 120, gold);
            addRing(w, BAL[0], BAL[1], 0.012f, 0.03f, 0.245f, 0.265f, 24, gold);
            for (int k = 0; k < 3; k++) {
                double a = 2 * Math.PI * k / 3 + 0.3;
                addRibbon(w, new float[][]{{BAL[0] + 0.025f * (float) Math.sin(a), BAL[1] - 0.025f * (float) Math.cos(a)},
                                           {BAL[0] + 0.185f * (float) Math.sin(a), BAL[1] - 0.185f * (float) Math.cos(a)}},
                          0.014f, 0.245f, 0.265f, gold);
            }
            for (int k = 0; k < 16; k++) {
                double a = 2 * Math.PI * k / 16;
                b.add(new Sphere(v(BAL[0] + 0.206f * (float) Math.sin(a), 0.255f, BAL[1] - 0.206f * (float) Math.cos(a)), 0.0095f, gold));
            }
            addRing(w, BAL[0], BAL[1], 0.012f, 0.036f, 0.266f, 0.284f, 24, steel); // collet
            addHairspring(w, BAL[0], BAL[1], 0.035f, 0.15f, HAIRSPRING_TURNS, 0.0025f, 0.0025f, 0.270f, 0.282f, steel);

            // --- BRIDGES --- (top face at 0.308, the balance cock a little higher)
            final float BR0 = 0.290f, BR1 = 0.308f;
            addDisc(w, B[0], B[1], 0.15f, BR0, BR1, 48, rhodium); // barrel bridge
            addRibbon(w, new float[][]{B, {-0.64f, -0.16f}}, 0.09f, BR0, BR1, rhodium);
            addRibbon(w, new float[][]{B, {-0.20f, -0.55f}}, 0.09f, BR0, BR1, rhodium);
            addWheel(w, B[0], B[1], 0.13f, 36, 0, BR1, BR1 + 0.010f, 1, steel);  // ratchet wheel on top
            addLathe(w, v(B[0], BR1 + 0.010f, B[1]), 1f, new Profile().to(0f, 0f).to(0.035f, 0f).to(0.035f, 0.005f)
                    .curve(0.032f, 0.014f, 0f, 0.016f, 6), 32, blued);                // its big screw

            addRibbon(w, new float[][]{{0.02f, -0.64f}, T, F, E, {0.66f, 0.14f}}, 0.07f, BR0, BR1, rhodium); // train bridge
            for (float[] a : new float[][]{T, F, E}) addDisc(w, a[0], a[1], 0.045f, BR0, BR1, 32, rhodium);
            addRibbon(w, new float[][]{P, {0.55f, 0.40f}}, 0.05f, BR0, BR1, rhodium); // pallet bridge
            addDisc(w, P[0], P[1], 0.035f, BR0, BR1, 24, rhodium);

            addRibbon(w, new float[][]{{-0.16f, 0.63f}, {-0.02f, 0.54f}, BAL}, new float[]{0.11f, 0.08f, 0.06f}, 0.292f, 0.312f, rhodium); // balance cock
            addDisc(w, BAL[0], BAL[1], 0.06f, 0.292f, 0.312f, 32, rhodium);
            addRibbon(w, new float[][]{BAL, {0.07f, 0.31f}}, 0.012f, 0.312f, 0.316f, steel); // regulator index
            addDisc(w, BAL[0], BAL[1], 0.03f, 0.312f, 0.316f, 24, steel);

            // jewels in gold chatons
            for (float[] a : new float[][]{T, F, E, P}) {
                addRing(w, a[0], a[1], 0.016f, 0.026f, BR1, BR1 + 0.004f, 24, gold);
                b.add(new Sphere(v(a[0], BR1 - 0.002f, a[1]), 0.016f, ruby));
            }
            addRing(w, BAL[0], BAL[1], 0.016f, 0.026f, 0.316f, 0.320f, 24, gold);
            b.add(new Sphere(v(BAL[0], 0.315f, BAL[1]), 0.015f, ruby));

            // blued screws, slots at assorted angles
            float[][] screws = {{-0.64f, -0.16f}, {-0.20f, -0.55f}, {0.02f, -0.64f}, {0.66f, 0.14f}, {0.55f, 0.40f}, {-0.16f, 0.63f}};
            for (int k = 0; k < screws.length; k++) {
                float sx = screws[k][0], sz = screws[k][1], top = k == 5 ? 0.312f : BR1;
                addLathe(w, v(sx, top, sz), 1f, new Profile().to(0f, 0f).to(0.026f, 0f).to(0.026f, 0.004f)
                        .curve(0.024f, 0.011f, 0f, 0.013f, 6), 32, blued);
                double a = 0.7 + 1.9 * k;
                float dx = 0.024f * (float) Math.cos(a), dz = 0.024f * (float) Math.sin(a);
                addRibbon(w, new float[][]{{sx - dx, sz - dz}, {sx + dx, sz + dz}}, 0.005f, top + 0.0095f, top + 0.0135f, ink);
            }

            // --- DIAL --- enamel chapter ring, minute track, Roman numerals (IIII, as watches have it)
            addRing(w, 0f, 0f, 0.72f, 0.915f, 0.322f, 0.332f, 240, enamel);
            addRing(w, 0f, 0f, 0.858f, 0.861f, 0.332f, 0.3335f, 240, ink);
            addRing(w, 0f, 0f, 0.902f, 0.905f, 0.332f, 0.3335f, 240, ink);
            for (int m = 0; m < 60; m++) {
                double a = Math.toRadians(6 * m);
                float sa = (float) Math.sin(a), ca = (float) -Math.cos(a), r1 = m % 5 == 0 ? 0.902f : 0.89f;
                addRibbon(w, new float[][]{{0.861f * sa, 0.861f * ca}, {r1 * sa, r1 * ca}}, m % 5 == 0 ? 0.010f : 0.005f, 0.332f, 0.3335f, ink);
            }
            String[] numerals = {"XII", "I", "II", "III", "IIII", "V", "VI", "VII", "VIII", "IX", "X", "XI"};
            for (int h = 0; h < 12; h++) {
                addNumeral(w, numerals[h], Math.toRadians(30 * h), 0.745f, 0.095f, 0.012f, 0.332f, 0.335f, ink);
            }

            // --- HANDS --- ten past ten, blued, on the cannon pinion
            addLathe(w, ORIGIN, 1f, new Profile().to(0f, 0.253f).to(0.028f, 0.253f).to(0.028f, 0.34f).to(0.018f, 0.34f)
                    .to(0.018f, 0.365f).to(0f, 0.365f), 32, brass);
            addHand(w, Math.toRadians(305), new float[][]{{-0.07f, 0.028f}, {-0.02f, 0.042f}, {0f, 0.046f}, {0.05f, 0.02f},
                    {0.25f, 0.016f}, {0.33f, 0.034f}, {0.40f, 0.02f}, {0.46f, 0.004f}}, 0.345f, 0.350f, blued);
            addHand(w, Math.toRadians(60), new float[][]{{-0.10f, 0.03f}, {0f, 0.042f}, {0.06f, 0.016f}, {0.55f, 0.01f},
                    {0.68f, 0.003f}}, 0.355f, 0.360f, blued);
            addLathe(w, v(0f, 0.360f, 0f), 1f, new Profile().to(0f, 0f).to(0.032f, 0f).to(0.032f, 0.004f)
                    .curve(0.03f, 0.011f, 0f, 0.012f, 6), 32, blued);

            // --- TURN + LIFT --- the whole watch, clockwise about its centre, then up off the table
            // (fresh copies: shared vertices must only move once)
            float turn = (float) Math.toRadians(WATCH_TURN), tc = (float) Math.cos(turn), ts = (float) Math.sin(turn);
            for (int i = watchStart; i < w.size(); i++) {
                Triangle t = w.get(i);
                t.p1 = v(t.p1.x * tc - t.p1.z * ts, t.p1.y + LIFT, t.p1.x * ts + t.p1.z * tc);
                t.p2 = v(t.p2.x * tc - t.p2.z * ts, t.p2.y + LIFT, t.p2.x * ts + t.p2.z * tc);
                t.p3 = v(t.p3.x * tc - t.p3.z * ts, t.p3.y + LIFT, t.p3.x * ts + t.p3.z * tc);
            }
            for (Sphere s : b) { // every sphere so far is part of the watch
                s.pos = v(s.pos.x * tc - s.pos.z * ts, s.pos.y + LIFT, s.pos.x * ts + s.pos.z * tc);
            }

            // --- CHAIN --- hangs from the bow's far end, bends down onto the table, then curls back
            // and off to the right. The hanging part is written in the watch's own frame (12 o'clock
            // at -z, before the turn) and turned with it; the rest lies on the table in the set's frame.
            float[][] hanging = {{0f, LIFT + 0.02f, -1.41f}, {0.02f, 0.62f * LIFT, -1.58f}, {0.06f, 0.22f * LIFT, -1.79f}, {0.12f, 0f, -2.04f}};
            float[][] onTable = {{2.35f, 0f, 0.05f}, {2.6f, 0f, -0.4f}, {2.55f, 0f, -1.0f}, {2.9f, 0f, -1.5f}, {3.8f, 0f, -1.6f}, {5.0f, 0f, -1.4f}};
            float[][] chainPath = new float[hanging.length + onTable.length][];
            for (int i = 0; i < hanging.length; i++) {
                float[] p = hanging[i];
                chainPath[i] = new float[]{p[0] * tc - p[2] * ts, p[1], p[0] * ts + p[2] * tc};
            }
            System.arraycopy(onTable, 0, chainPath, hanging.length, onTable.length);
            addChain(w, chainPath, 0.016f, 0.042f, 0.05f, CHAIN_BEND, CHAIN_SIDES, gold);

            // --- LIGHTS --- (set coordinates; falloff scales with S^2 like the geometry)
            ArrayList<PointLight> rig = new ArrayList<>();
            rig.add(new PointLight(v( 1.8f, 3.2f,  1.8f), new Vec3(255, 240, 220), 40f * S * S)); // key, warm, high front-right
            rig.add(new PointLight(v(-2.4f, 2.0f,  0.6f), new Vec3(190, 210, 255), 20f * S * S)); // fill, cool, left
            rig.add(new PointLight(v(-0.8f, 2.6f, -3.2f), new Vec3(255, 255, 255), 30f * S * S)); // rim, beyond 12 o'clock

            // --- INTO THE CAMERA --- the kernel's camera is fixed at the origin looking down -z, so the
            // set is moved and turned in front of it: as if the camera sat VIEW_DIST from the watch,
            // looking down at VIEW_PITCH (eye and pitch are set up above the table). Fresh copies,
            // since neighbouring facets share vertices.
            for (Triangle t : w) {
                t.p1 = toView(t.p1, eye, pitch, S, positionOffset);
                t.p2 = toView(t.p2, eye, pitch, S, positionOffset);
                t.p3 = toView(t.p3, eye, pitch, S, positionOffset);
            }
            for (Sphere s : b) {
                s.pos = toView(s.pos, eye, pitch, S, positionOffset);
                s.radius *= S;
            }
            for (PointLight l : rig) {
                l.pos = toView(l.pos, eye, pitch, S, positionOffset);
            }
            triangles.addAll(w);
            spheres.addAll(b);
            pointLights.addAll(rig);
        }

        if (scene == 5) {
            /* =====================================================================================
             * "GLASSHOUSE" - a bright white gallery given over to blown glass, after the kind of
             * work in Chihuly Garden and Glass:
             *   - a hanging "sun": fluted, twisted tendrils radiating from a hidden core, each
             *     curling hard at the tip, in yellows, oranges and reds, on a steel rod
             *   - a stand of tall reeds in cobalt, violet and teal, leaning out from the floor
             *   - three "Macchia" bowls - thick walls, ruffled wavy rims - on a white plinth
             *   - five Niijima floats (big glass spheres) on the floor
             *   - a polished white stone floor that mirrors all of it, white walls and ceiling and
             *     six lights: light bouncing round a white room is what keeps glass looking bright
             *
             * Every glass piece is its own closed solid, and none of them touch or overlap - the
             * kernel tracks "inside glass" with a single flag. Tendrils and reeds are placed by
             * rejection sampling against everything already placed (GlassPiece), and anything
             * resting on something sits a hair above it.
             * Built in metres-ish with the floor at y = 0, then scaled by S and put in front of the
             * kernel's fixed camera with toView, like scene 4. Glass density is per world unit, so
             * each piece's is picked from how thick it is in world units.
             * ===================================================================================== */
            final float S          = 10f;
            final float VIEW_PITCH = 2f;
            final float GLASS_IOR  = 1.72f; // lead crystal - bends light more than window glass (1.5)
            final int TENDRIL_SIDES = 16, TENDRIL_STEPS = 56, TENDRIL_TRIES = 1500;
            final int REED_SIDES    = 18, REED_STEPS    = 60, REEDS         = 22;
            final int BOWL_RINGS    = 48, BOWL_SIDES    = 160;

            // --- MATERIALS ---
            // painted walls in proper colours, each a mid tone - saturated enough to read, dark
            // enough that light doesn't just pile up round the room and wash everything out
            Material ceiling   = new Material(new Vec3(190, 186, 178), 0f, 0.90f, 0f); // warm grey
            Material backWall  = new Material(new Vec3( 95, 150, 135), 0f, 0.90f, 0f); // deep sage
            Material leftWall  = new Material(new Vec3(205, 110,  85), 0f, 0.90f, 0f); // terracotta
            Material rightWall = new Material(new Vec3(100, 130, 205), 0f, 0.90f, 0f); // cornflower
            Material frontWall = new Material(new Vec3(200, 165, 100), 0f, 0.90f, 0f); // ochre, behind the camera
            // black granite polished to a mirror: all its colour comes from what it reflects, and
            // ior 2.2 reflects ~14% head-on, climbing fast at the low angles the camera sees it at
            Material floor   = new Material(new Vec3( 20,  20,  24), 0f, 0.1f, 0f, 2.2f, 1f);
            Material lampBody = new Material(new Vec3( 28,  28,  30), 0f, 0.35f, 0f);          // spotlight cans, stems, tracks
            // the bulb only needs to blow out when seen head-on, and anything far brighter than that just
            // turns into fireflies when a bounce happens to hit it - so it's a dim grey, lit very hard
            Material bulb     = new Material(new Vec3(120, 116, 108), 0f, 1.00f, 0f);
            Material plinth  = new Material(new Vec3(228, 228, 225), 0f, 0.60f, 0f);
            Material steel   = new Material(new Vec3(200, 202, 206), 1f, 0.15f, 0f);
            Material coreMat = new Material(new Vec3(255, 150,  40), 0f, 0.40f, 0f);          // hidden in the sun's heart

            // glass: colour is what survives 1 world unit x density of travel inside
            // The sun's glass is slightly opalescent - 85% transmissive, the rest scattering like milky
            // glass. In this kernel fully clear glass never takes light itself (it only shows what it
            // reflects and refracts, and it blocks shadow rays), so without this the spotlights aimed
            // at the sun would have almost nothing to light. SUN_CLARITY = 1 for perfectly clear glass.
            final float SUN_CLARITY = 0.85f;
            Material[] sunGlass = {
                new Material(new Vec3(255, 214,  40), 0f, 0f, SUN_CLARITY, GLASS_IOR, 0.6f), // yellow
                new Material(new Vec3(255, 160,  30), 0f, 0f, SUN_CLARITY, GLASS_IOR, 0.6f), // amber
                new Material(new Vec3(255, 110,  25), 0f, 0f, SUN_CLARITY, GLASS_IOR, 0.6f), // orange
                new Material(new Vec3(235,  45,  35), 0f, 0f, SUN_CLARITY, GLASS_IOR, 0.5f), // red
            };
            Material[] reedGlass = {
                new Material(new Vec3( 50, 100, 255), 0f, 0f, 1f, GLASS_IOR, 0.45f), // cobalt
                new Material(new Vec3(150,  70, 255), 0f, 0f, 1f, GLASS_IOR, 0.45f), // violet
                new Material(new Vec3( 30, 210, 200), 0f, 0f, 1f, GLASS_IOR, 0.45f), // teal
            };
            Material rose   = new Material(new Vec3(255,  60, 150), 0f, 0f, 1f, GLASS_IOR, 0.9f);
            Material lime   = new Material(new Vec3(170, 255,  50), 0f, 0f, 1f, GLASS_IOR, 0.9f);
            Material cobalt = new Material(new Vec3( 60, 120, 255), 0f, 0f, 1f, GLASS_IOR, 0.9f);

            ArrayList<Triangle> w = new ArrayList<>();
            ArrayList<Sphere> b = new ArrayList<>();
            ArrayList<GlassPiece> placed = new ArrayList<>();
            java.util.Random rnd = new java.util.Random(909);

            // --- ROOM --- 14 wide, 12 deep, 6 high; the camera stands inside it
            final float RX = 7f, RY = 6f, ZB = -7f, ZF = 5f;
            addQuad(w, v(-RX, 0f, ZF),  v(RX, 0f, ZF),  v(RX, 0f, ZB),  v(-RX, 0f, ZB),  floor, ORIGIN);
            addQuad(w, v(-RX, RY, ZF),  v(RX, RY, ZF),  v(RX, RY, ZB),  v(-RX, RY, ZB),  ceiling, ORIGIN);
            addQuad(w, v(-RX, 0f, ZB),  v(RX, 0f, ZB),  v(RX, RY, ZB),  v(-RX, RY, ZB),  backWall, ORIGIN);
//            addQuad(w, v(-RX, 0f, ZF),  v(RX, 0f, ZF),  v(RX, RY, ZF),  v(-RX, RY, ZF),  frontWall, ORIGIN);
//            addQuad(w, v(-RX, 0f, ZB),  v(-RX, 0f, ZF), v(-RX, RY, ZF), v(-RX, RY, ZB),  leftWall, ORIGIN);
//            addQuad(w, v( RX, 0f, ZB),  v( RX, 0f, ZF), v( RX, RY, ZF), v( RX, RY, ZB),  rightWall, ORIGIN);

            // Placed in this order - floats, plinth and bowls, reeds, then the sun - so the sun's
            // tendrils can be tested against everything else and simply skipped where they'd touch.

            // --- FLOATS --- big glass spheres resting on the floor: {x, z, radius}
            float[][] floats = {{1.1f, 0.4f, 0.30f}, {-0.3f, 0.7f, 0.24f}, {2.0f, -0.2f, 0.34f}, {0.55f, -0.45f, 0.28f}};
            Material[] floatGlass = {
                new Material(new Vec3(200, 245, 255), 0f, 0f, 1f, GLASS_IOR, 0.05f), // aqua
                new Material(new Vec3(255, 190,  80), 0f, 0f, 1f, GLASS_IOR, 0.12f), // amber
                new Material(new Vec3(250, 250, 255), 0f, 0f, 1f, GLASS_IOR, 0.02f), // clear
                new Material(new Vec3(190, 120, 255), 0f, 0f, 1f, GLASS_IOR, 0.08f), // violet
                new Material(new Vec3(120, 255, 140), 0f, 0f, 1f, GLASS_IOR, 0.12f), // green
            };
            for (int i = 0; i < floats.length; i++) {
                b.add(new Sphere(v(floats[i][0], floats[i][2] + 0.002f, floats[i][1]), floats[i][2], floatGlass[i]));
            }

            // --- MACCHIA BOWLS --- on a long low plinth, each resting a hair above it: {x, radius, height, wall, waves, amp}
            final float PLINTH_TOP = 0.6f, BOWL_Z = -1.6f;
            float[] plinthMin = {1.7f, 0f, -2.3f}, plinthMax = {4.7f, PLINTH_TOP, -0.9f};
            addBox(w, v(plinthMin[0], plinthMin[1], plinthMin[2]), v(plinthMax[0], plinthMax[1], plinthMax[2]), plinth, ORIGIN);
            float[][] bowls = {{2.30f, 0.50f, 0.45f, 0.032f, 7, 0.08f}, {3.35f, 0.40f, 0.34f, 0.030f, 5, 0.07f}, {4.20f, 0.32f, 0.36f, 0.026f, 9, 0.05f}};
            Material[] bowlGlass = {rose, lime, cobalt};
            for (int i = 0; i < bowls.length; i++) {
                float[] bw = bowls[i];
                addVessel(w, v(bw[0], PLINTH_TOP + 0.003f, BOWL_Z), bw[1], bw[2], bw[3], (int) bw[4], bw[5], 0.3 + 1.7 * i, BOWL_RINGS, BOWL_SIDES, bowlGlass[i]);
            }

            // --- REEDS --- tall tapered spears leaning out from a patch of floor
            float reedX = -2.5f, reedZ = -1.8f;
            for (int i = 0, made = 0; i < 400 && made < REEDS; i++) {
                double a = rnd.nextDouble() * 2 * Math.PI;
                float rad = 0.8f * (float) Math.sqrt(rnd.nextFloat());
                float bx = reedX + rad * (float) Math.cos(a), bz = reedZ + rad * (float) Math.sin(a);
                float baseR = 0.05f + 0.03f * rnd.nextFloat();
                float lean = (float) Math.toRadians(4 + 12 * rnd.nextFloat()) * (0.4f + rad / 0.8f); // outer reeds lean further
                Vec3 dir = unit(v((float) Math.cos(a) * (float) Math.sin(lean), (float) Math.cos(lean), (float) Math.sin(a) * (float) Math.sin(lean)));
                Vec3 start = v(bx, 0.7f * baseR + 0.003f, bz); // rounded foot just off the floor
                GlassPiece piece = GlassPiece.sweep(start, dir, randomUnit(rnd), 2.0f + 1.6f * rnd.nextFloat(), baseR, 0.95f,
                                                    0f, 0.4f, 0.35f + 0.3f * rnd.nextFloat(), REED_STEPS, 0.1f);
                if (piece.hitsAny(placed) || piece.hitsFloats(b)) continue;
                placed.add(piece);
                addTube(w, piece.centres, piece.radii, REED_SIDES, 5, 0.08f, 3f, reedGlass[rnd.nextInt(reedGlass.length)]);
                made++;
            }

            // --- THE SUN --- hung from the ceiling on a rod through its core
            Vec3 core = v(0f, 2.55f, -1.2f);
            final float CORE_R = 0.42f, ROD_R = 0.03f;
            b.add(new Sphere(v(core.x, core.y, core.z), CORE_R, coreMat));
            addLathe(w, v(core.x, core.y, core.z), 1f, new Profile().to(ROD_R, 0f).to(ROD_R, RY - core.y), 16, steel);

            int tendrils = 0;
            for (int i = 0; i < TENDRIL_TRIES; i++) {
                // golden-spiral directions round the core, visited in a scrambled order
                int k = (int) ((i * 997L) % TENDRIL_TRIES);
                float dy = 1f - 2f * (k + 0.5f) / TENDRIL_TRIES, rr = (float) Math.sqrt(1 - dy * dy);
                double phi = k * 2.399963;
                Vec3 dir = v(rr * (float) Math.cos(phi), dy, rr * (float) Math.sin(phi));
                if (dir.y > 0.93f) continue; // the rod goes up through here

                float baseR = 0.04f + 0.018f * rnd.nextFloat();
                float length = 0.8f + 1.0f * rnd.nextFloat();
                float curl = 2f + 4f * rnd.nextFloat();
                Vec3 start = lin(core, dir, CORE_R + 0.7f * baseR + 0.012f); // its rounded end clears the core
                GlassPiece piece = GlassPiece.sweep(start, dir, randomUnit(rnd), length, baseR, 0.85f, curl, 1.5f, 0f, TENDRIL_STEPS, 0.15f);

                if (piece.below(0.05f) || piece.hitsRod(core.x, core.z, core.y, ROD_R) || piece.hitsBox(plinthMin, plinthMax, 0.6f)
                        || piece.hitsFloats(b) || piece.hitsAny(placed)) continue;
                placed.add(piece);
                addTube(w, piece.centres, piece.radii, TENDRIL_SIDES, 6, 0.12f, 8f, sunGlass[rnd.nextInt(sunGlass.length)]);
                tendrils++;
            }

            /* --- LIGHTS --- gallery track spotlights, and next to nothing else, so the room stays dark
             * and each piece sits in its own pool of light. Every lamp is a real fixture (addSpotlight):
             * a black can on a stem off a ceiling track, a white bulb inside and the point light just in
             * front of it. Shadow rays from anywhere outside the can's mouth hit the can, so each lamp
             * throws a cone (~18 degrees either side) rather than lighting the whole room, and the bulb -
             * lit from a hair away - blows out white: it glows inside the can and shows up as a sharp
             * highlight in the glass and the floor. (Point lights themselves never appear in
             * reflections in this kernel, and nothing glows on its own, so this is how a lamp is seen.)
             * The back track is in shot; the front one is above the camera, seen only in reflections.
             * {x on the track, target x, y, z, intensity (x S^2)}. */
            ArrayList<PointLight> rig = new ArrayList<>();
            final float BACK_TRACK = -3.3f, FRONT_TRACK = 0.9f;
            addBox(w, v(-4.2f, RY - 0.04f, BACK_TRACK - 0.03f), v(4.2f, RY, BACK_TRACK + 0.03f), lampBody, ORIGIN);
            addBox(w, v(-4.2f, RY - 0.04f, FRONT_TRACK - 0.03f), v(4.2f, RY, FRONT_TRACK + 0.03f), lampBody, ORIGIN);
            // every lamp on the sun except one on the bowls; aim points spread over the sun so the
            // narrow beams cover all of it between them. These are the only lights in the room.
            float[][] backSpots = {
                {-3.2f, -0.7f, 2.2f, -1.2f, 18f}, // sun, from behind-left, low side
                {-1.6f,  0.2f, 3.0f, -1.3f, 18f}, // sun, from behind, top
                { 1.6f,  0.6f, 2.2f, -1.1f, 18f}, // sun, from behind-right
                { 3.4f,  3.3f, 0.8f, -1.6f, 14f}, // the bowls
            };
            float[][] frontSpots = {
                {-1.2f, -0.3f, 2.8f, -1.2f, 22f}, // sun, the key, from the front
                { 1.4f,  0.5f, 2.4f, -1.2f, 20f}, // sun, front-right
                {-1.8f, -0.6f, 1.9f, -1.0f, 20f}, // sun, front-left, lower tendrils
                { 3.0f,  0.9f, 1.8f, -1.2f, 18f}, // sun, low right side
            };
            for (float[] s : backSpots)  addSpotlight(w, b, rig, v(s[0], RY, BACK_TRACK),  v(s[1], s[2], s[3]), s[4] * S * S, lampBody, bulb);
            for (float[] s : frontSpots) addSpotlight(w, b, rig, v(s[0], RY, FRONT_TRACK), v(s[1], s[2], s[3]), s[4] * S * S, lampBody, bulb);

            // --- INTO THE CAMERA --- standing in the room, eye height, looking at the sun
            float pitch = (float) Math.toRadians(VIEW_PITCH);
            Vec3 eye = v(0f, 1.6f, 2.4f);
            for (Triangle t : w) {
                t.p1 = toView(t.p1, eye, pitch, S, positionOffset);
                t.p2 = toView(t.p2, eye, pitch, S, positionOffset);
                t.p3 = toView(t.p3, eye, pitch, S, positionOffset);
            }
            for (Sphere s : b) {
                s.pos = toView(s.pos, eye, pitch, S, positionOffset);
                s.radius *= S;
            }
            for (PointLight l : rig) l.pos = toView(l.pos, eye, pitch, S, positionOffset);

            System.out.println("scene 5: " + tendrils + " tendrils placed");
            triangles.addAll(w);
            spheres.addAll(b);
            pointLights.addAll(rig);
        }

        if (scene == 6) {
            /* =====================================================================================
             * "RIG" - the gaming PC from the photo: a dual-chamber, pillarless Lian Li case with the
             * glass off, on a dark desk against a wall, lit by nothing but its own RGB.
             *   - case: base, top, rear wall, motherboard tray, the closed rear chamber and its front
             *     pillar, rubber feet and grommets, LIAN LI on the top edge
             *   - nine UNI FAN TL style fans: three on top pulling through the radiator, one rear
             *     exhaust, three bottom intakes and two 140s on the tray beside the board (addFan)
             *   - white ATX board: finned VRM heatsinks, I/O shroud, DIMM slots with two white
             *     Vengeance sticks, PCIe slot, M.2 and chipset heatsinks, sockets, screws
             *   - AIO pump with its LCD (36 degrees, a gauge, the pump speed) sunk in a lit bezel,
             *     braided tubes up to a 360 radiator above the top fans
             *   - an RTX 5090 Founders Edition on a vertical mount: gunmetal shroud with the X, two
             *     seven-blade fans over fin stacks, GEFORCE RTX lit along the top edge, the 12V-2x6
             *     cable, I/O bracket, slot covers and the riser bracket
             *   - sleeved 24-pin, 8-pin and GPU cables running into the grommets
             *
             * Nothing is emissive. Every glow is a white surface lit very hard by point lights boxed
             * in so they only reach what they're meant to:
             *   - light bars: a white-lined tray on the side of each fan with two lights inside;
             *     the tray's walls block the light everywhere except out of its mouth
             *   - pump: one light sunk inside the bezel, so the bezel's inside wall blows out - from
             *     the side it's the lit crescent in the photo - and the light only leaves forwards
             *   - GEFORCE RTX: two lights in a channel along the top edge, over white letters
             *   - fan hubs: a light just in front of each hub lights its ring, the blades and the
             *     chamfer strips, and throws the fan's glow into the case like the real ones
             * They all take PC_RGB (top of this file) except the GEFORCE RTX logo, which is white on
             * a real Founders Edition. Behind the case, two more PC_RGB lights wash the wall.
             *
             * No glass: this kernel doesn't bend rays on the way out of glass (ratioOfIor is 1 on
             * exit), so a pane would shift the whole interior like a prism, and shadow rays treat
             * glass as solid, so it would also stop the RGB from reaching the desk.
             *
             * Built in centimetres with the desk at y = 0. The case is built in its own space
             * (x rear -> front, y up, z from the back panel to where the side glass would be), then
             * stood on the desk and turned, and everything goes through toView like scenes 3-5. The
             * kernel jitters every light by up to 0.35 units, so S = 1 (1 unit = 1 cm) keeps each
             * light inside the ~1 cm channels built for it.
             * ===================================================================================== */
            final float S = 1f;
            final float VIEW_PITCH = 10f, YAW = -42f;
            final float HUB_I = 900f, BAR_I = 300f, PUMP_I = 700f, LOGO_I = 150f, BIAS_I = 1100f;
            final int BAR_LIGHTS = 2;

            // --- MATERIALS ---
            Material caseBlack = new Material(v( 12,  12,  14), 0f, 0.50f, 0f); // powder-coated steel
            Material trim      = new Material(v( 40,  40,  46), 1f, 0.35f, 0f); // black anodised: brackets, slot covers
            Material rubber    = new Material(v(  8,   8,   8), 0f, 0.95f, 0f);
            Material plastic   = new Material(v( 16,  16,  18), 0f, 0.40f, 0f); // fan frames, sockets, slots
            Material blade     = new Material(v(232, 228, 250), 0f, 0.45f, 0f); // milky fan blades
            Material glow      = new Material(v(255, 255, 255), 0f, 0.90f, 0f); // every LED diffuser, the LCD's text
            Material mirror    = new Material(v(  4,   4,   6), 0f, 0.04f, 0f, 1.6f, 1f); // infinity-mirror hubs, the LCD glass
            Material ringGrey  = new Material(v( 70,  70,  80), 0f, 0.30f, 0f);
            Material pcb       = new Material(v(206, 206, 212), 0f, 0.60f, 0f); // white board
            Material alu       = new Material(v(214, 216, 222), 1f, 0.28f, 0f); // silver heatsinks
            Material whitePlas = new Material(v(232, 232, 236), 0f, 0.45f, 0f); // I/O shroud, RAM
            Material ink       = new Material(v( 64,  64,  70), 0f, 0.50f, 0f); // printing
            Material badge     = new Material(v( 70,  70,  76), 0f, 0.40f, 0f);
            Material gunmetal  = new Material(v( 72,  74,  80), 1f, 0.38f, 0f); // GPU shroud
            Material polished  = new Material(v(160, 162, 170), 1f, 0.12f, 0f); // the X
            Material fins      = new Material(v( 46,  46,  50), 1f, 0.50f, 0f);
            Material fanBlack  = new Material(v( 22,  22,  24), 0f, 0.50f, 0f); // GPU fans
            Material sleeve    = new Material(v( 20,  20,  22), 0f, 0.75f, 0f); // braided tubes, cables
            Material gloss     = new Material(v( 14,  14,  16), 0f, 0.10f, 0f); // pump can, radiator tank
            Material steel     = new Material(v(180, 182, 188), 1f, 0.30f, 0f); // screws
            Material desk      = new Material(v( 36,  32,  30), 0f, 0.30f, 0f);
            Material wall      = new Material(v(100, 100, 112), 0f, 0.90f, 0f);

            ArrayList<Triangle> w = new ArrayList<>();
            ArrayList<Sphere> b = new ArrayList<>();
            ArrayList<PointLight> rig = new ArrayList<>();
            Vec3 rgb = PC_RGB;
            FanLook fan = new FanLook(plastic, blade, glow, mirror, ringGrey);

            // --- CASE --- in its own space: x rear -> front, y up, z back panel -> side
            final float D = 46.5f, H = 46f, W = 29f;
            final float FLOOR = 2.2f, CEIL = 45.1f, TRAY = 9f, MB = TRAY + 0.76f; // inside floor and ceiling, tray, board surface
            for (float fx : new float[]{4f, D - 4f}) {
                for (float fz : new float[]{4f, W - 4f}) {
                    addAxialLathe(w, v(fx, 0f, fz), v(0f, 1f, 0f), new Profile().to(0f, 0f).to(2.2f, 0f).curve(2.4f, 0f, 2.4f, 0.4f, 3)
                            .to(2.4f, 1.4f).to(0f, 1.4f), 24, 0, 0f, rubber);
                }
            }
            addBevelBox(w, v(0f, 1.4f, 0f), v(D, FLOOR, W), 0.3f, caseBlack);                    // base
            addBevelBox(w, v(0f, CEIL, 0f), v(D, H, W), 0.3f, caseBlack);                        // top
            addBevelBox(w, v(0f, FLOOR, 0f), v(0.8f, CEIL, W), 0.05f, caseBlack);               // rear wall
            addBevelBox(w, v(0.8f, FLOOR, 0f), v(D - 1.2f, CEIL, 0.5f), 0.05f, caseBlack);      // back panel
            addBevelBox(w, v(D - 1.2f, FLOOR, 0f), v(D, CEIL, TRAY + 0.4f), 0.2f, caseBlack);   // front pillar over the rear chamber
            addBevelBox(w, v(0.8f, FLOOR, TRAY - 0.2f), v(D - 1.2f, CEIL, TRAY), 0.05f, caseBlack); // motherboard tray
            for (float gy : new float[]{16f, 22.5f, 29.1f, 37f}) {
                addBevelBox(w, v(27.1f, gy - 2.2f, TRAY), v(29.3f, gy + 2.2f, TRAY + 0.25f), 0.3f, rubber);
            }
            addBevelBox(w, v(5.0f, 40.3f, TRAY), v(7.4f, 42.2f, TRAY + 0.25f), 0.3f, rubber);
            addText(w, "LIAN LI", new Frame(v(3f, 45.33f, W + 0.02f), v(1f, 0f, 0f), v(0f, 1f, 0f), v(0f, 0f, 1f)), 0.42f, 0.06f, false, badge);
            for (float[] s : new float[][]{{16.6f, 18.7f}, {24.0f, 25.8f}}) {                   // slot covers either side of the GPU bracket
                addBevelBox(w, v(0.8f, 9.6f, s[0]), v(1.05f, 26.2f, s[1]), 0.05f, trim);
            }

            // --- FANS --- front faces into the case, light bars towards the glass (the tray fans' towards the front)
            Vec3 down = v(0f, -1f, 0f), up = v(0f, 1f, 0f), toSide = v(0f, 0f, 1f), toFront = v(1f, 0f, 0f);
            for (float fx : new float[]{15.5f, 27.5f, 39.5f}) {   // top, pulling up through the radiator
                addFan(w, rig, Frame.facing(v(fx, 39.8f, 19f), down, toSide), 12f, fan, rgb, HUB_I * S * S, BAR_I * S * S, BAR_LIGHTS);
            }
            addFan(w, rig, Frame.facing(v(3.3f, 33f, 19f), toFront, toSide), 12f, fan, rgb, HUB_I * S * S, BAR_I * S * S, BAR_LIGHTS); // rear
            for (float fx : new float[]{11.3f, 23.8f, 36.3f}) {   // bottom intakes
                addFan(w, rig, Frame.facing(v(fx, 4.7f, 19f), up, toSide), 12f, fan, rgb, HUB_I * S * S, BAR_I * S * S, BAR_LIGHTS);
            }
            for (float fy : new float[]{36.6f, 22.2f}) {          // 140s on the tray, beside the board
                addFan(w, rig, Frame.facing(v(37.2f, fy, 11.7f), toSide, toFront), 14f, fan, rgb, HUB_I * S * S, BAR_I * S * S, BAR_LIGHTS);
            }

            // --- BOARD AND COOLER ---
            addMotherboard(w, b, 1.8f, 8.4f, MB, pcb, alu, whitePlas, plastic, ink, steel);
            addPump(w, rig, v(1.8f + 10.7f, 8.4f + 22.1f, MB), rgb, PUMP_I * S * S, plastic, gloss, glow, mirror, glow);
            addBevelBox(w, v(5.8f, 42.4f, 13f), v(9.4f, 45.05f, 25f), 0.25f, gloss);              // radiator end tank
            addBevelBox(w, v(9.4f, 42.5f, 13.3f), v(45.3f, 45.0f, 24.7f), 0.1f, fins);            // radiator core, over the top fans
            for (float fz : new float[]{14.6f, 17.2f}) {
                addAxialLathe(w, v(7.5f, 41.1f, fz), up, new Profile().to(0f, 0f).to(0.85f, 0f).to(0.85f, 1.3f), 20, 0, 0f, gunmetal);
            }
            float tz = MB + 3.1f;                                  // the pump's outlets
            addCable(w, 0.55f, 12, 2.5f, sleeve, v(11.4f, 34.6f, tz), v(11.4f, 38.5f, tz), v(7.5f, 37.5f, 14.6f), v(7.5f, 41.6f, 14.6f));
            addCable(w, 0.55f, 12, 2.5f, sleeve, v(13.6f, 34.6f, tz), v(13.6f, 39.0f, 13.5f), v(7.5f, 37.0f, 17.2f), v(7.5f, 41.6f, 17.2f));

            // --- GRAPHICS CARD --- on the vertical mount, fans to the side
            addGpu(w, rig, v(17.6f, 17.9f, 21.5f), LOGO_I * S * S, gunmetal, polished, fanBlack, fins, plastic, glow, trim);
            addBevelBox(w, v(14f, 9.4f, 18.6f), v(24f, 11.0f, 23.8f), 0.15f, trim);              // riser bracket

            // --- CABLES --- 12V-2x6 off the card round into the tray, the 24-pin, the 8-pin up through the top
            addCable(w, 0.75f, 14, 0f, sleeve, v(17.6f, 25.4f, 21.0f), v(17.6f, 28.5f, 21.0f), v(23.5f, 28.0f, 12.5f), v(28.1f, 22.5f, 9.8f), v(28.2f, 22.5f, 8.2f));
            addCable(w, 0.85f, 16, 0f, sleeve, v(25.9f, 29.1f, 10.6f), v(27.3f, 29.1f, 11.3f), v(28.2f, 29.1f, 10.4f), v(28.2f, 29.1f, 8.2f));
            addCable(w, 0.55f, 10, 0f, sleeve, v(6.2f, 38.6f, 10.6f), v(6.2f, 40.6f, 10.9f), v(6.2f, 41.2f, 9.8f), v(6.2f, 41.2f, 8.4f));

            // --- INTO THE ROOM --- the case on the desk, turned so its open side faces the camera's left
            float yaw = (float) Math.toRadians(YAW);
            java.util.function.UnaryOperator<Vec3> place = p -> spin(v(p.x - D / 2, p.y, p.z - W / 2), 0f, yaw);
            for (Triangle t : w) {
                t.p1 = place.apply(t.p1);
                t.p2 = place.apply(t.p2);
                t.p3 = place.apply(t.p3);
            }
            for (Sphere s : b) s.pos = place.apply(s.pos);
            for (PointLight l : rig) l.pos = place.apply(l.pos);

            addQuad(w, v(-250f, 0f, -48f), v(250f, 0f, -48f), v(250f, 0f, 60f), v(-250f, 0f, 60f), desk, ORIGIN);
            addQuad(w, v(-250f, 0f, -48f), v(250f, 0f, -48f), v(250f, 220f, -48f), v(-250f, 220f, -48f), wall, ORIGIN);
            rig.add(new PointLight(v(-9f, 16f, -37f), new Vec3(rgb), BIAS_I * S * S)); // behind the case, on the wall
            rig.add(new PointLight(v( 9f, 30f, -37f), new Vec3(rgb), BIAS_I * S * S));

            // --- INTO THE CAMERA --- sitting at the desk, a little above the case, looking down at it
            float pitch = (float) Math.toRadians(VIEW_PITCH);
            Vec3 eye = v(0f, 34f, 60f);
            for (Triangle t : w) {
                t.p1 = toView(t.p1, eye, pitch, S, positionOffset);
                t.p2 = toView(t.p2, eye, pitch, S, positionOffset);
                t.p3 = toView(t.p3, eye, pitch, S, positionOffset);
            }
            for (Sphere s : b) {
                s.pos = toView(s.pos, eye, pitch, S, positionOffset);
                s.radius *= S;
            }
            for (PointLight l : rig) l.pos = toView(l.pos, eye, pitch, S, positionOffset);

            System.out.println("scene 6: " + w.size() + " triangles, " + b.size() + " spheres, " + rig.size() + " lights");
            triangles.addAll(w);
            spheres.addAll(b);
            pointLights.addAll(rig);
        }

        if (scene == 7) {
            /* =====================================================================================
             * "DREAMLINER" - a Boeing 787-9 parked on a wet apron, ported from the CPU
             * renderer (IndianArjun94/3D-Path-Tracer-CPU, branch vibecode, scene 7). The aircraft
             * itself is models/Boeing787 - lofted from Boeing's 3-view, with raked wingtips, GEnx
             * nacelles with chevrons, real windows into a flight deck and cabin, gear, antennas.
             *
             * What changed in the port:
             *   - nothing glows: the CUDA kernel has no emission yet, so every emissive material
             *     on the aircraft (cabin and flight deck lighting, displays, nav lights, beacons,
             *     strobes, landing lights) is an ordinary surface in its lens / diffuser colour
             *   - the scene's own emitters are gone: the blue taxiway edge lights and the glowing
             *     lamp spheres on the flood masts
             *   - the paint is glossier: roughness 0 and metallic 0.15 on the white paint (0.26 kept
             *     on the wings), so the skin mirrors the floods, the walls and the apron; and the
             *     landing gear is reworked in models/Boeing787 (see its LANDING GEAR note)
             *   - there's no sky in this kernel (a miss is black), so the apron is walled in 100 m out
             *     by a 200 m tall backdrop painted in bands from a warm horizon up to night blue,
             *     washed by rows of dim lights in front of it - it reads as a dusk sky
             *   - lights do the work emission did, and they're directional: four aimed floodlights on
             *     masts (addAimedFlood), each from its own side in its own colour - key, rim, fill
             *     and an amber accent - plus a faint fill from high overhead. Each flood's light is a
             *     small area light: a grid of point lights sharing one power (addAreaLightGrid) -
             *     the kernel only has point lights - inside a shroud that only lets it out forwards.
             *
             * Built in metres (the CPU scene's units, the aircraft at the origin, nose to -z,
             * starboard +x, apron at y = 0) and scaled by S into world units: at 1:1, 2,248 of the
             * aircraft's smallest triangles would be under hitTriangle's minimum size; at S = 10,
             * 15 are. The kernel's camera is fixed, so the set is turned in front of it (aimView):
             * from ahead of the aircraft, 25 degrees off the nose to starboard, at eye height and
             * looking almost level - and 40 m back, since this camera is 90 degrees wide rather than
             * a long lens, and any closer the nose balloons.
             * ===================================================================================== */
            positionOffset.add(new Vec3(-2,0,7));

            final float S = 10f;
            final float WALL_HALF = 100f, WALL_H = 200f; // backdrop: 200 m square, tall enough to reach the top of a level camera's frame even at the far corner
            // light power, all in metre units (scaled by S^2 when the lights are made)
            final float WASH_I = 120f;    // each of the lights washing the backdrop walls
            final float SKY_I = 80f;      // each of the faint 3 x 3 overhead fill
            // the paint: mirror smooth with a touch of metal, so the skin reflects the floods, the walls and the apron
            final float PAINT_METALLIC = 0.15f, PAINT_ROUGHNESS = 0f;

            Material concrete    = new Material(v(104, 107, 112), 0f, 0.17f, 0f); // wet: glossy on purpose
            Material joint       = new Material(v( 70,  72,  76), 0f, 0.30f, 0f);
            Material paintYellow = new Material(v(236, 192,  52), 0f, 0.35f, 0f);
            Material pole        = new Material(v( 96,  98, 102), 0.65f, 0.35f, 0f);
            Material housing     = new Material(v( 40,  42,  46), 0.30f, 0.50f, 0f);
            Material reflector   = new Material(v(235, 232, 226), 0f, 0.90f, 0f); // not emissive: lit from 0.3 m

            ArrayList<Triangle> w = new ArrayList<>();
            ArrayList<Sphere> b = new ArrayList<>();
            ArrayList<PointLight> rig = new ArrayList<>();

            // --- APRON --- one slab out to the walls, the joint grid that gives it scale, the lead-in line
            final float APRON = WALL_HALF + 5f;
            addQuad(w, v(-APRON, 0f, APRON), v(APRON, 0f, APRON), v(APRON, 0f, -APRON), v(-APRON, 0f, -APRON), concrete, ORIGIN);
            final float SLAB = 7.5f, EXTENT = 97.5f, HALF = 0.055f, JOINT_Y = 0.004f;
            for (float p = -EXTENT; p <= EXTENT; p += SLAB) {
                addQuad(w, v(p - HALF, JOINT_Y, EXTENT), v(p + HALF, JOINT_Y, EXTENT), v(p + HALF, JOINT_Y, -EXTENT), v(p - HALF, JOINT_Y, -EXTENT), joint, ORIGIN);
                addQuad(w, v(-EXTENT, JOINT_Y, p - HALF), v(EXTENT, JOINT_Y, p - HALF), v(EXTENT, JOINT_Y, p + HALF), v(-EXTENT, JOINT_Y, p + HALF), joint, ORIGIN);
            }
            addQuad(w, v(-0.17f, 0.008f, 55f), v(0.17f, 0.008f, 55f), v(0.17f, 0.008f, -EXTENT), v(-0.17f, 0.008f, -EXTENT), paintYellow, ORIGIN);

            // --- THE AIRCRAFT ---
            Boeing787 jet = new Boeing787();
            jet.landingLightsOn = true; // their lenses, anyway - nothing lights up
            jet.materials.white     = new Material(v(247, 247, 249), PAINT_METALLIC, PAINT_ROUGHNESS, 0f);
            jet.materials.nacelle   = new Material(v(242, 242, 245), PAINT_METALLIC, PAINT_ROUGHNESS, 0f);
            jet.materials.pylon     = new Material(v(238, 239, 242), PAINT_METALLIC, PAINT_ROUGHNESS, 0f);
            jet.materials.wingUpper = new Material(v(222, 225, 230), 0.26f, PAINT_ROUGHNESS, 0f); // already more metallic than 0.15
            jet.materials.wingLower = new Material(v(190, 195, 202), 0.26f, PAINT_ROUGHNESS, 0f);
            jet.build(w, b, 0, 0, 0, 0);

            /* --- FLOODLIGHTS --- the lighting is directional: four aimed floods on masts, each from
             * its own side and in its own colour, instead of light from everywhere. Each is a
             * shrouded head (addAimedFlood) - light leaves only through its mouth, as a beam about 45
             * degrees either side across and 28 up and down - so the aircraft is lit from four
             * directions and the walls only catch what spills past it.
             * {azimuth: 0 dead ahead of the nose, 90 starboard; distance; head height; aim x, y, z;
             *  r, g, b; power}, distances and power in metres. */
            float[][] floods = {
                { 80f, 62f, 26f, 0f, 4.5f,  -4f, 255, 228, 196, 8000f}, // key: high on the starboard side, warm white
                {235f, 66f, 30f, 0f, 7.0f,   4f, 175, 200, 255, 6000f}, // rim: high, behind to port, cool - outlines the crown and fin
                {-55f, 56f, 14f, 0f, 4.0f, -10f, 240, 240, 245, 2000f}, // fill: low, ahead to port, soft and neutral
                {140f, 62f, 22f, 2f, 5.0f,   8f, 255, 176, 110, 3600f}, // accent: behind to starboard, sodium amber on the fin and engines
            };
            for (float[] f : floods) {
                double az = Math.toRadians(f[0]);
                Vec3 head = v(f[1] * (float) Math.sin(az), f[2], -f[1] * (float) Math.cos(az));
                addAimedFlood(w, rig, head, v(f[3], f[4], f[5]), pole, housing, reflector, v(f[6], f[7], f[8]), f[9] * S * S);
            }

            // a faint fill from high overhead, so the sides the floods miss aren't pure black
            for (int i = -1; i <= 1; i++) {
                for (int j = -1; j <= 1; j++) {
                    rig.add(new PointLight(v(40f * i, 70f, 40f * j), v(150, 170, 220), SKY_I * S * S));
                }
            }

            // --- BACKDROP --- four walls, painted in bands from a warm horizon up to night blue so they
            // read as a dusk sky rather than a wall, and washed by rows of dim lights in front of them
//            final int BANDS = 40;
//            Vec3 horizon = v(178, 126, 96), zenith = v(30, 46, 92);
//            float[][] walls = { // {x0, z0, x1, z1, inward x, inward z} - each wall's ends, and which way the room is
//                {-WALL_HALF,  WALL_HALF,  WALL_HALF,  WALL_HALF, 0f, -1f},
//                {-WALL_HALF, -WALL_HALF,  WALL_HALF, -WALL_HALF, 0f,  1f},
//                {-WALL_HALF, -WALL_HALF, -WALL_HALF,  WALL_HALF, 1f,  0f},
//                { WALL_HALF, -WALL_HALF,  WALL_HALF,  WALL_HALF, -1f, 0f},
//            };
//            for (float[] wl : walls) {
//                for (int k = 0; k < BANDS; k++) {
//                    float t = (k + 0.5f) / BANDS, y0 = WALL_H * k / BANDS, y1 = WALL_H * (k + 1) / BANDS;
//                    Material band = new Material(v(lerp(horizon.x, zenith.x, t), lerp(horizon.y, zenith.y, t), lerp(horizon.z, zenith.z, t)), 0f, 0.95f, 0f);
//                    addQuad(w, v(wl[0], y0, wl[1]), v(wl[2], y0, wl[3]), v(wl[2], y1, wl[3]), v(wl[0], y1, wl[1]), band, ORIGIN);
//                }
//                // wash: columns every 25 m along the wall, at four heights, 12 m out from it
//                float len = (float) Math.hypot(wl[2] - wl[0], wl[3] - wl[1]);
//                for (float s = 12.5f; s < len; s += 25f) {
//                    float f = s / len, px = lerp(wl[0], wl[2], f) + 12f * wl[4], pz = lerp(wl[1], wl[3], f) + 12f * wl[5];
//                    for (float h : new float[]{15f, 60f, 110f, 165f}) {
//                        rig.add(new PointLight(v(px, h, pz), v(255, 236, 214), WASH_I * S * S));
//                    }
//                }
//            }

            // --- INTO THE CAMERA --- 25 degrees off the nose to starboard, at eye height, looking almost level
            Vec3 look = v(0f, 5f, -8f);
            Vec3 eye = v(0.423f * 40f, 6f, -8f - 0.906f * 40f);
            float yaw = (float) Math.atan2(look.x - eye.x, -(look.z - eye.z));
            float pitch = (float) Math.atan2(eye.y - look.y, (float) Math.hypot(look.x - eye.x, look.z - eye.z));
            for (Triangle t : w) {
                t.p1 = aimView(t.p1, eye, yaw, pitch, S, positionOffset);
                t.p2 = aimView(t.p2, eye, yaw, pitch, S, positionOffset);
                t.p3 = aimView(t.p3, eye, yaw, pitch, S, positionOffset);
            }
            for (Sphere s : b) {
                s.pos = aimView(s.pos, eye, yaw, pitch, S, positionOffset);
                s.radius *= S;
            }
            for (PointLight l : rig) l.pos = aimView(l.pos, eye, yaw, pitch, S, positionOffset);

            System.out.println("scene 7: " + w.size() + " triangles, " + b.size() + " spheres, " + rig.size() + " lights");
            triangles.addAll(w);
            spheres.addAll(b);
            pointLights.addAll(rig);
        }
    }

    // small vertex shorthand
    private static Vec3 v(float x, float y, float z) { return new Vec3(x, y, z); }

    // p + o as a NEW vector - Vec3's own add() works in place, which moves every triangle sharing p
    private static Vec3 moved(Vec3 p, Vec3 o) { return v(p.x + o.x, p.y + o.y, p.z + o.z); }

    /* One quad from 4 corners (winding-independent: the point-in-triangle test is self-consistent),
     * moved by off. The corners are copied, never changed: this used to add off to them in place,
     * which gave p0 and p2 the offset twice (each is in both triangles) and dragged along every
     * other triangle sharing a corner - any non-zero offset sheared the shape. */
    private static void addQuad(ArrayList<Triangle> triangles, Vec3 p0, Vec3 p1, Vec3 p2, Vec3 p3, Material m, Vec3 off) {
        Vec3 a = moved(p0, off), b = moved(p1, off), c = moved(p2, off), d = moved(p3, off);
        triangles.add(new Triangle(a, b, c, m));
        triangles.add(new Triangle(a, c, d, m));
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


    // ============================ scene 2: the ring ============================

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
            out.add(new Triangle(moved(centre, off), moved(polar(top, radius - bevel, a0, 0f), off),
                                 moved(polar(top, radius - bevel, a1, 0f), off), m)); // top face (centre is shared: never move it in place)
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


    // ============================ scene 3: the chess set ============================

    /* A lathe profile: (radius, height) points from the bottom of a piece to the top. Radius 0
     * means "on the axis", and addLathe closes the surface there with a fan instead of slivers. */
    private static class Profile {
        final ArrayList<float[]> pts = new ArrayList<>();

        Profile to(float r, float y) {
            if (r < 1e-5f) r = 0f; // cos(90) isn't exactly 0 - snap it so poles stay poles
            if (!pts.isEmpty()) {
                float[] last = pts.get(pts.size() - 1);
                if (Math.abs(last[0] - r) < 1e-5f && Math.abs(last[1] - y) < 1e-5f) return this; // no zero-length rings
            }
            pts.add(new float[]{r, y});
            return this;
        }

        // quadratic bezier from the last point, pulled towards (cr, cy)
        Profile curve(float cr, float cy, float r, float y, int steps) {
            float[] s = pts.get(pts.size() - 1);
            for (int i = 1; i <= steps; i++) {
                float t = (float) i / steps, u = 1 - t;
                to(u * u * s[0] + 2 * u * t * cr + t * t * r, u * u * s[1] + 2 * u * t * cy + t * t * y);
            }
            return this;
        }

        // arc around (cr, cy): 0 deg points outwards, 90 deg points up
        Profile arc(float cr, float cy, float radius, float fromDeg, float toDeg, int steps) {
            for (int i = 0; i <= steps; i++) {
                double a = Math.toRadians(lerp(fromDeg, toDeg, (float) i / steps));
                to(cr + radius * (float) Math.cos(a), cy + radius * (float) Math.sin(a));
            }
            return this;
        }
    }

    /* The foot every Staunton piece shares, for a base of radius R: felt, outer edge, rounded
     * shoulder, a bead, then a cove curving up into the stem. Ends at (0.52R, 0.55R). */
    private static Profile stauntonBase(float R) {
        return new Profile()
                .to(0f, 0f).to(R, 0f).to(R, 0.12f * R)
                .arc(0.90f * R, 0.12f * R, 0.10f * R, 0f, 90f, 3)   // shoulder
                .to(0.82f * R, 0.22f * R)
                .arc(0.82f * R, 0.30f * R, 0.08f * R, -90f, 90f, 4) // bead
                .to(0.70f * R, 0.38f * R)
                .curve(0.55f * R, 0.40f * R, 0.52f * R, 0.55f * R, 4); // cove
    }

    // sweeps a profile around a vertical axis standing on base -> about 2 * sides triangles per profile segment
    private static void addLathe(ArrayList<Triangle> out, Vec3 base, float scale, Profile profile, int sides, Material m) {
        int n = profile.pts.size();
        Vec3[][] rings = new Vec3[n][];

        for (int k = 0; k < n; k++) {
            float r = profile.pts.get(k)[0] * scale, y = profile.pts.get(k)[1] * scale;
            if (r == 0f) {
                rings[k] = new Vec3[]{v(base.x, base.y + y, base.z)}; // a pole: one point, not a ring
            } else {
                rings[k] = new Vec3[sides];
                for (int i = 0; i < sides; i++) rings[k][i] = polar(base, r, 2 * Math.PI * i / sides, y);
            }
        }

        for (int k = 0; k + 1 < n; k++) {
            Vec3[] a = rings[k], b = rings[k + 1];
            if (a.length == 1 && b.length == 1) continue; // runs along the axis, no surface

            for (int i = 0; i < sides; i++) {
                int i2 = (i + 1) % sides;
                if (a.length == 1)      out.add(new Triangle(a[0], b[i], b[i2], m)); // fan out of a pole
                else if (b.length == 1) out.add(new Triangle(a[i], a[i2], b[0], m)); // fan into a pole
                else                    addQuad(out, a[i], a[i2], b[i2], b[i], m, ORIGIN);
            }
        }
    }

    /* A solid slice of a thick-walled tube between two angles (a rook's merlon): outer wall, inner
     * wall, top, and both end faces. No bottom - it always sits on something. */
    private static void addRingSegment(ArrayList<Triangle> out, Vec3 base, float rIn, float rOut, float y0, float y1,
                                       double a0, double a1, int steps, Material m) {
        for (int i = 0; i < steps; i++) {
            double t0 = a0 + (a1 - a0) * i / steps, t1 = a0 + (a1 - a0) * (i + 1) / steps;
            addQuad(out, polar(base, rOut, t0, y0), polar(base, rOut, t1, y0), polar(base, rOut, t1, y1), polar(base, rOut, t0, y1), m, ORIGIN);
            addQuad(out, polar(base, rIn,  t0, y0), polar(base, rIn,  t1, y0), polar(base, rIn,  t1, y1), polar(base, rIn,  t0, y1), m, ORIGIN);
            addQuad(out, polar(base, rIn,  t0, y1), polar(base, rIn,  t1, y1), polar(base, rOut, t1, y1), polar(base, rOut, t0, y1), m, ORIGIN);
        }
        for (double a : new double[]{a0, a1}) {
            addQuad(out, polar(base, rIn, a, y0), polar(base, rOut, a, y0), polar(base, rOut, a, y1), polar(base, rIn, a, y1), m, ORIGIN);
        }
    }

    /* A flat wooden panel whose grain is real geometry. The panel spans origin + u*along + w*across
     * (u, w in [0, 1]) and is cut into 'strips' long strips running with the grain:
     *   - the lines between strips wander side to side like grain lines do; the wander fades out
     *     at the panel's long edges so its outline stays straight and neighbours still tile
     *   - each strip is split into 'segments' along its length so those lines can curve
     *   - every piece is shaded on its own: light earlywood with a dark latewood band once per
     *     growth ring (latewood = how dark that band gets), the rings drifting across the strips
     *     along the length, plus a little per-strip jitter and a slow ripple ("figure")
     * wander is the grain lines' sideways amplitude and scale sets the size of every pattern
     * (both in world units per board square), so the look survives resizing.
     * -> 2 * strips * segments triangles. */
    private static void addWoodPanel(ArrayList<Triangle> out, Vec3 origin, Vec3 along, Vec3 across,
                                     int strips, int segments, Vec3 color, float latewood, float wander,
                                     float scale, float roughness, float ior, long seed) {
        java.util.Random rnd = new java.util.Random(seed);
        double ph1 = rnd.nextDouble(), ph2 = rnd.nextDouble(), ph3 = rnd.nextDouble(), ph4 = rnd.nextDouble();
        float ringWidth = (0.14f + 0.08f * rnd.nextFloat()) * scale; // distance between growth rings

        float lenU = (float) Math.sqrt(along.x * along.x + along.y * along.y + along.z * along.z);
        float lenW = (float) Math.sqrt(across.x * across.x + across.y * across.y + across.z * across.z);
        float amp = wander * scale / lenW; // wander in w units - kept well under a strip's width per unit w so lines never cross

        // grain line k at step j: w = k/strips pushed sideways by two beating sine waves along the length
        Vec3[][] grid = new Vec3[strips + 1][segments + 1];
        for (int k = 0; k <= strips; k++) {
            double w0 = (double) k / strips;
            for (int j = 0; j <= segments; j++) {
                double u = (double) j / segments, uWorld = u * lenU;
                double n = 0.75 * Math.sin(2 * Math.PI * (uWorld / (3.1 * scale) + ph1) + 2.1 * w0)
                         + 0.25 * Math.sin(2 * Math.PI * (uWorld / (1.3 * scale) + ph2) + 5.3 * w0);
                float w = (float) (w0 + amp * Math.sin(Math.PI * w0) * n);
                grid[k][j] = v(origin.x + along.x * (float) u + across.x * w,
                               origin.y + along.y * (float) u + across.y * w,
                               origin.z + along.z * (float) u + across.z * w);
            }
        }

        for (int k = 0; k < strips; k++) {
            float wc = (k + 0.5f) / strips;
            float jitter = 0.97f + 0.06f * rnd.nextFloat();

            for (int j = 0; j < segments; j++) {
                double uWorld = (j + 0.5) / segments * lenU;

                // where this piece sits within its growth ring; the rings drift slowly along the length
                double ring = wc * lenW / ringWidth + ph3 + 0.12 * Math.sin(2 * Math.PI * uWorld / (2.3 * scale) + ph1 * 6.28);
                float r = (float) (ring - Math.floor(ring));
                float tone = lerp(1f, latewood, Math.max(0f, Math.min(1f, (r - 0.55f) / 0.3f)));
                float figure = 1f + 0.03f * (float) Math.sin(2 * Math.PI * (uWorld / (0.9 * scale) + ph4) + 3 * wc);

                float shade = tone * jitter * figure;
                Vec3 c = v(Math.min(255f, color.x * shade), Math.min(255f, color.y * shade), Math.min(255f, color.z * shade));
                addQuad(out, grid[k][j], grid[k][j + 1], grid[k + 1][j + 1], grid[k + 1][j],
                        new Material(c, 0f, roughness, 0f, ior, 1f), ORIGIN);
            }
        }
    }

    // centre of a square on the board top; rank 0 is nearest the camera
    private static Vec3 chessSquare(int file, int rank, float sq, float boardY, float boardZ) {
        return v((file - 3.5f) * sq, boardY, boardZ + (3.5f - rank) * sq);
    }

    /* One Staunton piece standing at 'at'. Profiles are in square units (a square is 1 wide),
     * proportioned off a real tournament set: king ~1.66 squares tall with a base 0.6 across. */
    private static void addChessPiece(ArrayList<Triangle> out, ArrayList<Sphere> balls, char type, Vec3 at,
                                      float scale, int sides, Material m) {
        switch (type) {
            case 'K' -> {
                Profile p = stauntonBase(0.30f)
                        .curve(0.10f, 0.45f, 0.105f, 0.80f, 6)                                         // stem
                        .to(0.19f, 0.84f).arc(0.19f, 0.87f, 0.03f, -90f, 90f, 4).to(0.12f, 0.90f)   // collar
                        .to(0.16f, 0.93f).to(0.16f, 0.95f).to(0.11f, 0.97f)                           // second collar
                        .curve(0.12f, 1.12f, 0.20f, 1.22f, 6)                                          // flared body
                        .to(0.21f, 1.25f).to(0.18f, 1.27f)                                             // crown rim
                        .curve(0.16f, 1.36f, 0.06f, 1.38f, 4)                                          // crown
                        .to(0.05f, 1.40f).to(0f, 1.40f);
                addLathe(out, at, scale, p, sides, m);

                // the cross, facing the camera; it sinks into the crown so there's no seam
                addBox(out, v(at.x - 0.035f * scale, at.y + 1.37f * scale, at.z - 0.03f * scale),
                            v(at.x + 0.035f * scale, at.y + 1.66f * scale, at.z + 0.03f * scale), m, ORIGIN);
                addBox(out, v(at.x - 0.11f * scale, at.y + 1.50f * scale, at.z - 0.03f * scale),
                            v(at.x + 0.11f * scale, at.y + 1.56f * scale, at.z + 0.03f * scale), m, ORIGIN);
            }
            case 'Q' -> {
                Profile p = stauntonBase(0.29f)
                        .curve(0.09f, 0.45f, 0.095f, 0.75f, 6)
                        .to(0.18f, 0.78f).arc(0.18f, 0.81f, 0.03f, -90f, 90f, 4).to(0.11f, 0.84f)
                        .to(0.15f, 0.87f).to(0.15f, 0.89f).to(0.10f, 0.91f)
                        .curve(0.10f, 1.08f, 0.20f, 1.18f, 6)
                        .to(0.215f, 1.20f).to(0.215f, 1.24f).to(0.17f, 1.24f)                        // coronet band
                        .curve(0.16f, 1.32f, 0.06f, 1.34f, 4)
                        .to(0.035f, 1.36f).to(0f, 1.37f);                                              // closes inside the finial
                addLathe(out, at, scale, p, sides, m);

                for (int k = 0; k < 8; k++) { // coronet points, half-sunk into the band
                    Vec3 c = polar(at, 0.19f * scale, 2 * Math.PI * k / 8 + Math.PI / 8, 1.255f * scale);
                    balls.add(new Sphere(c, 0.03f * scale, m));
                }
                balls.add(new Sphere(v(at.x, at.y + 1.40f * scale, at.z), 0.055f * scale, m)); // finial
            }
            case 'B' -> {
                Profile p = stauntonBase(0.26f)
                        .curve(0.08f, 0.40f, 0.085f, 0.62f, 6)
                        .to(0.16f, 0.65f).arc(0.16f, 0.675f, 0.025f, -90f, 90f, 4).to(0.10f, 0.70f)
                        .to(0.13f, 0.72f).to(0.13f, 0.735f).to(0.07f, 0.75f)
                        .curve(0.17f, 0.85f, 0.13f, 1.00f, 6)                                          // mitre swells out
                        .curve(0.09f, 1.08f, 0.04f, 1.105f, 4)                                         // and closes up
                        .to(0f, 1.13f);                                                                // inside the finial
                addLathe(out, at, scale, p, sides, m);
                balls.add(new Sphere(v(at.x, at.y + 1.15f * scale, at.z), 0.05f * scale, m));
            }
            case 'R' -> {
                Profile p = stauntonBase(0.28f)
                        .curve(0.14f, 0.45f, 0.18f, 0.62f, 6)                                          // waisted tower
                        .to(0.21f, 0.64f).to(0.21f, 0.67f).to(0.18f, 0.68f)                           // collar
                        .to(0.19f, 0.70f).to(0.22f, 0.71f).to(0.22f, 0.84f)                           // turret wall
                        .to(0.16f, 0.84f).to(0.16f, 0.80f).to(0f, 0.80f);                             // rim + recessed top
                addLathe(out, at, scale, p, sides, m);

                // 6 merlons, one centred on the camera side (+z is 90 deg), sunk a hair into the rim
                for (int k = 0; k < 6; k++) {
                    double a0 = Math.toRadians(72 + 60 * k), a1 = Math.toRadians(108 + 60 * k);
                    addRingSegment(out, at, 0.165f * scale, 0.215f * scale, 0.835f * scale, 0.92f * scale, a0, a1, 4, m);
                }
            }
            case 'P' -> {
                Profile p = stauntonBase(0.22f)
                        .curve(0.07f, 0.30f, 0.075f, 0.45f, 5)
                        .to(0.14f, 0.48f).arc(0.14f, 0.50f, 0.02f, -90f, 90f, 4).to(0.07f, 0.52f)
                        .to(0.07f, 0.56f).to(0f, 0.56f);                                               // neck, closing inside the head
                addLathe(out, at, scale, p, sides, m);
                balls.add(new Sphere(v(at.x, at.y + 0.64f * scale, at.z), 0.12f * scale, m));
            }
            default -> throw new IllegalArgumentException("no chess piece '" + type + "'");
        }
    }


    // ============================ scene 4: the pocket watch ============================

    // p + d * s, as a new vector (Vec3's own methods work in place)
    private static Vec3 lin(Vec3 p, Vec3 d, float s) { return v(p.x + d.x * s, p.y + d.y * s, p.z + d.z * s); }

    private static Vec3 cross3(Vec3 a, Vec3 b) { return v(a.y * b.z - a.z * b.y, a.z * b.x - a.x * b.z, a.x * b.y - a.y * b.x); }

    private static Vec3 unit(Vec3 a) {
        float l = (float) Math.sqrt(a.x * a.x + a.y * a.y + a.z * a.z);
        return v(a.x / l, a.y / l, a.z / l);
    }

    // a point on the watch set (table at y = 0) -> kernel camera space: relative to the eye, pitched, scaled, offset
    // off is in the scene's own units, along the camera's axes (see loadScene), so it's added before scaling
    private static Vec3 toView(Vec3 p, Vec3 eye, float pitch, float scale, Vec3 off) {
        Vec3 q = spin(v(p.x - eye.x, p.y - eye.y, p.z - eye.z), pitch, 0f);
        return v((q.x + off.x) * scale, (q.y + off.y) * scale, (q.z + off.z) * scale);
    }

    private static float smooth(float e0, float e1, float x) {
        float t = Math.max(0f, Math.min(1f, (x - e0) / (e1 - e0)));
        return t * t * (3 - 2 * t);
    }

    // cheap repeatable 0..1 noise for a pair of integers
    private static float hash01(int a, int b) {
        double s = Math.sin(a * 12.9898 + b * 78.233) * 43758.5453;
        return (float) (s - Math.floor(s));
    }

    // smooth 0..1 noise: random values on an integer lattice, eased between (value noise)
    private static float valueNoise(float x, float y, int seed) {
        int ix = (int) Math.floor(x), iy = (int) Math.floor(y);
        float fx = smooth(0f, 1f, x - ix), fy = smooth(0f, 1f, y - iy);
        float a = hash01(ix + seed, iy), b = hash01(ix + 1 + seed, iy);
        float c = hash01(ix + seed, iy + 1), d = hash01(ix + 1 + seed, iy + 1);
        return lerp(lerp(a, b, fx), lerp(c, d, fx), fy);
    }

    // column positions from x0 to x1: cells are minCell wide at x = 0 and widen by 'growth' per unit of |x|
    private static float[] graded(float x0, float x1, float minCell, float growth) {
        ArrayList<Float> xs = new ArrayList<>();
        for (float x = x0; ; x = Math.min(x1, x + minCell * (1 + growth * Math.abs(x)))) {
            xs.add(x);
            if (x >= x1) break;
        }
        float[] out = new float[xs.size()];
        for (int i = 0; i < out.length; i++) out[i] = xs.get(i);
        return out;
    }

    /* A flat-sawn plank lying at height y along xs (grain along x) and across z0..z1, coloured by a
     * simulated log. Each plank gets its own log: the board is a slice some depth from the pith,
     * and the log's axis tilts, bows and sweeps sideways relative to the slice. Colour then comes
     * from the growth ring at each spot's distance from the pith, so the rings cut the surface as
     * straight grain where the slice is far from the pith and as nested cathedral arches where it
     * passes close - the way real flat-sawn boards look. On top of that:
     *   - rings vary in width, and each ring goes gradually from light earlywood to a dense dark
     *     latewood band, then snaps back (the way trees actually lay wood down)
     *   - the rings wobble, with slow blotches of colour across the board
     *   - streaky fibres run along the grain in random lengths
     *   - some planks get a knot: a dark core in its own tight rings, the main grain bunched
     *     around it
     * The top face is a grid - columns at xs (see graded), cellsAcross rows - each cell shaded on
     * its own -> 2 * (xs.length - 1) * cellsAcross triangles. */
    private static void addPlank(ArrayList<Triangle> out, float[] xs, float z0, float z1, float y,
                                 int cellsAcross, Vec3 color, float roughness, float ior, long seed) {
        java.util.Random rnd = new java.util.Random(seed);
        int cellsAlong = xs.length - 1;
        float x0 = xs[0], len = xs[cellsAlong] - x0, width = Math.abs(z1 - z0);

        // the log
        float ringWidth = 0.07f + 0.05f * rnd.nextFloat();
        float pith = width * (0.2f + 0.6f * rnd.nextFloat());          // across the board, where the pith runs
        if (rnd.nextFloat() < 0.35f) pith += (rnd.nextBoolean() ? 1 : -1) * width * (0.8f + 0.7f * rnd.nextFloat()); // pith off the board: straighter grain
        float depth = 0.12f + 0.5f * rnd.nextFloat();                   // board face to pith
        float tilt = (rnd.nextFloat() - 0.5f) * 0.10f;                  // log axis vs board, per unit length
        float bowAmp = 0.05f + 0.08f * rnd.nextFloat(), bowLen = 5f + 5f * rnd.nextFloat();
        float sweepAmp = 0.06f + 0.10f * rnd.nextFloat(), sweepLen = 7f + 6f * rnd.nextFloat();
        double[] ph = new double[3];
        for (int i = 0; i < ph.length; i++) ph[i] = rnd.nextDouble() * 2 * Math.PI;

        // this plank's own tint - boards from different trees never quite match
        float tr = 0.88f + 0.22f * rnd.nextFloat(), tg = tr * (0.96f + 0.08f * rnd.nextFloat()), tb = tr * (0.94f + 0.12f * rnd.nextFloat());

        boolean knot = rnd.nextFloat() < 0.45f;
        float ku = len * (0.1f + 0.8f * rnd.nextFloat()), kw = width * (0.15f + 0.7f * rnd.nextFloat());
        float kSize = 0.05f + 0.07f * rnd.nextFloat();

        // fibre streaks: per row, runs of random length along the grain
        float[] rowOffset = new float[cellsAcross];
        for (int j = 0; j < cellsAcross; j++) rowOffset[j] = rnd.nextFloat() * 10f;

        float dir = Math.signum(z1 - z0);
        Vec3[][] grid = new Vec3[cellsAlong + 1][cellsAcross + 1];
        for (int i = 0; i <= cellsAlong; i++) {
            for (int j = 0; j <= cellsAcross; j++) {
                grid[i][j] = v(xs[i], y, z0 + dir * width * j / cellsAcross);
            }
        }

        for (int i = 0; i < cellsAlong; i++) {
            float u = (xs[i] + xs[i + 1]) / 2 - x0;
            // where the pith is, relative to this slice, at this point along the board
            float d = Math.abs(depth + tilt * (u - len / 2) + bowAmp * (float) Math.sin(2 * Math.PI * u / bowLen + ph[0])) + 0.02f;
            float pw = pith + sweepAmp * (float) Math.sin(2 * Math.PI * u / sweepLen + ph[1]);

            for (int j = 0; j < cellsAcross; j++) {
                float wv = width * (j + 0.5f) / cellsAcross;

                float r = (float) Math.sqrt((wv - pw) * (wv - pw) + d * d);
                // rings meander: two octaves of smooth random noise, long along the grain, short across it
                int ns = (int) (seed % 1000) * 37;
                r += ringWidth * 0.5f * (valueNoise(u / 1.6f, wv / 0.7f, ns) + 0.5f * valueNoise(u / 0.55f, wv / 0.3f, ns + 11) - 0.75f);

                float knotPull = 0f, knotW = 0f, knotTone = 1f;
                if (knot) {
                    float dk = (float) Math.hypot((u - ku) * 0.55f, wv - kw); // knots stretch a little along the grain
                    knotPull = 1.6f * (float) Math.exp(-(dk / (3 * kSize)) * (dk / (3 * kSize)));
                    knotW = (float) Math.exp(-(dk / kSize) * (dk / kSize));
                    float kt = dk / 0.018f; kt -= (float) Math.floor(kt);
                    knotTone = dk < 0.35f * kSize ? 0.30f : 0.50f + 0.25f * smooth(0.2f, 0.8f, kt);
                }

                float phase = r / ringWidth + 0.35f * (float) Math.sin(0.37 * r / ringWidth + ph[2]) + knotPull; // uneven rings
                float t = phase - (float) Math.floor(phase);
                float late = smooth(0.45f, 0.88f, t) * (1f - smooth(0.93f, 1f, t)); // gradual in, sharp out
                float tone = 1f - 0.40f * late;
                tone = tone * (1 - knotW) + knotTone * knotW;

                float blotch = 1f + 0.14f * (valueNoise(u / 3f, wv / 1.2f, ns + 23) - 0.5f);
                float q = u / 0.6f + rowOffset[j], qf = q - (float) Math.floor(q); // streak value, eased between random knots
                float streak = 1f + 0.06f * (lerp(hash01(j + (int) seed * 131, (int) Math.floor(q)),
                                                  hash01(j + (int) seed * 131, (int) Math.floor(q) + 1), smooth(0f, 1f, qf)) - 0.5f);

                float s = tone * blotch * streak;
                Vec3 c = v(Math.min(255f, color.x * s * tr), Math.min(255f, color.y * s * tg), Math.min(255f, color.z * s * tb));
                addQuad(out, grid[i][j], grid[i + 1][j], grid[i + 1][j + 1], grid[i][j + 1],
                        new Material(c, 0f, roughness, 0f, ior, 1f), ORIGIN);
            }
        }
    }

    /* addLathe around any axis: profile radius is measured from the axis and profile height runs
     * along it from base. ridges > 0 flutes the surface like a crown's knurling - the radius swells
     * by up to ridgeDepth, ridges times round (sides should be a multiple of ridges). */
    private static void addAxialLathe(ArrayList<Triangle> out, Vec3 base, Vec3 axis, Profile profile, int sides,
                                      int ridges, float ridgeDepth, Material m) {
        Vec3 ax = unit(axis);
        Vec3 e1 = Math.abs(ax.y) > 0.99f ? v(1f, 0f, 0f) : unit(cross3(ax, v(0f, 1f, 0f)));
        Vec3 e2 = cross3(ax, e1);

        int n = profile.pts.size();
        Vec3[][] rings = new Vec3[n][];
        for (int k = 0; k < n; k++) {
            float r = profile.pts.get(k)[0], h = profile.pts.get(k)[1];
            Vec3 c = lin(base, ax, h);
            if (r == 0f) {
                rings[k] = new Vec3[]{c};
                continue;
            }
            rings[k] = new Vec3[sides];
            for (int i = 0; i < sides; i++) {
                double a = 2 * Math.PI * i / sides;
                float rr = r + (ridges > 0 ? ridgeDepth * (0.5f + 0.5f * (float) Math.cos(ridges * a)) : 0f);
                rings[k][i] = lin(lin(c, e1, rr * (float) Math.cos(a)), e2, rr * (float) Math.sin(a));
            }
        }

        for (int k = 0; k + 1 < n; k++) {
            Vec3[] a = rings[k], bb = rings[k + 1];
            if (a.length == 1 && bb.length == 1) continue;
            for (int i = 0; i < sides; i++) {
                int i2 = (i + 1) % sides;
                if (a.length == 1)       out.add(new Triangle(a[0], bb[i], bb[i2], m));
                else if (bb.length == 1) out.add(new Triangle(a[i], a[i2], bb[0], m));
                else                     addQuad(out, a[i], a[i2], bb[i2], bb[i], m, ORIGIN);
            }
        }
    }

    /* Round wire bent into a closed stadium - two half circles of radius bend joined by straights -
     * centred on c, long axis along a, in the plane of a and b. straight = 0 gives a plain ring.
     * Chain links and the bow. -> 4 * bendSteps * sides triangles. */
    private static void addWireLoop(ArrayList<Triangle> out, Vec3 c, Vec3 a, Vec3 b, float straight, float bend,
                                    float wire, int bendSteps, int sides, Material m) {
        Vec3 n = unit(cross3(a, b));
        int count = 2 * bendSteps;
        Vec3[] centre = new Vec3[count];
        for (int i = 0; i < bendSteps; i++) { // far half circle, then the near one; the straights join them
            double t0 = -Math.PI / 2 + Math.PI * i / bendSteps, t1 = Math.PI / 2 + Math.PI * i / bendSteps;
            centre[i]             = lin(lin(c, a,  straight / 2 + bend * (float) Math.cos(t0)), b, bend * (float) Math.sin(t0));
            centre[i + bendSteps] = lin(lin(c, a, -straight / 2 + bend * (float) Math.cos(t1)), b, bend * (float) Math.sin(t1));
        }

        Vec3[][] rings = new Vec3[count][sides];
        for (int i = 0; i < count; i++) {
            Vec3 prev = centre[(i + count - 1) % count], next = centre[(i + 1) % count];
            Vec3 tangent = unit(v(next.x - prev.x, next.y - prev.y, next.z - prev.z));
            Vec3 side = cross3(tangent, n);
            for (int j = 0; j < sides; j++) {
                double phi = 2 * Math.PI * j / sides;
                rings[i][j] = lin(lin(centre[i], side, wire * (float) Math.cos(phi)), n, wire * (float) Math.sin(phi));
            }
        }
        for (int i = 0; i < count; i++) {
            int i2 = (i + 1) % count;
            for (int j = 0; j < sides; j++) {
                int j2 = (j + 1) % sides;
                addQuad(out, rings[i][j], rings[i2][j], rings[i2][j2], rings[i][j2], m, ORIGIN);
            }
        }
    }

    /* A curb chain along a curve through (x, y, z) control points (Catmull-Rom, never dipping below
     * the table at y = 0), so it can hang from something and then lie along the table. Links
     * alternate "flat" and "upright" - their planes a quarter turn apart about the chain - one pitch
     * apart, the pitch being a link's inside length so each one threads through its neighbours.
     * Where the chain lies on the table each link is lifted to rest on it (flat ones by the wire,
     * upright ones by their half-width); where it hangs straight down that lift fades to nothing. */
    private static void addChain(ArrayList<Triangle> out, float[][] ctrl, float wire, float bend, float straight,
                                 int bendSteps, int sides, Material m) {
        // dense samples of the curve, then walk it one pitch at a time
        ArrayList<float[]> pts = new ArrayList<>();
        for (int s = 0; s + 1 < ctrl.length; s++) {
            float[] p0 = ctrl[Math.max(0, s - 1)], p1 = ctrl[s], p2 = ctrl[s + 1], p3 = ctrl[Math.min(ctrl.length - 1, s + 2)];
            for (int i = 0; i < 200; i++) {
                float t = i / 200f, t2 = t * t, t3 = t2 * t;
                float[] p = new float[3];
                for (int k = 0; k < 3; k++) {
                    p[k] = 0.5f * (2 * p1[k] + (-p0[k] + p2[k]) * t + (2 * p0[k] - 5 * p1[k] + 4 * p2[k] - p3[k]) * t2
                                   + (-p0[k] + 3 * p1[k] - 3 * p2[k] + p3[k]) * t3);
                }
                p[1] = Math.max(0f, p[1]); // the spline overshoots a little where it lands - not through the table
                pts.add(p);
            }
        }
        pts.add(ctrl[ctrl.length - 1]);

        Vec3 up = v(0f, 1f, 0f);
        float pitch = straight + 2 * bend - 2 * wire;
        float travelled = 0f, nextLink = 0f;
        int link = 0;
        for (int i = 1; i < pts.size(); i++) {
            float[] a = pts.get(i - 1), bb = pts.get(i);
            float seg = (float) Math.sqrt((bb[0] - a[0]) * (bb[0] - a[0]) + (bb[1] - a[1]) * (bb[1] - a[1]) + (bb[2] - a[2]) * (bb[2] - a[2]));
            while (seg > 0 && nextLink <= travelled + seg) {
                float f = (nextLink - travelled) / seg;
                Vec3 p = v(a[0] + (bb[0] - a[0]) * f, a[1] + (bb[1] - a[1]) * f, a[2] + (bb[2] - a[2]) * f);
                Vec3 along = v((bb[0] - a[0]) / seg, (bb[1] - a[1]) / seg, (bb[2] - a[2]) / seg);

                Vec3 flatSide = Math.abs(along.y) > 0.999f ? v(1f, 0f, 0f) : unit(cross3(up, along)); // level, square to the chain
                boolean upright = link % 2 == 1;
                Vec3 across = upright ? cross3(along, flatSide) : flatSide;

                // rest on the table: push along the part of "up" that's square to the chain
                float rest = upright ? bend + wire : wire;
                Vec3 lift = v(-along.x * along.y, 1f - along.y * along.y, -along.z * along.y);
                Vec3 centre = lin(p, lift, rest);
                // a link tilted where the chain lands reaches lower than that - keep all of it above the table
                float reach = Math.abs(along.y) * (straight / 2 + bend) + Math.abs(across.y) * bend + wire;
                if (centre.y < reach) centre.y = reach;
                addWireLoop(out, centre, along, across, straight, bend, wire, bendSteps, sides, m);
                link++;
                nextLink += pitch;
            }
            travelled += seg;
        }
    }

    /* The main plate's top face, finished with perlage: rows of little overlapping discs of circular
     * graining, each laid over the last. The face is a fine polar grid and every cell takes the
     * shade of whichever disc is on top at its centre - darker in the middle, bright at the rim. */
    private static void addPerlagePlate(ArrayList<Triangle> out, float radius, float y, int rings, int sectors,
                                        Vec3 color, float roughness) {
        Vec3[][] grid = new Vec3[rings + 1][sectors];
        for (int k = 1; k <= rings; k++) {
            float r = radius * k / rings;
            for (int i = 0; i < sectors; i++) {
                double a = 2 * Math.PI * i / sectors;
                grid[k][i] = v(r * (float) Math.sin(a), y, -r * (float) Math.cos(a));
            }
        }
        Vec3 centre = v(0f, y, 0f);

        for (int k = 0; k < rings; k++) {
            float rc = radius * (k + 0.5f) / rings;
            for (int i = 0; i < sectors; i++) {
                int i2 = (i + 1) % sectors;
                double ac = 2 * Math.PI * (i + 0.5) / sectors;
                float shade = perlage(rc * (float) Math.sin(ac), -rc * (float) Math.cos(ac));
                Material m = new Material(v(color.x * shade, color.y * shade, color.z * shade), 1f, roughness, 0f);
                if (k == 0) out.add(new Triangle(centre, grid[1][i], grid[1][i2], m));
                else        addQuad(out, grid[k][i], grid[k][i2], grid[k + 1][i2], grid[k + 1][i], m, ORIGIN);
            }
        }
    }

    // shade of the perlage at (x, z): the last-drawn disc covering the point wins, rows drawn in order
    private static float perlage(float x, float z) {
        final float SPACING = 0.07f, SPOT = 0.065f; // spots ~6 grid cells across, so they read as circles
        int gi = Math.round(x / SPACING), gj = Math.round(z / SPACING);
        long best = Long.MIN_VALUE;
        float bestD = 0f;
        for (int dj = -1; dj <= 1; dj++) {
            for (int di = -1; di <= 1; di++) {
                float d = (float) Math.hypot(x - (gi + di) * SPACING, z - (gj + dj) * SPACING);
                long order = (long) (gj + dj) * 100000L + (gi + di);
                if (d < SPOT && order > best) {
                    best = order;
                    bestD = d;
                }
            }
        }
        return best == Long.MIN_VALUE ? 1f : 0.80f + 0.22f * (float) Math.pow(bestD / SPOT, 1.5);
    }

    /* A flat ring between rIn and rOut, cut between y0 and y1: top, bottom and both walls.
     * rIn = 0 gives a solid disc (fanned from the centre). */
    private static void addRing(ArrayList<Triangle> out, float cx, float cz, float rIn, float rOut, float y0, float y1,
                                int segs, Material m) {
        Vec3 top = v(cx, y1, cz), bottom = v(cx, y0, cz);
        for (int i = 0; i < segs; i++) {
            double a0 = 2 * Math.PI * i / segs, a1 = 2 * Math.PI * (i + 1) / segs;
            Vec3 o0t = polar(top, rOut, a0, 0f), o1t = polar(top, rOut, a1, 0f);
            Vec3 o0b = polar(bottom, rOut, a0, 0f), o1b = polar(bottom, rOut, a1, 0f);
            addQuad(out, o0b, o1b, o1t, o0t, m, ORIGIN); // outer wall
            if (rIn == 0f) {
                out.add(new Triangle(top, o0t, o1t, m));
                out.add(new Triangle(bottom, o0b, o1b, m));
            } else {
                Vec3 i0t = polar(top, rIn, a0, 0f), i1t = polar(top, rIn, a1, 0f);
                Vec3 i0b = polar(bottom, rIn, a0, 0f), i1b = polar(bottom, rIn, a1, 0f);
                addQuad(out, i0t, i1t, o1t, o0t, m, ORIGIN); // top
                addQuad(out, i0b, i1b, o1b, o0b, m, ORIGIN); // bottom
                addQuad(out, i0b, i1b, i1t, i0t, m, ORIGIN); // inner wall
            }
        }
    }

    private static void addDisc(ArrayList<Triangle> out, float cx, float cz, float r, float y0, float y1, int segs, Material m) {
        addRing(out, cx, cz, 0f, r, y0, y1, segs, m);
    }

    /* A flat strip following a path in xz, with a width at every point, cut between y0 and y1:
     * top, bottom, both side walls and square ends. Bridges, spokes, hands, numerals, ticks and
     * the hairspring are all ribbons. */
    private static void addRibbon(ArrayList<Triangle> out, float[][] path, float[] widths, float y0, float y1, Material m) {
        int n = path.length;
        Vec3[] lt = new Vec3[n], rt = new Vec3[n], lb = new Vec3[n], rb = new Vec3[n];
        for (int i = 0; i < n; i++) {
            float[] a = path[Math.max(0, i - 1)], bb = path[Math.min(n - 1, i + 1)];
            float tx = bb[0] - a[0], tz = bb[1] - a[1], len = (float) Math.hypot(tx, tz);
            float nx = -tz / len * widths[i] / 2, nz = tx / len * widths[i] / 2; // half-width, sideways
            lt[i] = v(path[i][0] + nx, y1, path[i][1] + nz);
            rt[i] = v(path[i][0] - nx, y1, path[i][1] - nz);
            lb[i] = v(path[i][0] + nx, y0, path[i][1] + nz);
            rb[i] = v(path[i][0] - nx, y0, path[i][1] - nz);
        }
        for (int i = 0; i + 1 < n; i++) {
            addQuad(out, lt[i], lt[i + 1], rt[i + 1], rt[i], m, ORIGIN); // top
            addQuad(out, lb[i], lb[i + 1], rb[i + 1], rb[i], m, ORIGIN); // bottom
            addQuad(out, lb[i], lb[i + 1], lt[i + 1], lt[i], m, ORIGIN); // left wall
            addQuad(out, rb[i], rb[i + 1], rt[i + 1], rt[i], m, ORIGIN); // right wall
        }
        addQuad(out, lb[0], rb[0], rt[0], lt[0], m, ORIGIN);                 // start
        addQuad(out, lb[n - 1], rb[n - 1], rt[n - 1], lt[n - 1], m, ORIGIN); // end
    }

    private static void addRibbon(ArrayList<Triangle> out, float[][] path, float width, float y0, float y1, Material m) {
        float[] widths = new float[path.length];
        java.util.Arrays.fill(widths, width);
        addRibbon(out, path, widths, y0, y1, m);
    }

    /* A wheel's toothed rim cut flat between y0 and y1, from rIn out to the teeth. style: 0 = train
     * tooth (ogival tip), 1 = ratchet (sawtooth), 2 = escape wheel club tooth. Each tooth is a
     * (fraction of the tooth pitch, height from root to tip) outline, so walls and faces follow
     * every flank. -> 4 * teeth * outline points triangles. */
    private static void addToothedRing(ArrayList<Triangle> out, float cx, float cz, float rRoot, float rTip, int teeth,
                                       float rIn, float y0, float y1, int style, Material m) {
        float[][] tooth = switch (style) {
            case 1  -> new float[][]{{0f, 0f}, {0.86f, 1f}, {0.90f, 0f}};
            case 2  -> new float[][]{{0f, 0f}, {0.45f, 0f}, {0.62f, 1f}, {0.76f, 1f}, {0.80f, 0.3f}, {0.84f, 0f}};
            default -> new float[][]{{0f, 0f}, {0.18f, 0f}, {0.30f, 0.55f}, {0.38f, 0.92f}, {0.46f, 1f},
                                     {0.54f, 1f}, {0.62f, 0.92f}, {0.70f, 0.55f}, {0.82f, 0f}};
        };
        int n = teeth * tooth.length;
        Vec3[] ot = new Vec3[n], ob = new Vec3[n], it = new Vec3[n], ib = new Vec3[n];
        Vec3 top = v(cx, y1, cz), bottom = v(cx, y0, cz);
        for (int k = 0; k < teeth; k++) {
            for (int j = 0; j < tooth.length; j++) {
                int idx = k * tooth.length + j;
                double a = 2 * Math.PI * (k + tooth[j][0]) / teeth;
                float r = rRoot + (rTip - rRoot) * tooth[j][1];
                ot[idx] = polar(top, r, a, 0f);
                ob[idx] = polar(bottom, r, a, 0f);
                it[idx] = polar(top, rIn, a, 0f);
                ib[idx] = polar(bottom, rIn, a, 0f);
            }
        }
        for (int i = 0; i < n; i++) {
            int i2 = (i + 1) % n;
            addQuad(out, it[i], it[i2], ot[i2], ot[i], m, ORIGIN); // top
            addQuad(out, ib[i], ib[i2], ob[i2], ob[i], m, ORIGIN); // bottom
            addQuad(out, ob[i], ob[i2], ot[i2], ot[i], m, ORIGIN); // tooth flanks
            addQuad(out, ib[i], ib[i2], it[i2], it[i], m, ORIGIN); // inside of the rim
        }
    }

    /* A train wheel: toothed rim, hub and curved crossings - or, with spokes = 0, a solid pinion or
     * drum on its arbor. Tooth height follows the tooth pitch, so fine and coarse wheels both
     * look properly cut. */
    private static void addWheel(ArrayList<Triangle> out, float cx, float cz, float rTip, int teeth, int spokes,
                                 float y0, float y1, int style, Material m) {
        float pitch = (float) (2 * Math.PI * rTip / teeth);
        float rRoot = rTip - Math.min(0.75f * pitch, 0.35f * rTip);
        if (spokes == 0) {
            addToothedRing(out, cx, cz, rRoot, rTip, teeth, Math.min(0.012f, 0.5f * rRoot), y0, y1, style, m);
            return;
        }

        float rim = Math.max(0.018f, 0.10f * rTip), rIn = rRoot - rim;
        float hub = Math.max(0.025f, 0.16f * rTip);
        addToothedRing(out, cx, cz, rRoot, rTip, teeth, rIn, y0, y1, style, m);
        addRing(out, cx, cz, 0.011f, hub, y0, y1, 24, m);

        float width = Math.max(0.012f, 0.09f * rTip);
        for (int s = 0; s < spokes; s++) {
            double a = 2 * Math.PI * s / spokes;
            float[][] path = new float[11][];
            for (int i = 0; i <= 10; i++) {
                float r = lerp(0.7f * hub, rIn + 0.4f * rim, i / 10f); // sunk into hub and rim
                double t = a + 0.45 * i / 10;                            // swept crossing
                path[i] = new float[]{cx + r * (float) Math.sin(t), cz - r * (float) Math.cos(t)};
            }
            addRibbon(out, path, width, y0, y1, m);
        }
    }

    /* A flat hairspring: an Archimedean spiral of ribbon from r0 out to r1, stepped every ds along
     * the strip so inner and outer coils are equally smooth. */
    private static void addHairspring(ArrayList<Triangle> out, float cx, float cz, float r0, float r1, int turns,
                                      float ds, float thickness, float y0, float y1, Material m) {
        double total = 2 * Math.PI * turns, grow = (r1 - r0) / total;
        ArrayList<float[]> path = new ArrayList<>();
        for (double th = 0; th <= total; ) {
            float r = (float) (r0 + grow * th);
            path.add(new float[]{cx + r * (float) Math.sin(th), cz - r * (float) Math.cos(th)});
            th += ds / r;
        }
        addRibbon(out, path.toArray(new float[0][]), thickness, y0, y1, m);
    }

    /* Roman numeral text on the chapter ring, drawn as strokes: I is one upright, V and X two
     * diagonals. Glyphs are laid out in a box 1 tall, centred, then wrapped onto the dial with
     * their feet towards the centre - the box's up runs out along the radius at 'angle'
     * (clockwise from 12), its right runs clockwise. */
    private static void addNumeral(ArrayList<Triangle> out, String text, double angle, float rBase, float height,
                                   float stroke, float y0, float y1, Material m) {
        final float GAP = 0.22f, WIDE = 0.62f;
        float total = GAP * (text.length() - 1);
        for (char ch : text.toCharArray()) total += ch == 'I' ? 0f : WIDE;

        float dx = (float) Math.sin(angle), dz = (float) -Math.cos(angle); // outward
        float tx = (float) Math.cos(angle), tz = (float) Math.sin(angle);  // clockwise
        float u = -total / 2;
        for (char ch : text.toCharArray()) {
            float[][] strokes = switch (ch) {
                case 'V' -> new float[][]{{0f, 1f, WIDE / 2, 0f}, {WIDE / 2, 0f, WIDE, 1f}};
                case 'X' -> new float[][]{{0f, 0f, WIDE, 1f}, {0f, 1f, WIDE, 0f}};
                default  -> new float[][]{{0f, 0f, 0f, 1f}};
            };
            for (float[] s : strokes) {
                float[][] path = new float[2][];
                for (int e = 0; e < 2; e++) {
                    float gu = (u + s[2 * e]) * height, gv = rBase + s[2 * e + 1] * height;
                    path[e] = new float[]{gv * dx + gu * tx, gv * dz + gu * tz};
                }
                addRibbon(out, path, stroke, y0, y1, m);
            }
            u += (ch == 'I' ? 0f : WIDE) + GAP;
        }
    }

    // a watch hand from the centre at 'angle' (clockwise from 12): {distance along the hand, width} pairs
    private static void addHand(ArrayList<Triangle> out, double angle, float[][] shape, float y0, float y1, Material m) {
        float dx = (float) Math.sin(angle), dz = (float) -Math.cos(angle);
        float[][] path = new float[shape.length][];
        float[] widths = new float[shape.length];
        for (int i = 0; i < shape.length; i++) {
            path[i] = new float[]{dx * shape[i][0], dz * shape[i][0]};
            widths[i] = shape[i][1];
        }
        addRibbon(out, path, widths, y0, y1, m);
    }


    // ============================ scene 5: the glass ============================

    private static Vec3 randomUnit(java.util.Random rnd) {
        while (true) {
            Vec3 p = v(rnd.nextFloat() * 2 - 1, rnd.nextFloat() * 2 - 1, rnd.nextFloat() * 2 - 1);
            float l2 = p.x * p.x + p.y * p.y + p.z * p.z;
            if (l2 > 0.01f && l2 <= 1f) return unit(p);
        }
    }

    /* The centre line and radius of one swept glass piece (a tendril or a reed), kept so later pieces
     * can be tested against it before they're placed - glass must never touch other glass here. */
    private static class GlassPiece {
        static final float MARGIN = 0.01f; // closest two pieces may come

        Vec3[] centres;
        float[] radii;
        float inflate;          // flutes stick out this much further (fraction of radius)
        float cx, cy, cz, bound; // bounding sphere, for a quick first test

        /* Sweeps a tapering piece out from start along dir. It bends towards 'side' (turned about
         * the centre line by 'helix' rad per unit, so bends become spirals) with a curvature of
         * curl * t^2 - gentle at the base, hooking over at the tip - plus sBend * sin(2 pi t) for a
         * lazy S. Radius runs from baseR down to baseR * (1 - taper) at the tip. */
        static GlassPiece sweep(Vec3 start, Vec3 dir, Vec3 hint, float length, float baseR, float taper,
                                float curl, float helix, float sBend, int steps, float inflate) {
            GlassPiece g = new GlassPiece();
            g.centres = new Vec3[steps + 1];
            g.radii = new float[steps + 1];
            g.inflate = inflate;

            Vec3 d = unit(dir), side = cross3(d, hint);
            side = side.x * side.x + side.y * side.y + side.z * side.z < 1e-6f ? unit(cross3(d, v(0.3f, 1f, 0.2f))) : unit(side);
            Vec3 p = v(start.x, start.y, start.z);
            float ds = length / steps;
            for (int k = 0; k <= steps; k++) {
                float t = (float) k / steps;
                g.centres[k] = p;
                g.radii[k] = baseR * (float) Math.pow(1 - taper * t, 1.1);

                float bend = (curl * t * t + sBend * (float) Math.sin(2 * Math.PI * t)) * ds;
                d = unit(lin(d, side, bend));
                side = unit(lin(side, d, -(side.x * d.x + side.y * d.y + side.z * d.z)));   // keep it square to d
                Vec3 across = cross3(d, side);                                               // then turn it about d
                side = unit(lin(v(side.x * (float) Math.cos(helix * ds), side.y * (float) Math.cos(helix * ds), side.z * (float) Math.cos(helix * ds)),
                                across, (float) Math.sin(helix * ds)));
                p = lin(p, d, ds);
            }

            float[] mn = {Float.MAX_VALUE, Float.MAX_VALUE, Float.MAX_VALUE}, mx = {-Float.MAX_VALUE, -Float.MAX_VALUE, -Float.MAX_VALUE};
            for (int k = 0; k <= steps; k++) {
                Vec3 c = g.centres[k];
                float r = g.radii[k] * (1 + inflate);
                mn[0] = Math.min(mn[0], c.x - r); mn[1] = Math.min(mn[1], c.y - r); mn[2] = Math.min(mn[2], c.z - r);
                mx[0] = Math.max(mx[0], c.x + r); mx[1] = Math.max(mx[1], c.y + r); mx[2] = Math.max(mx[2], c.z + r);
            }
            g.cx = (mn[0] + mx[0]) / 2; g.cy = (mn[1] + mx[1]) / 2; g.cz = (mn[2] + mx[2]) / 2;
            g.bound = 0.5f * (float) Math.sqrt((mx[0] - mn[0]) * (mx[0] - mn[0]) + (mx[1] - mn[1]) * (mx[1] - mn[1]) + (mx[2] - mn[2]) * (mx[2] - mn[2]));
            return g;
        }

        boolean hits(GlassPiece o) {
            float dx = cx - o.cx, dy = cy - o.cy, dz = cz - o.cz, reach = bound + o.bound + MARGIN;
            if (dx * dx + dy * dy + dz * dz > reach * reach) return false;
            for (int i = 0; i < centres.length; i++) {
                Vec3 a = centres[i];
                float ra = radii[i] * (1 + inflate);
                for (int j = 0; j < o.centres.length; j++) {
                    Vec3 c = o.centres[j];
                    float gap = ra + o.radii[j] * (1 + o.inflate) + MARGIN;
                    float ex = a.x - c.x, ey = a.y - c.y, ez = a.z - c.z;
                    if (ex * ex + ey * ey + ez * ez < gap * gap) return true;
                }
            }
            return false;
        }

        boolean hitsAny(ArrayList<GlassPiece> others) {
            for (GlassPiece o : others) if (hits(o)) return true;
            return false;
        }

        // any part lower than y
        boolean below(float y) {
            for (int i = 0; i < centres.length; i++) if (centres[i].y - radii[i] * (1 + inflate) < y) return true;
            return false;
        }

        // an axis-aligned box, taken 'above' higher than it is (room for what stands on it)
        boolean hitsBox(float[] mn, float[] mx, float above) {
            for (int i = 0; i < centres.length; i++) {
                Vec3 c = centres[i];
                float r = radii[i] * (1 + inflate) + MARGIN;
                float dx = Math.max(0f, Math.max(mn[0] - c.x, c.x - mx[0]));
                float dy = Math.max(0f, Math.max(mn[1] - c.y, c.y - (mx[1] + above)));
                float dz = Math.max(0f, Math.max(mn[2] - c.z, c.z - mx[2]));
                if (dx * dx + dy * dy + dz * dz < r * r) return true;
            }
            return false;
        }

        // any glass sphere in the list (opaque ones, like the sun's core, are the pieces' anchors)
        boolean hitsFloats(ArrayList<Sphere> spheres) {
            for (Sphere s : spheres) {
                if (s.material.transmission <= 0f) continue;
                for (int i = 0; i < centres.length; i++) {
                    Vec3 c = centres[i];
                    float gap = s.radius + radii[i] * (1 + inflate) + MARGIN;
                    float dx = c.x - s.pos.x, dy = c.y - s.pos.y, dz = c.z - s.pos.z;
                    if (dx * dx + dy * dy + dz * dz < gap * gap) return true;
                }
            }
            return false;
        }

        // a vertical rod at (rx, rz) running up from y = fromY
        boolean hitsRod(float rx, float rz, float fromY, float rodR) {
            for (int i = 0; i < centres.length; i++) {
                Vec3 c = centres[i];
                float gap = rodR + radii[i] * (1 + inflate) + MARGIN;
                if (c.y + gap > fromY && (c.x - rx) * (c.x - rx) + (c.z - rz) * (c.z - rz) < gap * gap) return true;
            }
            return false;
        }
    }

    // joins a sequence of rings (a one-point "ring" is a pole) into one surface, like addLathe does
    private static void connectRings(ArrayList<Triangle> out, ArrayList<Vec3[]> seq, Material m) {
        for (int k = 0; k + 1 < seq.size(); k++) {
            Vec3[] a = seq.get(k), bb = seq.get(k + 1);
            int sides = Math.max(a.length, bb.length);
            for (int i = 0; i < sides; i++) {
                int i2 = (i + 1) % sides;
                if (a.length == 1)       out.add(new Triangle(a[0], bb[i], bb[i2], m));
                else if (bb.length == 1) out.add(new Triangle(a[i], a[i2], bb[0], m));
                else                     addQuad(out, a[i], a[i2], bb[i2], bb[i], m, ORIGIN);
            }
        }
    }

    /* A closed glass tube round a centre line: rings square to the line (carried along without
     * twisting), fluted - the radius swells in 'flutes' ridges of fluteDepth - with the flutes
     * spiralling at 'twist' rad per unit of length. The base is rounded off and the tip drawn to a
     * point, so the solid is watertight. */
    private static void addTube(ArrayList<Triangle> out, Vec3[] c, float[] r, int sides, int flutes, float fluteDepth,
                                float twist, Material m) {
        int n = c.length;
        Vec3[] tangent = new Vec3[n];
        for (int k = 0; k < n; k++) {
            Vec3 a = c[Math.max(0, k - 1)], bb = c[Math.min(n - 1, k + 1)];
            tangent[k] = unit(v(bb.x - a.x, bb.y - a.y, bb.z - a.z));
        }

        ArrayList<Vec3[]> seq = new ArrayList<>();
        Vec3 t0 = tangent[0];
        Vec3 normal = unit(cross3(t0, Math.abs(t0.y) < 0.9f ? v(0f, 1f, 0f) : v(1f, 0f, 0f)));

        // rounded foot: a pole just behind the first ring, and a ring half way round to it
        seq.add(new Vec3[]{lin(c[0], t0, -0.7f * r[0])});
        seq.add(tubeRing(lin(c[0], t0, -0.45f * r[0]), t0, normal, 0.75f * r[0], sides, flutes, fluteDepth, 0f));

        float along = 0f;
        for (int k = 0; k < n; k++) {
            if (k > 0) {
                Vec3 t = tangent[k];
                normal = unit(lin(normal, t, -(normal.x * t.x + normal.y * t.y + normal.z * t.z))); // carry the frame along
                along += (float) Math.sqrt((c[k].x - c[k - 1].x) * (c[k].x - c[k - 1].x) + (c[k].y - c[k - 1].y) * (c[k].y - c[k - 1].y)
                                           + (c[k].z - c[k - 1].z) * (c[k].z - c[k - 1].z));
            }
            seq.add(tubeRing(c[k], tangent[k], normal, r[k], sides, flutes, fluteDepth, twist * along));
        }
        seq.add(new Vec3[]{lin(c[n - 1], tangent[n - 1], 1.5f * r[n - 1])}); // tip
        connectRings(out, seq, m);
    }

    private static Vec3[] tubeRing(Vec3 c, Vec3 t, Vec3 normal, float r, int sides, int flutes, float fluteDepth, float spin) {
        Vec3 binormal = cross3(t, normal);
        Vec3[] ring = new Vec3[sides];
        for (int j = 0; j < sides; j++) {
            double a = 2 * Math.PI * j / sides;
            float rr = r * (1 + fluteDepth * (float) Math.cos(flutes * a + spin));
            ring[j] = lin(lin(c, normal, rr * (float) Math.cos(a)), binormal, rr * (float) Math.sin(a));
        }
        return ring;
    }

    /* A blown bowl ("Macchia") as one closed solid: outside surface from the bottom up to the rim,
     * a rounded lip, then the inside back down. Quarter-ellipse profile reaching radius R and height
     * H at the rim; the rim rises and falls in 'waves' waves of height amp and ruffles in and out,
     * both growing towards the rim. The inside is the same shape inset by the wall thickness th. */
    private static void addVessel(ArrayList<Triangle> out, Vec3 base, float R, float H, float th, int waves, float amp,
                                  double phase, int rings, int sides, Material m) {
        ArrayList<Vec3[]> seq = new ArrayList<>();
        seq.add(new Vec3[]{v(base.x, base.y, base.z)});                                            // outside, bottom centre
        for (int k = 1; k <= rings; k++) seq.add(vesselRing(base, R, H, (float) k / rings, 0f, 0f, waves, amp, phase, sides));
        for (int l = 1; l <= 3; l++) {                                                             // the lip
            float f = l / 4f;
            seq.add(vesselRing(base, R, H, 1f, f * th, 0.6f * th * (float) Math.sin(Math.PI * f), waves, amp, phase, sides));
        }
        for (int k = rings; k >= 1; k--) seq.add(vesselRing(base, R, H, (float) k / rings, th, 0f, waves, amp, phase, sides));
        seq.add(new Vec3[]{v(base.x, base.y + th, base.z)});                                       // inside, bottom centre
        connectRings(out, seq, m);
    }

    private static Vec3[] vesselRing(Vec3 base, float R, float H, float t, float inset, float lift, int waves, float amp,
                                     double phase, int sides) {
        float s = (float) Math.sin(Math.PI * t / 2), co = (float) Math.cos(Math.PI * t / 2);
        Vec3[] ring = new Vec3[sides];
        for (int j = 0; j < sides; j++) {
            double a = 2 * Math.PI * j / sides;
            float rr = (R - inset) * s * (1 + 0.06f * (float) Math.sin(waves * a + phase + 0.8) * t * t); // ruffle
            float yy = inset + (H - inset) * (1 - co) + amp * (float) Math.sin(waves * a + phase) * t * t * t + lift;
            ring[j] = v(base.x + rr * (float) Math.cos(a), base.y + yy, base.z + rr * (float) Math.sin(a));
        }
        return ring;
    }

    /* A track spotlight hanging from 'mount' (a point on the ceiling) and aimed at 'target': a stem
     * down to a closed-back can, a bulb in the back of the can and the point light in front of the
     * bulb. The can is a thick-walled shell (outside, lip, inside), so light only leaves through
     * its mouth: with the light CAN_LEN - LIGHT_AT = 0.30 back from a mouth CAN_R - 0.012 wide, the
     * beam's half-angle is about atan(0.098 / 0.30) = 18 degrees.
     * The light sits 0.185 off the bulb rather than touching it: close enough that the bulb still
     * blows out head-on, but ~28x less light on it than a hair away, which is what stops stray
     * bounces into the bulb from coming back as fireflies. */
    private static void addSpotlight(ArrayList<Triangle> out, ArrayList<Sphere> balls, ArrayList<PointLight> rig,
                                     Vec3 mount, Vec3 target, float intensity, Material body, Material bulb) {
        final float STEM = 0.35f, CAN_R = 0.11f, CAN_LEN = 0.60f, WALL = 0.012f;
        final float BULB_AT = 0.07f, BULB_R = 0.045f, LIGHT_AT = BULB_AT + BULB_R + 0.185f; // along the can from its back

        Vec3 pivot = v(mount.x, mount.y - STEM, mount.z);
        Vec3 aim = unit(v(target.x - pivot.x, target.y - pivot.y, target.z - pivot.z));
        Vec3 back = lin(pivot, aim, -0.3f * CAN_LEN); // hung a little behind its middle

        addAxialLathe(out, back, aim, new Profile()
                .to(0f, 0f).to(CAN_R, 0f).to(CAN_R, CAN_LEN)                       // closed back, outside
                .to(CAN_R - WALL, CAN_LEN).to(CAN_R - WALL, WALL).to(0f, WALL),   // lip, then back down the inside
                32, 0, 0f, body);
        balls.add(new Sphere(lin(back, aim, BULB_AT), BULB_R, bulb));
        rig.add(new PointLight(lin(back, aim, LIGHT_AT), new Vec3(255, 244, 225), intensity));

        // stem: straight down from the track to the top of the can
        float along = aim.y;
        Vec3 top = unit(v(-aim.x * along, 1f - aim.y * along, -aim.z * along)); // "up", square to the can
        Vec3 attach = lin(pivot, top, CAN_R);
        addLathe(out, v(attach.x, attach.y, attach.z), 1f, new Profile().to(0.012f, 0f).to(0.012f, mount.y - attach.y), 12, body);
    }


    // ============================ scene 6: the gaming PC ============================

    /* An origin and three unit axes. p(x, y, z) is the point x along ax, y along ay and z along az
     * from o. Every PC part is built in a frame like this, so the same fan builder serves a fan on
     * the ceiling, the floor, the rear wall or the motherboard tray. */
    private static final class Frame {
        final Vec3 o, ax, ay, az;

        Frame(Vec3 o, Vec3 ax, Vec3 ay, Vec3 az) {
            this.o = o;
            this.ax = ax;
            this.ay = ay;
            this.az = az;
        }

        // the world axes, centred on c
        static Frame axis(Vec3 c) { return new Frame(c, v(1f, 0f, 0f), v(0f, 1f, 0f), v(0f, 0f, 1f)); }

        // az = n (the way the part faces), ax = u, ay to complete a right-handed set
        static Frame facing(Vec3 c, Vec3 n, Vec3 u) { return new Frame(c, u, cross3(n, u), n); }

        Vec3 p(float x, float y, float z) {
            return v(o.x + ax.x * x + ay.x * y + az.x * z,
                     o.y + ax.y * x + ay.y * y + az.y * z,
                     o.z + ax.z * x + ay.z * y + az.z * z);
        }

        Frame at(float x, float y, float z) { return new Frame(p(x, y, z), ax, ay, az); }
    }

    /* A box centred on f.o, half-sizes hx, hy, hz along f's axes, every edge chamfered by 'bevel':
     * 6 faces, 12 edge strips and 8 corner triangles (44 triangles). The chamfers catch the light
     * along the edges, which is most of what makes a machined part look solid. */
    private static void addBevelBox(ArrayList<Triangle> out, Frame f, float hx, float hy, float hz, float bevel, Material m) {
        float e = Math.min(bevel, 0.45f * Math.min(hx, Math.min(hy, hz)));
        Vec3[][][][] c = new Vec3[2][2][2][];
        for (int i = 0; i < 2; i++) for (int j = 0; j < 2; j++) for (int k = 0; k < 2; k++) {
            float sx = 2 * i - 1, sy = 2 * j - 1, sz = 2 * k - 1;
            c[i][j][k] = new Vec3[]{
                f.p(sx * hx, sy * (hy - e), sz * (hz - e)),  // on the x face
                f.p(sx * (hx - e), sy * hy, sz * (hz - e)),  // on the y face
                f.p(sx * (hx - e), sy * (hy - e), sz * hz)}; // on the z face
            out.add(new Triangle(c[i][j][k][0], c[i][j][k][1], c[i][j][k][2], m));
        }
        for (int s = 0; s < 2; s++) {
            addQuad(out, c[s][0][0][0], c[s][1][0][0], c[s][1][1][0], c[s][0][1][0], m, ORIGIN);
            addQuad(out, c[0][s][0][1], c[1][s][0][1], c[1][s][1][1], c[0][s][1][1], m, ORIGIN);
            addQuad(out, c[0][0][s][2], c[1][0][s][2], c[1][1][s][2], c[0][1][s][2], m, ORIGIN);
        }
        for (int a = 0; a < 2; a++) for (int b = 0; b < 2; b++) {
            addQuad(out, c[a][b][0][0], c[a][b][1][0], c[a][b][1][1], c[a][b][0][1], m, ORIGIN); // edges along z
            addQuad(out, c[a][0][b][0], c[a][1][b][0], c[a][1][b][2], c[a][0][b][2], m, ORIGIN); // edges along y
            addQuad(out, c[0][a][b][1], c[1][a][b][1], c[1][a][b][2], c[0][a][b][2], m, ORIGIN); // edges along x
        }
    }

    // the same, square to the world axes, from its corners
    private static void addBevelBox(ArrayList<Triangle> out, Vec3 min, Vec3 max, float bevel, Material m) {
        addBevelBox(out, Frame.axis(v((min.x + max.x) / 2, (min.y + max.y) / 2, (min.z + max.z) / 2)),
                    (max.x - min.x) / 2, (max.y - min.y) / 2, (max.z - min.z) / 2, bevel, m);
    }

    /* A stroke font with just the glyphs the build needs: polylines {x0,y0, x1,y1, ...} on a cell
     * 0.6 wide (0.3 for I, 1 and the degree sign) and 1 tall. */
    private static final java.util.Map<Character, float[][]> GLYPHS = new java.util.HashMap<>();
    static {
        glyph('A', new float[]{0,0, .3f,1, .6f,0}, new float[]{.12f,.4f, .48f,.4f});
        glyph('C', new float[]{.6f,.82f, .45f,1, .15f,1, 0,.82f, 0,.18f, .15f,0, .45f,0, .6f,.18f});
        glyph('E', new float[]{.6f,1, 0,1, 0,0, .6f,0}, new float[]{0,.5f, .45f,.5f});
        glyph('F', new float[]{.6f,1, 0,1, 0,0}, new float[]{0,.5f, .45f,.5f});
        glyph('G', new float[]{.6f,.82f, .45f,1, .15f,1, 0,.82f, 0,.18f, .15f,0, .45f,0, .6f,.18f, .6f,.45f, .32f,.45f});
        glyph('I', new float[]{.15f,0, .15f,1});
        glyph('L', new float[]{0,1, 0,0, .6f,0});
        glyph('M', new float[]{0,0, 0,1, .3f,.45f, .6f,1, .6f,0});
        glyph('N', new float[]{0,0, 0,1, .6f,0, .6f,1});
        glyph('O', new float[]{.15f,0, .45f,0, .6f,.18f, .6f,.82f, .45f,1, .15f,1, 0,.82f, 0,.18f, .15f,0});
        glyph('P', new float[]{0,0, 0,1, .45f,1, .6f,.85f, .6f,.63f, .45f,.48f, 0,.48f});
        glyph('R', new float[]{0,0, 0,1, .45f,1, .6f,.85f, .6f,.63f, .45f,.48f, 0,.48f}, new float[]{.3f,.48f, .6f,0});
        glyph('T', new float[]{0,1, .6f,1}, new float[]{.3f,1, .3f,0});
        glyph('U', new float[]{0,1, 0,.18f, .15f,0, .45f,0, .6f,.18f, .6f,1});
        glyph('V', new float[]{0,1, .3f,0, .6f,1});
        glyph('W', new float[]{0,1, .12f,0, .3f,.62f, .48f,0, .6f,1});
        glyph('X', new float[]{0,0, .6f,1}, new float[]{0,1, .6f,0});
        glyph('1', new float[]{0,.78f, .2f,1, .2f,0});
        glyph('2', new float[]{0,.8f, .15f,1, .45f,1, .6f,.82f, .6f,.62f, 0,0, .6f,0});
        glyph('3', new float[]{0,.85f, .15f,1, .45f,1, .6f,.85f, .6f,.65f, .45f,.52f, .2f,.52f},
                   new float[]{.45f,.52f, .6f,.38f, .6f,.15f, .45f,0, .15f,0, 0,.15f});
        glyph('4', new float[]{.45f,0, .45f,1, 0,.3f, .6f,.3f});
        glyph('5', new float[]{.6f,1, 0,1, 0,.55f, .45f,.55f, .6f,.4f, .6f,.15f, .45f,0, .15f,0, 0,.15f});
        glyph('6', new float[]{.55f,.95f, .4f,1, .15f,1, 0,.8f, 0,.18f, .15f,0, .45f,0, .6f,.18f, .6f,.4f, .45f,.55f, .15f,.55f, 0,.38f});
        glyph('°', new float[]{.03f,1, .23f,1, .23f,.8f, .03f,.8f, .03f,1});
    }

    private static void glyph(char c, float[]... lines) { GLYPHS.put(c, lines); }

    private static float advance(char c) {
        return switch (c) { case 'I', '1', '°' -> 0.3f; case ' ' -> 0.45f; default -> 0.6f; };
    }

    /* Text on f's plane: baseline along f.ax starting at f.o (or centred on it), letters up f.ay.
     * f.o should already sit a hair off the surface it's printed on. Returns the width. */
    private static float addText(ArrayList<Triangle> out, String text, Frame f, float height, float stroke, boolean centred, Material m) {
        final float GAP = 0.22f;
        float width = -GAP * height;
        for (char c : text.toCharArray()) width += (advance(c) + GAP) * height;
        float x = centred ? -width / 2 : 0f;
        for (char c : text.toCharArray()) {
            float[][] lines = GLYPHS.get(c);
            if (lines != null) for (float[] line : lines) {
                for (int k = 0; k + 3 < line.length; k += 2) {
                    addStroke(out, f, x + line[k] * height, line[k + 1] * height, x + line[k + 2] * height, line[k + 3] * height, stroke, m);
                }
            }
            x += (advance(c) + GAP) * height;
        }
        return width;
    }

    // one straight stroke in f's plane, w wide, with square caps so the joints of a polyline fill in
    private static void addStroke(ArrayList<Triangle> out, Frame f, float x0, float y0, float x1, float y1, float w, Material m) {
        float dx = x1 - x0, dy = y1 - y0, l = (float) Math.sqrt(dx * dx + dy * dy);
        if (l < 1e-5f) return;
        float tx = dx / l * w / 2, ty = dy / l * w / 2; // along the stroke, half the width
        addQuad(out, f.p(x0 - tx - ty, y0 - ty + tx, 0f), f.p(x1 + tx - ty, y1 + ty + tx, 0f),
                     f.p(x1 + tx + ty, y1 + ty - tx, 0f), f.p(x0 - tx + ty, y0 - ty - tx, 0f), m, ORIGIN);
    }

    // a fan's materials: frame, blades, the white LED surfaces, the dark hub mirror, the ring on the mirror
    private record FanLook(Material frame, Material blade, Material glow, Material mirror, Material ring) {}

    /* A UNI FAN TL style fan. f.o is the middle of the front (intake) face, f.az points out of it and
     * f.ax towards the side that carries the light bar. size 12 or 14 (cm); 2.5 thick.
     *   - frame: a square with chamfered corners round a round bore, one ring of quads between the
     *     octagon and the bore circle, front, back, outside and bore -> 8 * RING triangles
     *   - lit strips along the four chamfers, just proud of the face
     *   - light bar: a tray on the ax side, lined in white and open outwards, with barLights lights
     *     inside. Its walls stop every light ray except the ones leaving through the mouth, so the
     *     lining blows out and only what's in front of the bar gets its light
     *   - nine swept, twisted, cambered blades
     *   - hub: a black can with a white chamfered ring at the front, a dark mirror inside the ring
     *     and a grey ring on the mirror (the infinity reflection)
     *   - one light on the axis 2 cm out: it lights the hub ring, the blades and the strips, and
     *     throws the fan's glow into the case the way the real fans do */
    private static void addFan(ArrayList<Triangle> out, ArrayList<PointLight> rig, Frame f, float size, FanLook look,
                               Vec3 rgb, float hubI, float barI, int barLights) {
        final float T = 2.5f, s = size / 12f;
        final float A = 6f * s, C = 1.7f * s, BORE = 5.65f * s, HUB = 2.3f * s, TIP = 5.5f * s;
        final int SIDE = 12, CHAMFER = 4, RING = 4 * (SIDE + CHAMFER);

        // octagon corners, anticlockwise from the bottom of the ax side: even edges are sides, odd ones chamfers
        float[][] oct = {{A, -(A - C)}, {A, A - C}, {A - C, A}, {-(A - C), A}, {-A, A - C}, {-A, -(A - C)}, {-(A - C), -A}, {A - C, -A}};
        Vec3[] outF = new Vec3[RING], outB = new Vec3[RING], inF = new Vec3[RING], inB = new Vec3[RING];
        int q = 0;
        for (int e = 0; e < 8; e++) {
            float[] p0 = oct[e], p1 = oct[(e + 1) % 8];
            int steps = e % 2 == 0 ? SIDE : CHAMFER;
            for (int k = 0; k < steps; k++, q++) {
                float t = (float) k / steps, x = lerp(p0[0], p1[0], t), y = lerp(p0[1], p1[1], t);
                double ang = Math.atan2(y, x);
                float bx = BORE * (float) Math.cos(ang), by = BORE * (float) Math.sin(ang);
                outF[q] = f.p(x, y, 0f);
                outB[q] = f.p(x, y, -T);
                inF[q] = f.p(bx, by, 0f);
                inB[q] = f.p(bx, by, -T);
            }
        }
        for (int k = 0; k < RING; k++) {
            int k2 = (k + 1) % RING;
            addQuad(out, outF[k], outF[k2], inF[k2], inF[k], look.frame(), ORIGIN); // front
            addQuad(out, outB[k], outB[k2], inB[k2], inB[k], look.frame(), ORIGIN); // back
            addQuad(out, outF[k], outF[k2], outB[k2], outB[k], look.frame(), ORIGIN); // outside
            addQuad(out, inF[k], inF[k2], inB[k2], inB[k], look.frame(), ORIGIN);   // bore
        }

        // lit strips on the chamfers
        for (int e = 1; e < 8; e += 2) {
            float[] p0 = oct[e], p1 = oct[(e + 1) % 8];
            float mx = (p0[0] + p1[0]) / 2, my = (p0[1] + p1[1]) / 2, ml = (float) Math.sqrt(mx * mx + my * my);
            float ix = -mx / ml, iy = -my / ml;                        // inwards, square to the chamfer
            float dx = p1[0] - p0[0], dy = p1[1] - p0[1], dl = (float) Math.sqrt(dx * dx + dy * dy);
            dx /= dl;
            dy /= dl;
            float in0 = 0.3f * s, in1 = 0.62f * s, trim = 0.2f * s;
            addQuad(out, f.p(p0[0] + dx * trim + ix * in0, p0[1] + dy * trim + iy * in0, 0.03f),
                         f.p(p1[0] - dx * trim + ix * in0, p1[1] - dy * trim + iy * in0, 0.03f),
                         f.p(p1[0] - dx * trim + ix * in1, p1[1] - dy * trim + iy * in1, 0.03f),
                         f.p(p0[0] + dx * trim + ix * in1, p0[1] + dy * trim + iy * in1, 0.03f), look.glow(), ORIGIN);
        }

        /* light bar, on the flat of the ax side: x runs out from the frame (a0 against it, a1 the
         * mouth, af the floor of the cavity), y along the side, z across the fan's thickness.
         * The cavity is ~0.8 deep and ~2 wide, so the kernel's +-0.35 light jitter stays inside. */
        final float H0 = -T + 0.12f, H1 = -0.12f, RIM = 0.15f;
        float half = (A - C) - 0.2f * s, a0 = A, a1 = A + 1.0f, af = A + 0.2f;
        float cb = half - RIM, c0 = H0 + RIM, c1 = H1 - RIM;
        Material shell = look.frame(), lining = look.glow();
        addQuad(out, f.p(a0, -half, H0), f.p(a1, -half, H0), f.p(a1, half, H0), f.p(a0, half, H0), shell, ORIGIN);
        addQuad(out, f.p(a0, -half, H1), f.p(a1, -half, H1), f.p(a1, half, H1), f.p(a0, half, H1), shell, ORIGIN);
        addQuad(out, f.p(a0, -half, H0), f.p(a1, -half, H0), f.p(a1, -half, H1), f.p(a0, -half, H1), shell, ORIGIN);
        addQuad(out, f.p(a0, half, H0), f.p(a1, half, H0), f.p(a1, half, H1), f.p(a0, half, H1), shell, ORIGIN);
        addQuad(out, f.p(a1, -half, H0), f.p(a1, half, H0), f.p(a1, cb, c0), f.p(a1, -cb, c0), shell, ORIGIN);   // rim
        addQuad(out, f.p(a1, -half, H1), f.p(a1, half, H1), f.p(a1, cb, c1), f.p(a1, -cb, c1), shell, ORIGIN);
        addQuad(out, f.p(a1, -half, H0), f.p(a1, -half, H1), f.p(a1, -cb, c1), f.p(a1, -cb, c0), shell, ORIGIN);
        addQuad(out, f.p(a1, half, H0), f.p(a1, half, H1), f.p(a1, cb, c1), f.p(a1, cb, c0), shell, ORIGIN);
        addQuad(out, f.p(af, -cb, c0), f.p(af, cb, c0), f.p(af, cb, c1), f.p(af, -cb, c1), lining, ORIGIN);      // floor
        addQuad(out, f.p(af, -cb, c0), f.p(a1, -cb, c0), f.p(a1, cb, c0), f.p(af, cb, c0), lining, ORIGIN);      // walls
        addQuad(out, f.p(af, -cb, c1), f.p(a1, -cb, c1), f.p(a1, cb, c1), f.p(af, cb, c1), lining, ORIGIN);
        addQuad(out, f.p(af, -cb, c0), f.p(a1, -cb, c0), f.p(a1, -cb, c1), f.p(af, -cb, c1), lining, ORIGIN);
        addQuad(out, f.p(af, cb, c0), f.p(a1, cb, c0), f.p(a1, cb, c1), f.p(af, cb, c1), lining, ORIGIN);
        for (int k = 0; k < barLights; k++) {
            float along = -cb + 2 * cb * (k + 0.5f) / barLights;
            rig.add(new PointLight(f.p(af + 0.4f, along, (c0 + c1) / 2), new Vec3(rgb), barI));
        }

        for (int b = 0; b < 9; b++) {
            addBlade(out, f, 2 * Math.PI * b / 9, HUB + 0.05f * s, TIP, 30f, 33f, -T / 2 - 0.1f, T - 0.5f, look.blade());
        }

        // hub, built back to front from the back of the can
        Vec3 back = f.p(0f, 0f, -T + 0.3f);
        float front = T - 0.3f, sunk = front - 0.28f, notch = HUB - 0.45f * s;
        addAxialLathe(out, back, f.az, new Profile().to(0f, 0f).to(HUB, 0f).to(HUB, front), 40, 0, 0f, look.frame());
        addAxialLathe(out, back, f.az, new Profile().to(HUB, front).to(notch, sunk), 40, 0, 0f, look.glow());
        addAxialLathe(out, back, f.az, new Profile().to(notch, sunk).to(0f, sunk), 40, 0, 0f, look.mirror());
        addAxialLathe(out, back, f.az, new Profile().to(1.32f * s, sunk + 0.01f).to(1.15f * s, sunk + 0.01f), 40, 0, 0f, look.ring());
        rig.add(new PointLight(f.p(0f, 0f, 2.0f), new Vec3(rgb), hubI));
    }

    /* One blade in fan frame f, turning towards +angle: a sheet from radius r0 to r1, span0..span1
     * degrees wide (root..tip), leading edge towards the front. The pitch eases from 42 degrees at
     * the root to 24 at the tip (never rising more than hMax along the chord), the tip sweeps 16
     * degrees forwards and the section is cambered. hMid: where its middle sits along f.az.
     * -> 2 * 7 * 5 triangles. */
    private static void addBlade(ArrayList<Triangle> out, Frame f, double theta0, float r0, float r1, float span0, float span1,
                                 float hMid, float hMax, Material m) {
        final int NR = 7, NC = 5;
        Vec3[][] g = new Vec3[NR + 1][NC + 1];
        for (int i = 0; i <= NR; i++) {
            float t = (float) i / NR, r = lerp(r0, r1, t);
            double span = Math.toRadians(lerp(span0, span1, t)), centre = theta0 + Math.toRadians(16) * t * t;
            float arc = r * (float) span, rise = Math.min(arc * tan(lerp(42f, 24f, t)), hMax);
            for (int j = 0; j <= NC; j++) {
                float c = (float) j / NC;                                  // 0 = leading edge, 1 = trailing
                double ang = centre + (0.5 - c) * span;
                float h = hMid + (0.5f - c) * rise + 0.07f * arc * (float) Math.sin(Math.PI * c);
                g[i][j] = f.p(r * (float) Math.cos(ang), r * (float) Math.sin(ang), h);
            }
        }
        for (int i = 0; i < NR; i++) {
            for (int j = 0; j < NC; j++) addQuad(out, g[i][j], g[i + 1][j], g[i + 1][j + 1], g[i][j + 1], m, ORIGIN);
        }
    }

    /* AIO pump head on the CPU, facing +z out of the board surface at 'board': a black can on a
     * base ring, and a bezel round an LCD sunk 1.3 cm into it. The bezel's inner wall is white and
     * the pump's one light sits inside it on the axis, so the wall blows out - from off to the side
     * it's the lit crescent in the photo - and the light can only leave forwards. The LCD is dark
     * glass showing the coolant temperature, a gauge and the pump speed in white, lit by the same
     * light. Two tube fittings on top, at x = board.x +-1.1, z = board.z + 3.1. */
    private static void addPump(ArrayList<Triangle> out, ArrayList<PointLight> rig, Vec3 board, Vec3 rgb, float intensity,
                                Material black, Material gloss, Material glow, Material screen, Material ink) {
        final float R = 3.45f, BEZEL = 2.85f, BODY = 3.9f, SUNK = 1.3f;
        Vec3 z = v(0f, 0f, 1f);
        addAxialLathe(out, board, z, new Profile().to(0f, 0.3f).to(3.75f, 0.3f).to(3.75f, 0.9f).to(R, 0.9f), 64, 0, 0f, black);
        addAxialLathe(out, board, z, new Profile().to(R, 0.9f).to(R, BODY - 0.25f).curve(R, BODY, R - 0.25f, BODY, 3).to(BEZEL, BODY),
                      72, 0, 0f, gloss);
        addAxialLathe(out, board, z, new Profile().to(BEZEL, BODY).to(BEZEL, BODY - SUNK), 72, 0, 0f, glow);
        addAxialLathe(out, board, z, new Profile().to(BEZEL, BODY - SUNK).to(0f, BODY - SUNK), 72, 0, 0f, screen);
        rig.add(new PointLight(v(board.x, board.y, board.z + BODY - SUNK + 1.0f), new Vec3(rgb), intensity));

        // the screen: WATER / 36 degC, a gauge arc with its needle, PUMP 2514 RPM
        Frame lcd = Frame.axis(v(board.x, board.y, board.z + BODY - SUNK + 0.015f));
        addText(out, "WATER", lcd.at(-1.55f, 1.2f, 0f), 0.36f, 0.05f, false, ink);
        addText(out, "36", lcd.at(-1.75f, -0.3f, 0f), 1.2f, 0.15f, false, ink);
        addText(out, "°C", lcd.at(0.05f, 0.35f, 0f), 0.5f, 0.07f, false, ink);
        addText(out, "PUMP", lcd.at(0.25f, -1.25f, 0f), 0.3f, 0.045f, false, ink);
        addText(out, "2514 RPM", lcd.at(-0.35f, -1.72f, 0f), 0.3f, 0.045f, false, ink);
        for (int k = 0; k < 24; k++) {
            double a0 = Math.toRadians(-55 + 5.5 * k), a1 = Math.toRadians(-55 + 5.5 * (k + 1));
            addStroke(out, lcd, 2.3f * (float) Math.cos(a0), 2.3f * (float) Math.sin(a0),
                                2.3f * (float) Math.cos(a1), 2.3f * (float) Math.sin(a1), 0.07f, ink);
        }
        addStroke(out, lcd, 0.35f, 0.1f, 1.95f, -1.05f, 0.08f, ink);

        for (float dx : new float[]{-1.1f, 1.1f}) {
            addAxialLathe(out, v(board.x + dx, board.y + R - 0.45f, board.z + 3.1f), v(0f, 1f, 0f),
                          new Profile().to(0f, 0f).to(0.75f, 0f).to(0.75f, 1.3f).to(0f, 1.3f), 20, 0, 0f, black);
        }
    }

    /* RTX 5090 Founders Edition on a vertical mount: centre c, long axis +x, fans facing +z, 30.4 x
     * 13.7 x 4.0. The front is two halves, each a plate with a round hole in it (a ring of quads from
     * the plate's edge to the hole, mirrored so they meet exactly on the centre line), bevelled into
     * the sides. In each hole: a fin stack on the floor and a seven-blade fan. The X between the
     * holes is two polished bars. Along the top edge, GEFORCE RTX in white letters sunk in a channel
     * with two white lights in it (the Founders logo isn't RGB), and the 12V-2x6 socket. The I/O
     * bracket is at the -x end. */
    private static void addGpu(ArrayList<Triangle> out, ArrayList<PointLight> rig, Vec3 c, float logoI,
                               Material body, Material polished, Material fanBlack, Material fins, Material dark,
                               Material logo, Material bracket) {
        final float HL = 15.2f, HH = 6.85f, HT = 2.0f, E = 0.45f, ZB = HT - 0.45f; // half sizes, bevel, where the sides start
        final float HOLE = 5.4f, XC = 7.6f, FLOOR = 0.3f;
        final float LX0 = 4.6f, LX1 = 13.8f, LZ = 0.85f, LD = 0.9f;                // the logo channel on the top edge
        Frame g = Frame.axis(c);

        // left half of the front: the plate's outline (anticlockwise), and the hole points at the same angles
        float x0 = -(HL - E), y0 = -(HH - E), y1 = HH - E;
        ArrayList<float[]> rim = new ArrayList<>();
        int nx = 33, ny = 28;
        for (int k = 0; k < nx; k++) rim.add(new float[]{lerp(x0, 0f, (float) k / nx), y0});
        for (int k = 0; k < ny; k++) rim.add(new float[]{0f, lerp(y0, y1, (float) k / ny)});
        for (int k = 0; k < nx; k++) rim.add(new float[]{lerp(0f, x0, (float) k / nx), y1});
        for (int k = 0; k < ny; k++) rim.add(new float[]{x0, lerp(y1, y0, (float) k / ny)});
        int n = rim.size();
        for (int side = -1; side <= 1; side += 2) {
            Vec3[] edge = new Vec3[n], outer = new Vec3[n], lip = new Vec3[n], floor = new Vec3[n];
            boolean[] centre = new boolean[n];
            float hx = side * XC;                                            // this hole's centre (the left half is side -1)
            for (int k = 0; k < n; k++) {
                float px = side < 0 ? rim.get(k)[0] : -rim.get(k)[0], py = rim.get(k)[1]; // mirrored for the right half
                double ang = Math.atan2(py, px - hx);
                float qx = hx + HOLE * (float) Math.cos(ang), qy = HOLE * (float) Math.sin(ang);
                centre[k] = Math.abs(px) < 1e-4f;
                edge[k] = g.p(px, py, HT);
                outer[k] = g.p(Math.abs(px) >= HL - E - 1e-4f ? Math.signum(px) * HL : px,
                               py <= y0 + 1e-4f ? -HH : py >= y1 - 1e-4f ? HH : py, ZB);
                lip[k] = g.p(qx, qy, HT);
                floor[k] = g.p(qx, qy, FLOOR);
            }
            Vec3 mid = g.p(hx, 0f, FLOOR);
            for (int k = 0; k < n; k++) {
                int k2 = (k + 1) % n;
                addQuad(out, edge[k], edge[k2], lip[k2], lip[k], body, ORIGIN);        // face
                addQuad(out, lip[k], lip[k2], floor[k2], floor[k], body, ORIGIN);      // hole wall
                out.add(new Triangle(mid, floor[k], floor[k2], dark));                   // hole floor
                if (!(centre[k] && centre[k2])) addQuad(out, edge[k], edge[k2], outer[k2], outer[k], body, ORIGIN); // bevel
            }

            // fin stack: plates across the hole, square to x
            for (float dx = -HOLE + 0.25f; dx < HOLE - 0.2f; dx += 0.32f) {
                float yh = (float) Math.sqrt(HOLE * HOLE - dx * dx) - 0.05f;
                addQuad(out, g.p(hx + dx, -yh, FLOOR), g.p(hx + dx, yh, FLOOR), g.p(hx + dx, yh, 1.05f), g.p(hx + dx, -yh, 1.05f), fins, ORIGIN);
            }
            Frame fan = Frame.facing(g.p(hx, 0f, 0f), v(0f, 0f, 1f), v(1f, 0f, 0f));
            for (int b = 0; b < 7; b++) addBlade(out, fan, 2 * Math.PI * b / 7 + side, 1.75f, 5.15f, 38f, 44f, 1.5f, 0.7f, fanBlack);
            addAxialLathe(out, g.p(hx, 0f, 0.95f), v(0f, 0f, 1f),
                          new Profile().to(0f, 0f).to(1.7f, 0f).to(1.7f, 0.85f).to(1.45f, 1.0f).to(0f, 1.0f), 32, 0, 0f, fanBlack);
            addAxialLathe(out, g.p(hx, 0f, 0.96f), v(0f, 0f, 1f), new Profile().to(0.7f, 1.0f).to(0f, 1.0f), 24, 0, 0f, polished);
        }

        // sides: bottom, the two ends, and the top with the logo channel cut out of it
        addQuad(out, g.p(-HL, -HH, -ZB), g.p(HL, -HH, -ZB), g.p(HL, -HH, ZB), g.p(-HL, -HH, ZB), body, ORIGIN);
        addQuad(out, g.p(-HL, -HH, -ZB), g.p(-HL, HH, -ZB), g.p(-HL, HH, ZB), g.p(-HL, -HH, ZB), body, ORIGIN);
        addQuad(out, g.p(HL, -HH, -ZB), g.p(HL, HH, -ZB), g.p(HL, HH, ZB), g.p(HL, -HH, ZB), body, ORIGIN);
        addQuad(out, g.p(-HL, HH, -ZB), g.p(LX0, HH, -ZB), g.p(LX0, HH, ZB), g.p(-HL, HH, ZB), body, ORIGIN);
        addQuad(out, g.p(LX1, HH, -ZB), g.p(HL, HH, -ZB), g.p(HL, HH, ZB), g.p(LX1, HH, ZB), body, ORIGIN);
        addQuad(out, g.p(LX0, HH, LZ), g.p(LX1, HH, LZ), g.p(LX1, HH, ZB), g.p(LX0, HH, ZB), body, ORIGIN);
        addQuad(out, g.p(LX0, HH, -ZB), g.p(LX1, HH, -ZB), g.p(LX1, HH, -LZ), g.p(LX0, HH, -LZ), body, ORIGIN);

        // back: a plain plate bevelled into the sides like a picture frame
        Vec3[] in = {g.p(-HL + E, -HH + E, -HT), g.p(HL - E, -HH + E, -HT), g.p(HL - E, HH - E, -HT), g.p(-HL + E, HH - E, -HT)};
        Vec3[] ou = {g.p(-HL, -HH, -ZB), g.p(HL, -HH, -ZB), g.p(HL, HH, -ZB), g.p(-HL, HH, -ZB)};
        addQuad(out, in[0], in[1], in[2], in[3], body, ORIGIN);
        for (int k = 0; k < 4; k++) addQuad(out, in[k], in[(k + 1) % 4], ou[(k + 1) % 4], ou[k], body, ORIGIN);

        // the X
        float xl = 3.3f, yl = 6.2f, len = (float) Math.sqrt(4 * xl * xl + 4 * yl * yl) / 2;
        for (int d = -1; d <= 1; d += 2) {
            Vec3 along = unit(v(xl, d * yl, 0f));
            addBevelBox(out, new Frame(g.p(0f, 0f, HT + 0.12f), along, cross3(v(0f, 0f, 1f), along), v(0f, 0f, 1f)), len, 0.42f, 0.14f, 0.1f, polished);
        }

        // GEFORCE RTX: channel walls and floor, the letters on the floor, two lights over them
        float fy = HH - LD;
        addQuad(out, g.p(LX0, fy, -LZ), g.p(LX1, fy, -LZ), g.p(LX1, fy, LZ), g.p(LX0, fy, LZ), dark, ORIGIN);
        addQuad(out, g.p(LX0, fy, -LZ), g.p(LX1, fy, -LZ), g.p(LX1, HH, -LZ), g.p(LX0, HH, -LZ), dark, ORIGIN);
        addQuad(out, g.p(LX0, fy, LZ), g.p(LX1, fy, LZ), g.p(LX1, HH, LZ), g.p(LX0, HH, LZ), dark, ORIGIN);
        addQuad(out, g.p(LX0, fy, -LZ), g.p(LX0, fy, LZ), g.p(LX0, HH, LZ), g.p(LX0, HH, -LZ), dark, ORIGIN);
        addQuad(out, g.p(LX1, fy, -LZ), g.p(LX1, fy, LZ), g.p(LX1, HH, LZ), g.p(LX1, HH, -LZ), dark, ORIGIN);
        float mx = (LX0 + LX1) / 2;
        addText(out, "GEFORCE RTX", new Frame(g.p(mx, fy + 0.015f, 0.36f), v(1f, 0f, 0f), v(0f, 0f, -1f), v(0f, 1f, 0f)), 0.72f, 0.1f, true, logo);
        for (float dx : new float[]{-2.3f, 2.3f}) rig.add(new PointLight(g.p(mx + dx, fy + 0.45f, 0f), v(255f, 255f, 255f), logoI));

        // 12V-2x6 socket on the top edge; I/O bracket and its ports at the -x end
        addBevelBox(out, g.at(0f, HH + 0.28f, -0.5f), 1.0f, 0.28f, 0.6f, 0.08f, dark);
        addBevelBox(out, g.at(-HL - 0.5f, 0f, 0f), 0.5f, HH - 1.2f, 1.2f, 0.05f, dark);
        addBevelBox(out, g.at(-HL - 1.12f, 0.4f, 0f), 0.1f, HH + 0.5f, HT + 0.25f, 0.03f, bracket);
    }

    /* White ATX board (24.4 x 30.5) on the tray, laid out from its bottom rear corner (x0, y0), its
     * front surface at z = mb: PCB, finned VRM heatsinks, the rear I/O shroud, four DIMM slots with
     * two white Vengeance sticks in the second and fourth, the armoured PCIe x16 slot, two M.2
     * heatsinks, the chipset heatsink, 24-pin, 8-pin EPS and SATA sockets, the front-panel header and
     * domed screws (half-buried spheres). The CPU socket is under the pump, centred on
     * (x0 + 10.7, y0 + 22.1). */
    private static void addMotherboard(ArrayList<Triangle> out, ArrayList<Sphere> balls, float x0, float y0, float mb,
                                       Material pcb, Material alu, Material white, Material black, Material ink, Material steel) {
        java.util.function.BiConsumer<float[], Material> box = (b, m) ->
                addBevelBox(out, v(x0 + b[0], y0 + b[1], mb + b[2]), v(x0 + b[3], y0 + b[4], mb + b[5]), b[6], m);

        addBevelBox(out, v(x0, y0, mb - 0.16f), v(x0 + 24.4f, y0 + 30.5f, mb), 0.05f, pcb);

        // VRM heatsinks: a base with fins standing on it
        box.accept(new float[]{5.8f, 27.3f, 0f, 13.8f, 29.9f, 0.8f, 0.1f}, alu);
        for (int k = 0; k < 11; k++) box.accept(new float[]{6.0f + 0.72f * k, 27.3f, 0.8f, 6.3f + 0.72f * k, 29.9f, 2.3f, 0.05f}, alu);
        box.accept(new float[]{3.2f, 15.6f, 0f, 5.5f, 26.4f, 0.8f, 0.1f}, alu);
        for (int k = 0; k < 12; k++) box.accept(new float[]{3.2f, 15.8f + 0.87f * k, 0.8f, 5.5f, 16.15f + 0.87f * k, 2.5f, 0.05f}, alu);

        box.accept(new float[]{0.1f, 17.6f, 0f, 3.0f, 30.2f, 3.0f, 0.3f}, white);          // rear I/O shroud
        box.accept(new float[]{0.4f, 18.4f, 3.0f, 0.7f, 29.4f, 3.08f, 0.02f}, ink);         // its accent line

        // DIMM slots, latches at both ends; sticks in the second and fourth
        float[] slots = {15.6f, 16.4f, 17.2f, 18.0f};
        for (int k = 0; k < 4; k++) {
            float xs = slots[k];
            box.accept(new float[]{xs - 0.3f, 16.0f, 0f, xs + 0.3f, 29.3f, 0.8f, 0.08f}, black);
            box.accept(new float[]{xs - 0.3f, 29.3f, 0f, xs + 0.3f, 29.9f, 1.1f, 0.08f}, white);
            box.accept(new float[]{xs - 0.3f, 15.4f, 0f, xs + 0.3f, 16.0f, 1.1f, 0.08f}, white);
            if (k % 2 == 0) continue;
            box.accept(new float[]{xs - 0.36f, 16.15f, 0.8f, xs + 0.36f, 29.15f, 4.4f, 0.12f}, white);
            box.accept(new float[]{xs - 0.28f, 16.6f, 4.4f, xs + 0.28f, 28.7f, 4.75f, 0.1f}, white);
            addText(out, "VENGEANCE", new Frame(v(x0 + xs + 0.375f, y0 + 22.65f, mb + 2.2f), v(0f, 1f, 0f), v(0f, 0f, 1f), v(1f, 0f, 0f)),
                    0.55f, 0.075f, true, ink);
        }

        box.accept(new float[]{1.7f, 10.8f, 0f, 10.6f, 11.55f, 1.1f, 0.1f}, alu);           // PCIe x16, armoured
        box.accept(new float[]{10.6f, 10.8f, 0f, 11.4f, 11.55f, 1.0f, 0.08f}, black);
        box.accept(new float[]{4.7f, 12.8f, 0f, 14.7f, 15.0f, 0.7f, 0.15f}, alu);            // M.2 heatsinks
        box.accept(new float[]{2.2f, 6.6f, 0f, 12.2f, 8.8f, 0.7f, 0.15f}, alu);
        box.accept(new float[]{15.2f, 1.6f, 0f, 23.0f, 8.1f, 1.2f, 0.3f}, alu);              // chipset
        box.accept(new float[]{16.2f, 2.4f, 1.2f, 22.0f, 3.0f, 1.32f, 0.03f}, white);
        box.accept(new float[]{23.2f, 17.6f, 0f, 24.2f, 23.8f, 1.7f, 0.1f}, black);          // 24-pin
        box.accept(new float[]{23.3f, 3.6f, 0f, 24.3f, 8.0f, 0.9f, 0.08f}, black);           // SATA
        box.accept(new float[]{3.4f, 29.4f, 0f, 5.4f, 30.3f, 1.5f, 0.08f}, black);           // 8-pin EPS
        box.accept(new float[]{16.0f, 0.2f, 0f, 22.0f, 0.9f, 0.8f, 0.05f}, black);           // front-panel header

        float[][] screws = {{0.6f, 0.6f}, {13.0f, 0.6f}, {23.8f, 0.6f}, {0.6f, 15.0f}, {14.9f, 11.2f}, {23.8f, 15.0f}, {14.9f, 29.9f}, {23.8f, 29.9f}};
        for (float[] s : screws) balls.add(new Sphere(v(x0 + s[0], y0 + s[1], mb), 0.3f, steel));
    }

    // n + 1 points along the Bezier curve with these control points (de Casteljau)
    private static Vec3[] bezier(int n, Vec3... ctrl) {
        Vec3[] pts = new Vec3[n + 1];
        for (int i = 0; i <= n; i++) {
            float t = (float) i / n;
            Vec3[] p = ctrl.clone();
            for (int level = p.length - 1; level > 0; level--) {
                for (int k = 0; k < level; k++) p[k] = v(lerp(p[k].x, p[k + 1].x, t), lerp(p[k].y, p[k + 1].y, t), lerp(p[k].z, p[k + 1].z, t));
            }
            pts[i] = p[0];
        }
        return pts;
    }

    // a sleeved cable or braided tube along a Bezier curve; the weave is flutes twisting at 'twist' rad per cm
    private static void addCable(ArrayList<Triangle> out, float radius, int flutes, float twist, Material m, Vec3... ctrl) {
        Vec3[] c = bezier(28, ctrl);
        float[] r = new float[c.length];
        java.util.Arrays.fill(r, radius);
        addTube(out, c, r, 14, flutes, 0.07f, twist, m);
    }


    // ============================ scene 7: the 787 ============================

    /* An area light, made of point lights: an nu x nv grid spread evenly over the rectangle
     * centre +- halfU +- halfV, sharing totalIntensity between them. The kernel only has point
     * lights, so this is how a light gets a size - its shadows soften with the panel's width and
     * it falls off like a panel rather than a pin. The light BVH samples one of them per hit, so
     * a bigger grid costs a deeper tree, not more shadow rays. */
    private static void addAreaLightGrid(ArrayList<PointLight> rig, Vec3 centre, Vec3 halfU, Vec3 halfV, int nu, int nv,
                                         Vec3 color, float totalIntensity) {
        float each = totalIntensity / (nu * nv);
        for (int i = 0; i < nu; i++) {
            float s = nu == 1 ? 0f : -1f + 2f * i / (nu - 1);
            for (int j = 0; j < nv; j++) {
                float t = nv == 1 ? 0f : -1f + 2f * j / (nv - 1);
                rig.add(new PointLight(lin(lin(centre, halfU, s), halfV, t), new Vec3(color), each));
            }
        }
    }

    /* A directional floodlight on its own mast, aimed at 'target'. The head is a shroud - an
     * open-fronted box, 3 m x 1.6 m across its mouth and 1.8 m deep, dark housing outside and a
     * white reflector across the back - with a 3 x 2 area light 0.3 m in front of the reflector.
     * Shadow rays from anywhere the mouth can't see hit the housing, so the light leaves as a beam
     * (about 45 degrees either side across the head, 28 up and down) rather than lighting the whole
     * apron. The reflector, lit from 0.3 m, is the bright face you see looking into the lamp - and
     * what the glossy paint picks up as a highlight. A tapering pole stands under the head. */
    private static void addAimedFlood(ArrayList<Triangle> out, ArrayList<PointLight> rig, Vec3 head, Vec3 target,
                                      Material pole, Material housing, Material reflector, Vec3 color, float intensity) {
        final float HALF_W = 1.5f, HALF_H = 0.8f, DEPTH = 1.8f, LIGHT_OFF = 0.3f;
        Vec3 f = unit(v(target.x - head.x, target.y - head.y, target.z - head.z));
        Vec3 r = unit(cross3(f, v(0f, 1f, 0f)));
        Vec3 u = cross3(r, f);

        Vec3 back = lin(head, f, -DEPTH / 2);
        Vec3[] reflectorFace = floodCorners(back, r, u, HALF_W, HALF_H);
        Vec3[] shell = floodCorners(lin(back, f, -0.03f), r, u, HALF_W, HALF_H);  // the housing's back, just behind the reflector
        Vec3[] mouth = floodCorners(lin(head, f, DEPTH / 2), r, u, HALF_W, HALF_H);
        addQuad(out, reflectorFace[0], reflectorFace[1], reflectorFace[2], reflectorFace[3], reflector, ORIGIN);
        addQuad(out, shell[0], shell[1], shell[2], shell[3], housing, ORIGIN);
        for (int i = 0; i < 4; i++) {
            int j = (i + 1) % 4;
            addQuad(out, shell[i], shell[j], mouth[j], mouth[i], housing, ORIGIN);
        }
        addAreaLightGrid(rig, lin(back, f, LIGHT_OFF), lin(ORIGIN, r, 0.9f), lin(ORIGIN, u, 0.35f), 3, 2, color, intensity);

        // the mast: from the ground up to just under the lowest corner of the head
        float top = Float.MAX_VALUE;
        for (Vec3 c : shell) top = Math.min(top, c.y);
        for (Vec3 c : mouth) top = Math.min(top, c.y);
        top -= 0.05f;
        final float R0 = 0.30f, R1 = 0.17f;
        final int SEGMENTS = 10;
        for (int j = 0; j < SEGMENTS; j++) {
            double t0 = 2 * Math.PI * j / SEGMENTS, t1 = 2 * Math.PI * (j + 1) / SEGMENTS;
            addQuad(out, v(head.x + R0 * (float) Math.cos(t0), 0f, head.z + R0 * (float) Math.sin(t0)),
                         v(head.x + R0 * (float) Math.cos(t1), 0f, head.z + R0 * (float) Math.sin(t1)),
                         v(head.x + R1 * (float) Math.cos(t1), top, head.z + R1 * (float) Math.sin(t1)),
                         v(head.x + R1 * (float) Math.cos(t0), top, head.z + R1 * (float) Math.sin(t0)), pole, ORIGIN);
        }
    }

    // the four corners of a flood head's rectangle centred on c, in order round it
    private static Vec3[] floodCorners(Vec3 c, Vec3 r, Vec3 u, float halfW, float halfH) {
        return new Vec3[]{
            lin(lin(c, r, -halfW), u, -halfH), lin(lin(c, r, halfW), u, -halfH),
            lin(lin(c, r, halfW), u, halfH), lin(lin(c, r, -halfW), u, halfH)
        };
    }

    /* toView with a heading as well as a pitch: relative to the eye, turned about the vertical by
     * yaw (so the direction the camera looks along lines up with -z), tipped by pitch (positive
     * looks down), offset (in scene units, along the camera's axes) and scaled into world units. */
    private static Vec3 aimView(Vec3 p, Vec3 eye, float yaw, float pitch, float scale, Vec3 off) {
        float x = p.x - eye.x, y = p.y - eye.y, z = p.z - eye.z;
        float cy = (float) Math.cos(yaw), sy = (float) Math.sin(yaw);
        Vec3 q = spin(v(x * cy + z * sy, y, -x * sy + z * cy), pitch, 0f);
        return v((q.x + off.x) * scale, (q.y + off.y) * scale, (q.z + off.z) * scale); // off in scene units, before scaling
    }
}