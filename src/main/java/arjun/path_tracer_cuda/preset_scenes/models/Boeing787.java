package arjun.path_tracer_cuda.preset_scenes.models;

import arjun.path_tracer_cuda.geometry.Material;
import arjun.path_tracer_cuda.geometry.Sphere;
import arjun.path_tracer_cuda.geometry.Triangle;
import arjun.path_tracer_cuda.geometry.Vec3;

import java.util.ArrayList;
import java.util.List;

/**
 * A Boeing 787-9 Dreamliner.
 *
 * Built from published dimensions, and aimed specifically at the features that make a
 * 787 a 787 rather than a generic twinjet:
 *
 *   - raked wingtips instead of winglets, and the pronounced upward curve of a carbon
 *     fibre wing that is still visibly bent while parked
 *   - the chevrons: the saw-toothed trailing edge of the GEnx nacelle
 *   - four pane wraparound flight deck glazing with no eyebrow windows, set into a
 *     pointed, drooping radome
 *   - unusually large cabin windows, set higher up the fuselage than anyone else's
 *   - an inboard trailing edge kink, three flap track fairings per wing, and a long
 *     wing-to-body fairing
 *
 * The windows are real: clear glass in holes through the skin, with a flight deck and
 * a lit economy cabin behind them. The paint is plain white, with no airline scheme.
 *
 * Everything is lofted: a shape is described as cross sections along an axis and
 * consecutive sections are stitched with quads. The fuselage profile is a pair of spline
 * curves - a crown line and a keel line - rather than a radius formula, which is what
 * lets the nose droop and the tail sweep up the way they actually do.
 *
 * WHERE THE SHAPES COME FROM
 *
 * Boeing's 3-view of the 787-9 (D6-58333, drawing 7879, converted from DWG). Almost
 * every curve in this file - the fuselage silhouettes, the nose cross section, the wing
 * and tail planforms, the wing's height and thickness along its span, the nacelle, the
 * belly fairing, every door and window - is measured off that drawing rather than
 * estimated from published figures. Stations are metres aft of the nose tip; heights
 * are metres above the fuselage axis unless they say otherwise.
 *
 * Coordinates are nose-forward: -Z is forward, +X is starboard, +Y is up, tarmac at
 * y = 0. build() rotates and translates, so it can go anywhere at any heading.
 *
 * REFERENCE DIMENSIONS (787-9)
 *   length            62.81 m      wingspan       60.12 m
 *   height            17.02 m      fuselage    5.77 x 5.97 m
 *   quarter chord sweep  32.2 deg  wheelbase      25.85 m
 *   engines    2x GEnx-1B, 2.82 m fan             track  9.80 m
 *   window pitch      0.610 m (24 in)
 */
public class Boeing787 {

    // ================================================================================
    // DIMENSIONS
    // ================================================================================

    public static final double LENGTH = 62.81;

    /* The barrel cross section: 5.754 m wide and 5.956 m tall on the drawing. Not round -
     * about 20 cm taller than it is wide, which is Boeing's published 5.77 x 5.97 to
     * within the drawing's line weight. */
    public static final double HALF_WIDTH = 2.877;
    public static final double HALF_HEIGHT = 2.978;

    /* Fuselage axis above the tarmac at the main gear.
     *
     * This was 5.95, which came from mistaking a dimension leader on the drawing for the
     * ground line. Measured against the real ground line it is 5.10 - and because the
     * wing, engines, windows and doors all hang off the fuselage, that one error had all
     * of them sitting most of a metre too high relative to the gear. */
    public static final double CENTRE_HEIGHT = 5.098;

    /* A 787 does not sit level. Parked, it is pitched 0.8 degrees nose down: the ground
     * line in Boeing's side view slopes at exactly that against the fuselage axis, which
     * puts the nose 0.36 m lower than it would be level. Applied in B.v() as a rotation
     * about the main gear contact patch, so the main wheels stay on the ground and the
     * nose leg is drawn long enough to reach it. */
    private static final double PITCH = Math.toRadians(0.8);

    public static final double SEMI_SPAN = 30.06;

    private static final double NOSE_Z = -LENGTH / 2;

    /* The fuselage itself stops short of the overall length. Its tail cone ends at the
     * APU exhaust 61.99 m from the nose; the published 62.81 is measured to the tips of
     * the tailplane, which sweep back past it. */
    private static final double TAIL_U = 61.99 / LENGTH;

    /* The wing's leading edge, extended in to the centreline, is 1.85 m below the axis.
     * Everything else about the wing's height comes from WING_RISE. */
    private static final double WING_ROOT_Y = CENTRE_HEIGHT - 1.85;

    /* Engine centreline, from the front and plan views, which agree: 9.88 m out and
     * 2.23 m below the fuselage axis, with the inlet lip 20.92 m aft of the nose. It was
     * 10.3 m out and eight metres further aft, hanging off a wing that was itself 6.3 m
     * too far back. */
    private static final double ENGINE_X = 9.88;
    private static final double ENGINE_Y = CENTRE_HEIGHT - 2.225;
    private static final double ENGINE_LIP = 20.92;
    private static final double NACELLE_MAX_R = 1.950;

    /* Gear stations measured off Boeing's 3-view (D6-58333 drawing 7879).
     *
     * The dimension entities in that drawing pin the nose at x = 2569.4 in, the nose
     * gear at 2782.0, the main gear at 3799.8 and the tail at 5041.7 - so the nose gear
     * is 5.40 m aft of the nose and the main gear 31.25 m, giving the published 25.83 m
     * wheelbase. Both were previously inferred from the wheelbase alone and sat about
     * three metres too far aft. */
    private static final double NOSE_GEAR_U = 5.401 / LENGTH;
    private static final double MAIN_GEAR_U = 31.254 / LENGTH;
    private static final double MAIN_GEAR_X = 9.80 / 2;

    // main tyres are 1370 x 533 mm, nose 1143 x 406 mm
    private static final double MAIN_WHEEL_R = 1.370 / 2;
    private static final double MAIN_WHEEL_HALF = 0.533 / 2;
    private static final double NOSE_WHEEL_R = 1.143 / 2;
    private static final double NOSE_WHEEL_HALF = 0.406 / 2;

    /** Stations in metres from the nose, as the fractions of LENGTH that u is measured in. */
    private static double[] stations(double... metres) {
        double[] u = new double[metres.length];
        for (int i = 0; i < u.length; i++) u[i] = metres[i] / LENGTH;
        return u;
    }

    /** Distances in metres out from the centreline, as fractions of the semi span. */
    private static double[] span(double... metres) {
        double[] eta = new double[metres.length];
        for (int i = 0; i < eta.length; i++) eta[i] = metres[i] / SEMI_SPAN;
        return eta;
    }

    // ================================================================================
    // FUSELAGE PROFILE
    // ================================================================================

    /* Three curves describe the whole hull: how wide it is, where its crown is, and
     * where its keel is, along the length. Crown and keel are tracked separately rather
     * than as "centre plus radius" because the interesting parts of an airliner are
     * exactly where those two disagree - the nose, which hangs well below the axis, and
     * the tail, where the keel climbs for rotation clearance while the crown stays high.
     *
     * All three are traced off Boeing's side and plan views. The previous version was
     * drawn by eye, and its nose was far too slender: it reached full width 17 m back
     * from the tip where the real one does so by 10 m, and its tip sat 0.56 m below the
     * axis where the real one hangs 1.28 m below it. That long, pointed nose is what
     * made the front of the aircraft look shallow and generic.
     *
     * The first few points are a rounded cap fitted to the drawing's first section, so
     * the tip closes smoothly to a point. */

    private static final double[] PROFILE_U = stations(
            0.000, 0.003, 0.012, 0.025, 0.051, 0.102, 0.150, 0.200, 0.300, 0.400,
            0.500, 0.600, 0.800, 1.000, 1.200, 1.400, 1.600, 1.800, 2.000, 2.250,
            2.500, 2.750, 3.000, 3.500, 4.000, 4.500, 5.000, 5.500, 6.000, 7.000,
            8.000, 9.000, 10.00, 11.00, 45.68, 46.50, 47.50, 48.50, 49.50, 50.50,
            51.50, 52.50, 53.50, 54.50, 55.50, 56.50, 57.50, 58.50, 59.00, 59.50,
            60.00, 60.50, 61.00, 61.50, 61.99);

    private static final Curve WIDTH = new Curve(PROFILE_U, new double[]{
            0.000, 0.068, 0.136, 0.197, 0.281, 0.355, 0.414, 0.471, 0.569, 0.648,
            0.718, 0.790, 0.911, 1.027, 1.136, 1.234, 1.328, 1.414, 1.495, 1.589,
            1.679, 1.764, 1.845, 1.996, 2.130, 2.262, 2.377, 2.482, 2.572, 2.721,
            2.823, 2.870, 2.877, 2.877, 2.877, 2.863, 2.786, 2.676, 2.552, 2.419,
            2.277, 2.117, 1.900, 1.680, 1.470, 1.300, 1.170, 1.044, 0.935, 0.825,
            0.708, 0.591, 0.472, 0.350, 0.230});

    private static final Curve CROWN = new Curve(PROFILE_U, new double[]{
            -1.280, -1.218, -1.156, -1.101, -1.024, -0.916, -0.853, -0.768, -0.638, -0.522,
            -0.415, -0.313, -0.117,  0.065,  0.232,  0.403,  0.573,  0.743,  0.913,  1.121,
             1.323,  1.499,  1.638,  1.873,  2.066,  2.229,  2.368,  2.487,  2.586,  2.748,
             2.858,  2.926,  2.965,  2.978,  2.978,  2.972,  2.947,  2.910,  2.864,  2.811,
             2.747,  2.662,  2.554,  2.430,  2.291,  2.135,  1.959,  1.771,  1.670,  1.575,
             1.470,  1.370,  1.250,  1.120,  1.000});

    private static final Curve KEEL = new Curve(PROFILE_U, new double[]{
            -1.280, -1.340, -1.399, -1.452, -1.526, -1.588, -1.643, -1.692, -1.772, -1.839,
            -1.902, -1.952, -2.051, -2.135, -2.205, -2.268, -2.324, -2.374, -2.419, -2.472,
            -2.519, -2.563, -2.601, -2.671, -2.733, -2.790, -2.835, -2.870, -2.899, -2.945,
            -2.972, -2.977, -2.978, -2.978, -2.978, -2.907, -2.793, -2.640, -2.478, -2.305,
            -2.122, -1.933, -1.732, -1.515, -1.282, -1.040, -0.783, -0.507, -0.366, -0.222,
            -0.073,  0.078,  0.232,  0.389,  0.544});

    /* The shape of the cross section between those three, as superellipse exponents for
     * the upper and lower halves: 2 is an ellipse, more is boxier, less is pointier.
     *
     * Not a guess. Each flight deck window outline can be located in 3D by combining
     * the three views, and every one of those points lies on the skin - so they say what
     * the section looks like up there. Over the whole glazed area, from 1.4 to 3.5 m
     * back, they fit an exponent of 1.69 with a spread of 0.01: the top of the nose is
     * markedly narrower than an ellipse, which is the ridge where the 787's flat
     * windscreen panes meet at the centre post. Forward of that, the radome joint ring
     * in the front view is a plain ellipse, and by the first door (whose top edge shows
     * in the plan view) the section is an ellipse again. */
    private static final Curve UPPER_SHAPE = new Curve(
            stations(0.00, 1.00, 1.20, 1.45, 3.50, 4.70, 5.90, LENGTH),
            new double[]{2.00, 2.00, 1.92, 1.70, 1.68, 1.85, 2.00, 2.00});

    private static final Curve LOWER_SHAPE = new Curve(
            stations(0.00, 0.60, 3.00, 8.00, LENGTH),
            new double[]{2.00, 2.15, 2.15, 2.00, 2.00});

    /* How far the widest point of the section sits above the midpoint between crown and
     * keel. In the parallel barrel the front view draws the top as a circle of 2.877 m
     * radius centred 0.10 m above the axis, and the bottom fits an ellipse from that same
     * height down to the keel - so the fuselage is widest a little above its middle. The
     * nose sections that the windows and radome ring measure are centred, so the offset
     * only builds up behind the first door. */
    private static final Curve WIDEST = new Curve(
            stations(0.00, 6.50, 10.00, 45.68, 52.00, LENGTH),
            new double[]{0.00, 0.00, 0.10, 0.10, 0.00, 0.00});

    /* The wing-to-body fairing, from the front view (how far it stands proud of the
     * fuselage at each angle round from the crown, in degrees) and the side and plan
     * views (where along the fuselage it starts and stops).
     *
     * Fitted point by point to the front view's outline, which is boxy: a shelf sloping
     * out from the fuselage below the wing root, a vertical wall 3.20 m out where the main
     * gear bays are, a chamfered corner, and a flat bottom only 0.08 m below the keel. The
     * old one hung 1.34 m below the keel, which made the belly look pregnant. */
    private static final Curve FAIRING_SECTION = new Curve(
            new double[]{104, 110, 112, 114, 116, 118, 120, 122, 124, 126, 128, 130, 132, 134, 136,
                         138, 140, 144, 148, 152, 156, 160, 164, 168, 172, 176, 180},
            new double[]{0.000, 0.060, 0.130, 0.254, 0.596, 0.728, 0.819, 0.897, 0.926, 0.884, 0.837,
                         0.866, 0.885, 0.891, 0.886, 0.868, 0.840, 0.745, 0.622, 0.496, 0.382, 0.286,
                         0.210, 0.154, 0.114, 0.091, 0.083});

    private static final Curve FAIRING_LENGTH = new Curve(
            new double[]{19.2, 20.0, 21.0, 22.0, 23.0, 24.0, 33.5, 34.3, 35.0, 35.8, 36.6, 37.2},
            new double[]{0.00, 0.35, 0.65, 0.85, 0.97, 1.00, 1.00, 0.85, 0.55, 0.25, 0.05, 0.00});

    // ================================================================================
    // WING
    // ================================================================================

    /* The planform, as the stations of the leading and trailing edges against distance
     * out from the centreline, traced off the plan view. Integrated, it gives 375.4 m2
     * against the published 377.
     *
     * What the trace shows and the old chord formula did not: the leading edge sweeps
     * at a steady 34.7 degrees and then curves progressively back through the raked tip
     * rather than breaking to a second straight line; the inboard trailing edge runs
     * almost straight across to the kink at 10 m; and the tip itself narrows to 0.4 m,
     * not the 1.75 m that published tables give - that figure is the tip of the
     * reference trapezoid, not of the real wing.
     *
     * The wing also sat 6.3 m too far aft. Its leading edge met the fuselage 28.3 m from
     * the nose; on the drawing it is 21.9. */
    private static final Curve WING_LE = new Curve(
            span(0, 4, 10, 16, 22, 25, 26, 27, 28, 28.5, 29, 29.5, 29.8, 30.0, SEMI_SPAN),
            new double[]{19.894, 22.664, 26.794, 30.925, 35.086, 37.185, 37.884, 38.619,
                         39.597, 40.202, 40.894, 41.700, 42.267, 42.640, 42.980});

    private static final Curve WING_TE = new Curve(
            span(0, 3.2, 6, 9, 10, 11, 12, 14, 18, 22, 26, 28, 29, 29.5, 29.8, 30.0, SEMI_SPAN),
            new double[]{33.527, 33.719, 33.858, 34.009, 34.120, 34.318, 34.579, 35.342, 37.016,
                         38.708, 40.473, 41.461, 42.214, 42.720, 43.062, 43.300, 43.360});

    /* Height of the leading edge above WING_ROOT_Y, and thickness to chord, along the
     * span. Both come from the front view: at each station the drawing gives the top and
     * bottom of the wing, and the section that fills exactly that envelope - with this
     * file's own twist and camber - fixes both numbers.
     *
     * The result is a leading edge climbing at a nearly constant 7.5 degrees with a
     * slight curl at the tip, from a root 0.2 m higher than before, and a wing thinner
     * than it was everywhere outboard - 8.5% at mid span against the old 11%. */
    private static final Curve WING_RISE = new Curve(
            span(0, 3.2, 4, 6, 8, 10, 12, 14, 16, 18, 20, 22, 24, 26, 28, 29, 29.5, 29.9, SEMI_SPAN),
            new double[]{0.000, 0.494, 0.618, 0.926, 1.214, 1.476, 1.756, 2.039, 2.311, 2.575,
                         2.837, 3.092, 3.347, 3.605, 3.885, 4.066, 4.198, 4.350, 4.420});

    private static final Curve WING_THICKNESS = new Curve(
            span(0, 3.2, 4, 6, 8, 10, 12, 14, 16, 18, 24, 28, SEMI_SPAN),
            new double[]{0.150, 0.138, 0.134, 0.123, 0.112, 0.104, 0.095, 0.089, 0.086, 0.085,
                         0.086, 0.090, 0.095});

    // ================================================================================
    // TAIL
    // ================================================================================

    /* Tailplane planform from the plan view, edge stations against metres out from the
     * centreline. It was already close; this mostly adds the leading edge fillet at the
     * root and the rounded tip. */
    private static final double TAIL_SEMI_SPAN = 9.91;

    private static final Curve TAIL_LE = new Curve(
            new double[]{0.0, 1.0, 1.9, 2.5, 3.0, 5.0, 7.0, 9.0, 9.5, 9.8, 9.88, TAIL_SEMI_SPAN},
            new double[]{53.00, 53.54, 54.08, 54.89, 55.32, 57.02, 58.71, 60.41, 60.89, 61.66, 62.30, 62.74});

    private static final Curve TAIL_TE = new Curve(
            new double[]{0.0, 1.0, 1.9, 2.5, 3.0, 5.0, 7.0, 9.0, 9.5, 9.8, TAIL_SEMI_SPAN},
            new double[]{59.50, 59.60, 59.75, 59.96, 60.15, 60.88, 61.61, 62.38, 62.61, 62.75, 62.80});

    /* The fin, from the side view: leading and trailing edge stations against height
     * above the axis. The loft starts at 1.6 m, buried in the tail cone, and the curve at
     * the bottom of the leading edge is the dorsal fillet blending it into the crown.
     *
     * This replaces a fin that stood five metres too far forward with a separate long
     * dorsal fin in front of it. The real one has no such thing - just the fillet. */
    private static final double FIN_ROOT_H = 1.60;
    private static final double FIN_TIP_H = 11.57;

    private static final Curve FIN_LE = new Curve(
            new double[]{1.60, 2.90, 3.20, 3.60, 4.00, 5.00, 7.00, 9.00, 10.00, 11.00, 11.30, 11.50, FIN_TIP_H},
            new double[]{48.10, 49.33, 50.74, 51.48, 52.02, 53.09, 55.07, 57.05, 58.04, 59.10, 59.72, 61.00, 61.90});

    private static final Curve FIN_TE = new Curve(
            new double[]{1.60, 1.90, 2.50, 3.00, 4.00, 6.00, 8.00, 10.00, 11.00, 11.50, FIN_TIP_H},
            new double[]{58.05, 58.15, 58.38, 58.58, 58.96, 59.73, 60.50, 61.29, 61.72, 61.96, 61.99});

    // ================================================================================
    // DOORS AND WINDOWS
    // ================================================================================

    /* Passenger doors: centre stations off the side view. All four are 1.07 m wide and
     * 1.88 m tall with the sill 0.36 m below the axis - so the cabin floor is far closer
     * to the axis than the "about a metre below" this file used to assume, and doors and
     * windows both sat over half a metre too low. */
    private static final double[] DOOR_STATIONS = {6.303, 18.371, 35.440, 49.632};
    private static final double DOOR_SILL = -0.361, DOOR_TOP = 1.518, DOOR_WIDTH = 1.067;

    /* Flight deck windows, traced off the drawing.
     *
     * Each row is a corner - station, distance out from the centreline, height above the
     * axis - and then how far along each edge its rounding reaches. Corners and radii are
     * a least-squares fit to Boeing's outline of each window in all three views at once,
     * side, plan and front: the side window lands within 3 mm of the drawing on average,
     * the windscreen within 1.5 cm.
     *
     * The radii are large - up to 27 cm - and that is most of the look. The first version
     * of these outlines rounded each corner by 5 to 10 cm, which on glass this size reads
     * as a sharp-cornered trapezoid; the real windows are soft, almost pebble-like.
     *
     * The old panes were placed by eye from photographs and were about 1.5 m too far
     * aft, too low, and the wrong shape. The windscreen is a raked quadrilateral either
     * side of a 15 cm centre post. The side window is the distinctive one: its bottom
     * edge dips to its lowest two thirds of the way back and then sweeps up into a
     * rounded point at the aft end, where it meets a diagonal running up and forward to
     * the top edge. */
    private static final double[][] WINDSCREEN = {
            {1.380, 0.079, 0.400, 0.267},
            {1.975, 0.926, 0.445, 0.224},
            {2.675, 0.857, 1.094, 0.151},
            {2.095, 0.081, 0.993, 0.084},
    };

    private static final double[][] SIDE_WINDOW = {
            {2.805, 0.953, 1.090, 0.130},
            {2.147, 1.025, 0.465, 0.229},
            {2.992, 1.561, 0.385, 0.116},
            {3.427, 1.638, 0.569, 0.094},
            {3.313, 1.344, 1.022, 0.099},
    };

    // ================================================================================
    // DETAIL LEVEL
    // ================================================================================

    /**
     * 1 is a coarse preview, 4 is the default, 6 or more is for close-ups.
     *
     * Raising it barely affects render speed: a BVH costs about log(n) per ray, so ten
     * times the triangles is three or four extra levels of tree to walk. What it does
     * cost is build time and memory, both roughly linear.
     */
    public int quality = 4;

    /** Cabin, flight deck dome light: on for a dusk scene, off for a parked, dark aircraft. */
    public boolean cabinLightsOn = true;

    /* Exterior lights, set as they would be at the gate at dusk: position lights and
     * beacons on, landing and taxi lights and strobes off. Switch them for a taxiing or
     * departing aircraft. */
    public boolean navLightsOn = true;        // red, green and white position lights
    public boolean beaconsOn = true;          // red anti-collision lights, top and bottom
    public boolean landingLightsOn = true;   // landing, taxi and runway turnoff lights
    public boolean strobesOn = true;         // white wingtip strobes
    public boolean gearDown = true;

    private int fusStations, fusSegments, wingStations, airfoilPoints, tailStations;
    private int nacelleSegments, cylinderSegments, wheelSegments;

    private void resolveQuality() {
        int q = Math.max(1, quality);
        fusStations = 90 + 70 * q;
        fusSegments = 32 + 20 * q;
        wingStations = 18 + 16 * q;
        airfoilPoints = 24 + 14 * q;
        tailStations = 10 + 8 * q;
        nacelleSegments = 28 + 16 * q;
        cylinderSegments = 10 + 5 * q;
        wheelSegments = 14 + 8 * q;
    }

    // ================================================================================
    // MATERIALS
    // ================================================================================

    /** Every material the aircraft uses. Swap any of them to change its finish.
     *
     * PORT NOTE: the CPU renderer this came from gives some of these an emission (cabin and
     * flight deck lighting, displays, the nav lights, beacons, strobes and landing lights).
     * The CUDA kernel has no emission yet, so here every one of them is an ordinary surface in
     * its lens / diffuser colour, and the aircraft is lit only by the scene's lights. */
    public static class Materials {
        /* Plain white paint all over - no airline scheme. Airliners are painted composite
         * and aluminium, not bare metal, so this is mostly dielectric with a little sheen.
         * A fully metallic fuselage would take only the specular path and mirror the sky,
         * which reads as chrome, not paint. */
        public Material white      = new Material(new Vec3(247, 247, 249), 0.08f, 0.17f, 0);
        public Material panelLine  = new Material(new Vec3(150, 153, 158), 0.15f, 0.45f, 0);
        public Material darkSeam   = new Material(new Vec3(74, 76, 82), 0.20f, 0.50f, 0);

        public Material wingUpper  = new Material(new Vec3(222, 225, 230), 0.26f, 0.20f, 0);
        public Material wingLower  = new Material(new Vec3(190, 195, 202), 0.26f, 0.26f, 0);
        public Material wingSeam   = new Material(new Vec3(132, 136, 142), 0.20f, 0.42f, 0);

        public Material nacelle     = new Material(new Vec3(242, 242, 245), 0.10f, 0.15f, 0);
        public Material nacelleLip  = new Material(new Vec3(214, 219, 226), 0.92f, 0.05f, 0);
        public Material intake      = new Material(new Vec3(38, 40, 44), 0.18f, 0.55f, 0);
        public Material fanBlade    = new Material(new Vec3(142, 148, 156), 0.88f, 0.14f, 0);
        public Material spinner     = new Material(new Vec3(228, 230, 234), 0.60f, 0.10f, 0);
        public Material spinnerMark = new Material(new Vec3(30, 30, 34), 0.30f, 0.30f, 0);
        public Material exhaust     = new Material(new Vec3(86, 80, 74), 0.72f, 0.32f, 0);
        public Material hotMetal    = new Material(new Vec3(120, 106, 94), 0.80f, 0.28f, 0);
        public Material pylon       = new Material(new Vec3(238, 239, 242), 0.12f, 0.20f, 0);

        public Material gearStrut = new Material(new Vec3(178, 184, 192), 0.86f, 0.14f, 0);
        public Material gearDark  = new Material(new Vec3(96, 100, 106), 0.70f, 0.30f, 0);
        public Material tyre      = new Material(new Vec3(24, 24, 26), 0.00f, 0.86f, 0);
        public Material hub       = new Material(new Vec3(172, 174, 178), 0.82f, 0.20f, 0);
        public Material brake     = new Material(new Vec3(92, 78, 68), 0.65f, 0.42f, 0);
        /** The sliding tubes of the shock struts and actuators (added in the port). */
        public Material chrome    = new Material(new Vec3(222, 226, 232), 1.0f, 0.03f, 0);

        /* Glazing, clear: there is a flight deck and a cabin behind it, so it can be what
         * glass is. Two sheets per window - see glaze() - with a faint tint and a perfectly
         * smooth surface. */
        public Material cockpitGlass = new Material(new Vec3(228, 238, 236), 0.0f, 0.0f, 1.0f, 1.52f, 0.6f);
        public Material windowGlass  = new Material(new Vec3(236, 240, 240), 0.0f, 0.0f, 1.0f, 1.49f, 0.6f);
        public Material frame        = new Material(new Vec3(58, 60, 66), 0.35f, 0.35f, 0);
        public Material antenna      = new Material(new Vec3(236, 236, 240), 0.10f, 0.25f, 0);
        public Material probe        = new Material(new Vec3(150, 150, 156), 0.85f, 0.20f, 0);

        // the cabin
        public Material cabinLining  = new Material(new Vec3(234, 232, 226), 0.0f, 0.55f, 0);
        public Material windowReveal = new Material(new Vec3(226, 224, 218), 0.0f, 0.50f, 0);
        public Material binPlastic   = new Material(new Vec3(238, 236, 230), 0.0f, 0.40f, 0);
        public Material carpet       = new Material(new Vec3(70, 74, 86), 0.0f, 0.90f, 0);
        public Material seatFabric   = new Material(new Vec3(58, 66, 86), 0.0f, 0.85f, 0);
        public Material cabinLight   = new Material(new Vec3(255, 246, 228), 0, 0.5f, 0);  // was emissive

        // the flight deck
        public Material flightDeck   = new Material(new Vec3(150, 153, 158), 0.05f, 0.60f, 0);
        public Material glareshield  = new Material(new Vec3(30, 31, 34), 0.0f, 0.80f, 0);
        public Material pilotSeat    = new Material(new Vec3(72, 72, 76), 0.05f, 0.50f, 0);
        public Material display      = new Material(new Vec3(30, 60, 110), 0, 0.15f, 0);    // was emissive
        public Material displayDark  = new Material(new Vec3(20, 40, 30), 0, 0.15f, 0);     // was emissive
        public Material domeLight    = new Material(new Vec3(255, 244, 226), 0, 0.5f, 0);   // was emissive
        public Material floodLight   = new Material(new Vec3(255, 244, 226), 0, 0.5f, 0);   // was emissive
        public Material panelLight   = new Material(new Vec3(196, 200, 206), 0.05f, 0.5f, 0); // was emissive

        // exterior lights - all were emissive; now just their lens colours
        public Material navRed    = new Material(new Vec3(255, 40, 40), 0, 0.4f, 0);
        public Material navGreen  = new Material(new Vec3(40, 255, 70), 0, 0.4f, 0);
        public Material navWhite  = new Material(new Vec3(255, 255, 255), 0, 0.4f, 0);
        public Material strobe    = new Material(new Vec3(255, 255, 255), 0, 0.4f, 0);
        /** A beacon's red lens. */
        public Material beaconRed = new Material(new Vec3(255, 50, 40), 0, 0.4f, 0);
        public Material landing   = new Material(new Vec3(255, 250, 235), 0, 0.4f, 0);
        /** A light that is switched off: a dark, glossy lens over its reflector. */
        public Material lensOff   = new Material(new Vec3(40, 44, 50), 0.55f, 0.06f, 0);
    }

    public Materials materials = new Materials();

    // ================================================================================
    // BUILD
    // ================================================================================

    public void build(List<Triangle> triangles, List<Sphere> spheres, double x, double y, double z, double headingDegrees) {
        resolveQuality();
        B b = new B(triangles, spheres, new P(x, y, z), Math.toRadians(headingDegrees), PITCH, NOSE_Z + MAIN_GEAR_U * LENGTH);

        collectOpenings();
        buildFuselage(b);
        buildWindows(b);
        buildDoors(b);
        buildInterior(b);

        buildWing(b, +1);
        buildWing(b, -1);
        buildWingBodyFairing(b);

        buildHorizontalStabiliser(b, +1);
        buildHorizontalStabiliser(b, -1);
        buildVerticalStabiliser(b);

        buildEngine(b, +1);
        buildEngine(b, -1);

        if (gearDown) {
            buildNoseGear(b);
            buildMainGear(b, +1);
            buildMainGear(b, -1);
        }

        buildAntennas(b);
        buildLights(b);
    }

    // ================================================================================
    // FUSELAGE
    // ================================================================================

    /**
     * A point on the fuselage skin.
     *
     * u runs 0 at the nose to 1 at the tail; theta is the angle round from the crown,
     * positive towards starboard; swell pushes the point out along the surface, which is
     * how everything that sits proud of or recessed into the skin gets placed.
     */
    private P fus(B b, double u, double theta, double swell) {
        double[] p = new double[2];
        section(u, theta, swell, p);
        return b.v(p[0], CENTRE_HEIGHT + p[1], NOSE_Z + u * LENGTH);
    }

    /** The same point as fus(), in the section's own terms: {lateral, height above the axis}. */
    private static void section(double u, double theta, double swell, double[] out) {
        double w = WIDTH.at(u), top = CROWN.at(u), bot = KEEL.at(u);
        double cy = (top + bot) * 0.5 + WIDEST.at(u);

        // a superellipse each side of the widest point: raising sin and cos to 2/n
        // keeps x^n + y^n = 1 on the outline
        double c = Math.cos(theta), s = Math.sin(theta);
        double half = c >= 0 ? top - cy : cy - bot;
        double e = 2.0 / (c >= 0 ? UPPER_SHAPE.at(u) : LOWER_SHAPE.at(u));
        double sx = Math.copySign(Math.pow(Math.abs(s), e), s);
        double cz = Math.copySign(Math.pow(Math.abs(c), e), c);

        out[0] = (w + swell) * sx;
        out[1] = cy + (half + swell) * cz;
    }

    /** The angle round from the crown at which the skin at station u reaches a height above the axis. */
    private static double thetaAtHeight(double u, double height) {
        double top = CROWN.at(u), bot = KEEL.at(u);
        double cy = (top + bot) * 0.5 + WIDEST.at(u);
        double half = height >= cy ? top - cy : cy - bot;
        double y = Math.max(-1, Math.min(1, (height - cy) / half));
        double n = y >= 0 ? UPPER_SHAPE.at(u) : LOWER_SHAPE.at(u);
        return Math.acos(Math.copySign(Math.pow(Math.abs(y), n / 2), y));
    }

    /**
     * The angle round from the crown of the skin point nearest a measured one, given as
     * distance out from the centreline and height above the axis. fus() inverted.
     */
    private static double thetaAt(double u, double lateral, double height) {
        double w = WIDTH.at(u), top = CROWN.at(u), bot = KEEL.at(u);
        double cy = (top + bot) * 0.5 + WIDEST.at(u);
        double dy = height - cy;
        double half = dy >= 0 ? top - cy : cy - bot;
        double n = dy >= 0 ? UPPER_SHAPE.at(u) : LOWER_SHAPE.at(u);
        double s = Math.pow(Math.min(1, Math.abs(lateral) / w), n / 2);
        double c = Math.pow(Math.min(1, Math.abs(dy) / half), n / 2);
        return Math.atan2(s, Math.copySign(c, dy));
    }

    /** How far out the inside of the skin is at station s, at a height above the axis. */
    private static double halfWidthAt(double s, double height) {
        double u = s / LENGTH;
        double[] p = new double[2];
        section(u, thetaAtHeight(u, height), 0, p);
        return p[0];
    }

    /**
     * Station positions along the fuselage, bunched where the shape is doing something.
     *
     * An even spread wastes most of its stations on the thirty-odd metres of parallel
     * tube in the middle, where two would do, and then renders the nose as a faceted cone.
     */
    private double[] fuselageStations() {
        int nose = (int) (fusStations * 0.40);
        int barrel = (int) (fusStations * 0.14);
        int tail = fusStations - nose - barrel;

        double noseEnd = 11.5 / LENGTH, tailStart = 45.5 / LENGTH;
        double[] u = new double[nose + barrel + tail + 1];
        int k = 0;

        // cosine spacing, so stations crowd towards the tip
        for (int i = 0; i < nose; i++) {
            u[k++] = noseEnd * (1 - Math.cos(((double) i / nose) * Math.PI * 0.5));
        }
        for (int i = 0; i < barrel; i++) {
            u[k++] = noseEnd + (tailStart - noseEnd) * ((double) i / barrel);
        }
        for (int i = 0; i <= tail; i++) {
            u[k++] = tailStart + (TAIL_U - tailStart) * Math.sin(((double) i / tail) * Math.PI * 0.5);
        }
        return u;
    }

    private static double normaliseAngle(double theta) {
        while (theta > Math.PI) theta -= 2 * Math.PI;
        while (theta < -Math.PI) theta += 2 * Math.PI;
        return theta;
    }

    private void buildFuselage(B b) {
        double[] u = fuselageStations();
        int segments = fusSegments;

        // the first ring collapses to the point of the nose
        for (int j = 0; j < segments; j++) {
            double[] a = cellAngles(j, segments);
            b.tri(fus(b, u[0], a[0], 0), fus(b, u[1], a[1], 0), fus(b, u[1], a[0], 0), materials.white);
        }
        skinGrid(b, u, 1, 0, materials.white, (i, j) -> true);

        // flat disc closing the tail cone, where the APU exhausts
        P centre = b.v(0, CENTRE_HEIGHT + (CROWN.at(TAIL_U) + KEEL.at(TAIL_U)) * 0.5, NOSE_Z + TAIL_U * LENGTH);
        for (int j = 0; j < segments; j++) {
            double t0 = 2 * Math.PI * j / segments, t1 = 2 * Math.PI * (j + 1) / segments;
            b.tri(centre, fus(b, TAIL_U, t1, 0), fus(b, TAIL_U, t0, 0), materials.exhaust);
        }
    }

    // ================================================================================
    // OPENINGS
    // ================================================================================

    /**
     * A hole through the skin where a window is.
     *
     * Held as a convex outline in (u, theta) - the coordinates fus() takes - because that
     * is the space the skin is built in, and cutting a convex hole out of each quad of it
     * is then just a few half-plane clips (see cutCell). The outline is the convex hull of
     * the window shape mapped onto the skin; every window here is convex to begin with.
     */
    private static final class Opening {
        final double[] u, t;          // anticlockwise, with points every couple of centimetres
        final double[] cutU, cutT;    // the same outline with only its corners, for cutting the skin
        final double uMin, uMax, tMin, tMax;
        final Material glass, reveal;
        final double depth;           // how far in the reveal runs, metres

        Opening(List<double[]> points, Material glass, Material reveal, double depth, double step) {
            /* The skin is cut with the bare hull: points added along a straight edge are
             * in line with each other, and clipping against a run of in-line edges only
             * produces zero-area slivers, which on a curved skin turn into hair-thin fins
             * standing out of the surface and casting specks of shadow. */
            double[][] hull = convexHull(points);
            cutU = new double[hull.length];
            cutT = new double[hull.length];
            for (int i = 0; i < hull.length; i++) { cutU[i] = hull[i][0]; cutT[i] = hull[i][1]; }

            double[][] dense = densify(hull, step);
            u = new double[dense.length];
            t = new double[dense.length];
            double a = 1e9, bb = -1e9, c = 1e9, d = -1e9;
            for (int i = 0; i < dense.length; i++) {
                u[i] = dense[i][0];
                t[i] = dense[i][1];
                a = Math.min(a, u[i]); bb = Math.max(bb, u[i]);
                c = Math.min(c, t[i]); d = Math.max(d, t[i]);
            }
            uMin = a; uMax = bb; tMin = c; tMax = d;
            this.glass = glass;
            this.reveal = reveal;
            this.depth = depth;
        }
    }

    private final List<Opening> openings = new ArrayList<>();

    /**
     * Put extra points along any long edge of an outline in (u, theta), so no edge spans
     * more than about step metres of skin.
     *
     * The hull keeps only the corners of a straight run, and on a flight deck window a
     * straight run can be 80 cm long. Everything built from the outline - the glass, the
     * reveal, the seal - joins its points with straight lines in 3D, and across 80 cm of
     * a nose this curved such a line sags 5 cm below the skin: the hole in the skin, which
     * follows the surface, then opens up beyond the glass and you can see in round the
     * edge. Short edges follow the surface closely enough that the gap closes.
     */
    private static double[][] densify(double[][] hull, double step) {
        List<double[]> out = new ArrayList<>();
        int n = hull.length;
        for (int i = 0; i < n; i++) {
            double[] a = hull[i], b = hull[(i + 1) % n];
            double metresPerRadian = WIDTH.at((a[0] + b[0]) / 2);
            double len = Math.hypot((b[0] - a[0]) * LENGTH, (b[1] - a[1]) * metresPerRadian);
            int m = Math.max(1, (int) Math.ceil(len / step));
            for (int j = 0; j < m; j++) {
                double t = (double) j / m;
                out.add(new double[]{lerp(a[0], b[0], t), lerp(a[1], b[1], t)});
            }
        }
        return out.toArray(new double[0][]);
    }

    /** Convex hull, anticlockwise, by Andrew's monotone chain. */
    private static double[][] convexHull(List<double[]> points) {
        List<double[]> p = new ArrayList<>(points);
        p.sort((x, y) -> x[0] != y[0] ? Double.compare(x[0], y[0]) : Double.compare(x[1], y[1]));
        int n = p.size(), k = 0;
        double[][] h = new double[2 * n][];
        for (int i = 0; i < n; i++) {
            while (k >= 2 && cross(h[k - 2], h[k - 1], p.get(i)) <= 0) k--;
            h[k++] = p.get(i);
        }
        for (int i = n - 2, lower = k + 1; i >= 0; i--) {
            while (k >= lower && cross(h[k - 2], h[k - 1], p.get(i)) <= 0) k--;
            h[k++] = p.get(i);
        }
        return java.util.Arrays.copyOf(h, k - 1);
    }

    private static double cross(double[] o, double[] a, double[] b) {
        return (a[0] - o[0]) * (b[1] - o[1]) - (a[1] - o[1]) * (b[0] - o[0]);
    }

    /**
     * Every window, as a hole through the skin, on both sides.
     *
     * Shapes are Boeing's: the cabin window is a rounded rectangle 0.273 by 0.468 m whose
     * corners are elliptical, 0.10 m round the side and 0.14 m up it, so only the middle
     * of each side is straight - traced off the side view, where the old superellipse
     * was visibly too round. The flight deck windows are WINDSCREEN and SIDE_WINDOW.
     */
    private void collectOpenings() {
        openings.clear();

        for (double s : windowStations()) {
            double[][] outline = roundedRect(s, WINDOW_CENTRE, 0.1364, 0.2342, 0.102, 0.142, 6);
            for (int side = -1; side <= 1; side += 2) {
                List<double[]> pts = new ArrayList<>();
                for (double[] p : outline) {
                    double u = p[0] / LENGTH;
                    pts.add(new double[]{u, side * thetaAtHeight(u, p[1])});
                }
                // small, on the flat side of the barrel: a coarse outline follows the skin fine
                openings.add(new Opening(pts, materials.windowGlass, materials.windowReveal, 0.11, 0.06));
            }
        }

        for (double[][] pane : new double[][][]{WINDSCREEN, SIDE_WINDOW}) {
            double[][] outline = roundedOutline(pane, 0.025);
            for (int side = -1; side <= 1; side += 2) {
                List<double[]> pts = new ArrayList<>();
                for (double[] p : outline) {
                    double u = p[0] / LENGTH;
                    pts.add(new double[]{u, side * thetaAt(u, p[1], p[2])});
                }
                // big, on the tightly curved nose: points every two centimetres
                openings.add(new Opening(pts, materials.cockpitGlass, materials.frame, 0.07, 0.02));
            }
        }
    }

    /** Which cells of a skin grid to build: row i (between stations), column j (round the section). */
    private interface CellFilter {
        boolean include(int i, int j);
    }

    /**
     * The angles bounding column j of a skin grid, in (-pi, pi] - the range openings are
     * held in.
     */
    private static double[] cellAngles(int j, int segments) {
        double t0 = 2 * Math.PI * j / segments, t1 = 2 * Math.PI * (j + 1) / segments;
        if (t0 >= Math.PI - 1e-12) { t0 -= 2 * Math.PI; t1 -= 2 * Math.PI; }
        return new double[]{t0, t1};
    }

    /**
     * A grid of skin cells - between consecutive stations in us, and round the section in
     * fusSegments columns - with the windows cut out of it.
     *
     * A cell no window touches is a quad, as it always was. One a window overlaps is
     * replaced by the exact shape of the cell minus the window, traced from the cell's own
     * corners and the points where the window outline really crosses it (cellMinus), and
     * ear-clipped into triangles. Because the only new points on a cell's edges are real
     * crossings of the outline, the cell next door, which the outline also crosses, puts a
     * point in exactly the same place, and the two meet without a crack.
     */
    private void skinGrid(B b, double[] us, int firstRow, double swell, Material m, CellFilter filter) {
        int segments = fusSegments;
        for (int i = firstRow; i < us.length - 1; i++) {
            for (int j = 0; j < segments; j++) {
                if (!filter.include(i, j)) continue;
                double u0 = us[i], u1 = us[i + 1];
                double[] a = cellAngles(j, segments);

                List<double[][]> tris = cutCell(u0, u1, a[0], a[1]);
                if (tris == null) {
                    b.quad(fus(b, u0, a[0], swell), fus(b, u0, a[1], swell),
                           fus(b, u1, a[1], swell), fus(b, u1, a[0], swell), m);
                    continue;
                }
                for (double[][] t : tris) {
                    b.tri(fus(b, t[0][0], t[0][1], swell), fus(b, t[1][0], t[1][1], swell),
                          fus(b, t[2][0], t[2][1], swell), m);
                }
            }
        }
    }

    /**
     * The parts of a skin cell outside every window, as triangles in (u, theta), or null if
     * no window touches the cell at all.
     */
    private List<double[][]> cutCell(double u0, double u1, double a0, double a1) {
        List<List<double[]>> regions = null;
        for (Opening o : openings) {
            if (o.uMax <= u0 || o.uMin >= u1 || o.tMax <= a0 || o.tMin >= a1) continue;
            if (regions == null) {
                regions = new ArrayList<>();
                List<double[]> cell = new ArrayList<>();
                cell.add(new double[]{u0, a0}); cell.add(new double[]{u1, a0});
                cell.add(new double[]{u1, a1}); cell.add(new double[]{u0, a1});
                regions.add(cell);
            }
            List<List<double[]>> next = new ArrayList<>();
            for (List<double[]> r : regions) {
                List<List<double[]>> cut = cellMinus(r, o);
                if (cut == null) return decomposed(u0, u1, a0, a1);   // a case the tracer does not handle
                next.addAll(cut);
            }
            regions = next;
        }
        if (regions == null) return null;

        List<double[][]> tris = new ArrayList<>();
        for (List<double[]> r : regions) {
            if (!earClip(r, tris)) return decomposed(u0, u1, a0, a1);
        }
        return tris;
    }

    /**
     * A simple polygon minus a convex opening, by tracing the boundary of what is left:
     * along the polygon wherever it is outside the opening, and back along the opening's
     * outline wherever that is inside the polygon. Both anticlockwise in (u, theta).
     *
     * Returns the pieces left over (none if the opening covers the polygon), or null for
     * the one arrangement it does not handle - the opening entirely inside the polygon,
     * which would leave a hole. Windows are taller than a cell, so that does not arise.
     */
    private static List<List<double[]>> cellMinus(List<double[]> r, Opening o) {
        int nr = r.size(), np = o.cutU.length;
        List<List<double[]>> out = new ArrayList<>();

        // every crossing of the polygon's edges with the opening's
        List<double[]> crossings = new ArrayList<>();   // {edge of r, s along it, edge of p, t along it, u, theta, entering}
        for (int i = 0; i < nr; i++) {
            double[] p0 = r.get(i), p1 = r.get((i + 1) % nr);
            for (int k = 0; k < np; k++) {
                int k2 = (k + 1) % np;
                double qu = o.cutU[k], qt = o.cutT[k], du = o.cutU[k2] - qu, dt = o.cutT[k2] - qt;
                double eu = p1[0] - p0[0], et = p1[1] - p0[1];
                double den = eu * dt - et * du;
                if (den == 0) continue;
                double s = ((qu - p0[0]) * dt - (qt - p0[1]) * du) / den;
                double t = ((qu - p0[0]) * et - (qt - p0[1]) * eu) / den;
                if (s < 0 || s >= 1 || t < 0 || t >= 1) continue;
                // entering the opening if the polygon's edge heads to the left of the opening's
                double entering = (du * et - dt * eu) > 0 ? 1 : 0;
                crossings.add(new double[]{i, s, k, t, p0[0] + eu * s, p0[1] + et * s, entering});
            }
        }

        if (crossings.isEmpty()) {
            double[] first = r.get(0);
            if (insideConvex(o, first[0], first[1])) return out;                 // swallowed whole
            if (insidePolygon(r, o.cutU[0], o.cutT[0])) return null;             // would leave a hole
            out.add(r);                                                          // untouched
            return out;
        }
        if (crossings.size() % 2 != 0) return null;

        // order along the polygon, and along the opening
        int n = crossings.size();
        Integer[] alongR = new Integer[n], alongP = new Integer[n];
        for (int i = 0; i < n; i++) { alongR[i] = i; alongP[i] = i; }
        java.util.Arrays.sort(alongR, (x, y) -> {
            double[] a = crossings.get(x), b = crossings.get(y);
            return a[0] != b[0] ? Double.compare(a[0], b[0]) : Double.compare(a[1], b[1]);
        });
        java.util.Arrays.sort(alongP, (x, y) -> {
            double[] a = crossings.get(x), b = crossings.get(y);
            return a[2] != b[2] ? Double.compare(a[2], b[2]) : Double.compare(a[3], b[3]);
        });
        int[] rankR = new int[n], rankP = new int[n];
        for (int i = 0; i < n; i++) { rankR[alongR[i]] = i; rankP[alongP[i]] = i; }

        boolean[] used = new boolean[n];
        for (int start = 0; start < n; start++) {
            if (used[start] || crossings.get(start)[6] != 0) continue;   // begin where the polygon leaves the opening
            List<double[]> piece = new ArrayList<>();
            int at = start, guard = 0;
            do {
                if (++guard > 4 * n + 4) return null;
                double[] x = crossings.get(at);
                if (x[6] != 0) return null;
                used[at] = true;
                piece.add(new double[]{x[4], x[5]});

                // along the polygon to the next crossing, which must be an entry
                int nextR = alongR[(rankR[at] + 1) % n];
                double[] y = crossings.get(nextR);
                int from = (int) x[0], to = (int) y[0];
                boolean wrap = to < from || (to == from && y[1] <= x[1]);
                for (int v = from + 1; ; v++) {
                    int idx = v % nr;
                    if (!wrap && v > to) break;
                    if (wrap && v > to + nr) break;
                    piece.add(r.get(idx));
                }
                if (y[6] == 0) return null;
                used[nextR] = true;
                piece.add(new double[]{y[4], y[5]});

                // back along the opening's outline to the previous crossing, which leaves again
                int prevP = alongP[(rankP[nextR] - 1 + n) % n];
                double[] z = crossings.get(prevP);
                int pf = (int) y[2], pt = (int) z[2];
                boolean pwrap = pt > pf || (pt == pf && z[3] >= y[3]);
                for (int v = pf; ; v--) {
                    int idx = ((v % np) + np) % np;
                    if (!pwrap && v <= pt) break;
                    if (pwrap && v <= pt - np) break;
                    piece.add(new double[]{o.cutU[idx], o.cutT[idx]});
                }
                at = prevP;
            } while (at != start);
            out.add(piece);
        }
        return out;
    }

    private static boolean insideConvex(Opening o, double u, double t) {
        int n = o.cutU.length;
        for (int k = 0; k < n; k++) {
            int k2 = (k + 1) % n;
            if ((o.cutU[k2] - o.cutU[k]) * (t - o.cutT[k]) - (o.cutT[k2] - o.cutT[k]) * (u - o.cutU[k]) < 0) return false;
        }
        return true;
    }

    private static boolean insidePolygon(List<double[]> r, double u, double t) {
        boolean in = false;
        for (int i = 0, j = r.size() - 1; i < r.size(); j = i++) {
            double[] a = r.get(i), b = r.get(j);
            if ((a[1] > t) != (b[1] > t) && u < (b[0] - a[0]) * (t - a[1]) / (b[1] - a[1]) + a[0]) in = !in;
        }
        return in;
    }

    /**
     * Ear clipping: split a simple anticlockwise polygon into triangles using only its own
     * corners. Adds them to out; false if it gets stuck.
     */
    private static boolean earClip(List<double[]> poly, List<double[][]> out) {
        List<double[]> p = new ArrayList<>();
        for (double[] v : poly) {
            if (p.isEmpty() || Math.abs(v[0] - p.get(p.size() - 1)[0]) + Math.abs(v[1] - p.get(p.size() - 1)[1]) > 1e-15) p.add(v);
        }
        if (p.size() > 1 && Math.abs(p.get(0)[0] - p.get(p.size() - 1)[0]) + Math.abs(p.get(0)[1] - p.get(p.size() - 1)[1]) <= 1e-15) {
            p.remove(p.size() - 1);
        }
        int guard = 0;
        while (p.size() > 3) {
            if (++guard > 10000) return false;
            int n = p.size();
            boolean clipped = false;
            for (int i = 0; i < n; i++) {
                double[] a = p.get((i + n - 1) % n), b = p.get(i), c = p.get((i + 1) % n);
                double turn = cross(a, b, c);
                if (turn <= 0) continue;     // reflex, or in line
                boolean empty = true;
                for (int k = 0; k < n && empty; k++) {
                    double[] v = p.get(k);
                    if (v == a || v == b || v == c) continue;
                    if (cross(a, b, v) >= 0 && cross(b, c, v) >= 0 && cross(c, a, v) >= 0) empty = false;
                }
                if (!empty) continue;
                out.add(new double[][]{a, b, c});
                p.remove(i);
                clipped = true;
                break;
            }
            if (!clipped) {
                // only in-line corners left to remove: drop one, it adds no area
                int drop = -1;
                for (int i = 0; i < n && drop < 0; i++) {
                    if (Math.abs(cross(p.get((i + n - 1) % n), p.get(i), p.get((i + 1) % n))) < 1e-18) drop = i;
                }
                if (drop < 0) return false;
                p.remove(drop);
            }
        }
        if (p.size() == 3 && cross(p.get(0), p.get(1), p.get(2)) > 0) out.add(new double[][]{p.get(0), p.get(1), p.get(2)});
        return true;
    }

    /**
     * The fallback: the cell clipped against every opening's edges in turn, as convex
     * pieces fanned into triangles. Always works, but cuts along the full lines through
     * the outline's edges and so leaves thin slivers; only used where the tracer declines.
     */
    private List<double[][]> decomposed(double u0, double u1, double a0, double a1) {
        List<double[][]> pieces = new ArrayList<>();
        pieces.add(new double[][]{{u0, a0}, {u1, a0}, {u1, a1}, {u0, a1}});
        for (Opening o : openings) {
            if (o.uMax <= u0 || o.uMin >= u1 || o.tMax <= a0 || o.tMin >= a1) continue;
            List<double[][]> next = new ArrayList<>();
            for (double[][] q : pieces) subtract(q, o, next);
            pieces = next;
        }
        List<double[][]> tris = new ArrayList<>();
        for (double[][] p : pieces) {
            for (int k = 1; k + 1 < p.length; k++) tris.add(new double[][]{p[0], p[k], p[k + 1]});
        }
        return tris;
    }

    /** Add to out the convex pieces of q that lie outside the opening. */
    private static void subtract(double[][] q, Opening o, List<double[][]> out) {
        double[][] rest = q;
        int n = o.cutU.length;
        for (int k = 0; k < n && rest != null; k++) {
            int k2 = (k + 1) % n;
            double[][] outside = clip(rest, o.cutU[k], o.cutT[k], o.cutU[k2], o.cutT[k2], false);
            if (outside != null) out.add(outside);
            rest = clip(rest, o.cutU[k], o.cutT[k], o.cutU[k2], o.cutT[k2], true);
        }
    }

    /**
     * Clip a convex polygon to one side of the line through a and b. Inside is the left,
     * which for an anticlockwise outline is the inside of the shape.
     */
    private static double[][] clip(double[][] poly, double au, double at, double bu, double bt, boolean inside) {
        List<double[]> kept = new ArrayList<>();
        int n = poly.length;
        for (int i = 0; i < n; i++) {
            double[] p = poly[i], q = poly[(i + 1) % n];
            double dp = (bu - au) * (p[1] - at) - (bt - at) * (p[0] - au);
            double dq = (bu - au) * (q[1] - at) - (bt - at) * (q[0] - au);
            if (!inside) { dp = -dp; dq = -dq; }
            if (dp >= 0) kept.add(p);
            if ((dp >= 0) != (dq >= 0)) {
                double f = dp / (dp - dq);
                kept.add(new double[]{p[0] + (q[0] - p[0]) * f, p[1] + (q[1] - p[1]) * f});
            }
        }
        return kept.size() >= 3 ? kept.toArray(new double[0][]) : null;
    }

    /**
     * Fill an opening: a reveal lining the hole through the skin, two sheets of glass in
     * it, and a seal round the outside.
     *
     * Two sheets because of how the shader does glass. It treats a transmissive surface
     * as the boundary of a solid, entering on one hit and leaving on the next; a single
     * sheet would leave every ray that passed through it convinced it was still inside
     * glass, and the next thing it hit would bend it the wrong way. Two parallel sheets
     * three centimetres apart are a pane with thickness, and a ray goes in and comes out.
     */
    private void glaze(B b, Opening o, double sealWidth) {
        int n = o.u.length;
        double uc = 0, tc = 0;
        for (int i = 0; i < n; i++) { uc += o.u[i] / n; tc += o.t[i] / n; }

        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            b.quad(fus(b, o.u[i], o.t[i], 0), fus(b, o.u[j], o.t[j], 0),
                   fus(b, o.u[j], o.t[j], -o.depth), fus(b, o.u[i], o.t[i], -o.depth), o.reveal);
        }

        pane(b, o, uc, tc, -0.015);
        pane(b, o, uc, tc, -0.045);

        double[][] seal = offsetOutline(o, sealWidth);
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            b.quad(fus(b, o.u[i], o.t[i], 0.004), fus(b, o.u[j], o.t[j], 0.004),
                   fus(b, seal[j][0], seal[j][1], 0.004), fus(b, seal[i][0], seal[i][1], 0.004), materials.frame);
        }
    }

    /** One sheet of glass filling an opening, in rings so it follows the curve of the skin. */
    private void pane(B b, Opening o, double uc, double tc, double swell) {
        int n = o.u.length, rings = 4;
        P centre = fus(b, uc, tc, swell);
        for (int r = 1; r <= rings; r++) {
            double f0 = (double) (r - 1) / rings, f1 = (double) r / rings;
            for (int i = 0; i < n; i++) {
                int j = (i + 1) % n;
                P a1 = fus(b, lerp(uc, o.u[i], f1), lerp(tc, o.t[i], f1), swell);
                P b1 = fus(b, lerp(uc, o.u[j], f1), lerp(tc, o.t[j], f1), swell);
                if (r == 1) {
                    b.tri(centre, a1, b1, o.glass);
                } else {
                    P a0 = fus(b, lerp(uc, o.u[i], f0), lerp(tc, o.t[i], f0), swell);
                    P b0 = fus(b, lerp(uc, o.u[j], f0), lerp(tc, o.t[j], f0), swell);
                    b.quad(a0, b0, b1, a1, o.glass);
                }
            }
        }
    }

    /**
     * An opening's outline pushed outward by a distance in metres along the skin.
     *
     * Worked out in a local metric - metres along the fuselage, metres round it - since a
     * step in u and a step in theta are very different distances, and the second changes
     * with how big the section is at that point.
     */
    private static double[][] offsetOutline(Opening o, double w) {
        int n = o.u.length;
        double[][] out = new double[n][];
        double[] p = new double[2], q = new double[2];
        for (int i = 0; i < n; i++) {
            int prev = (i + n - 1) % n, next = (i + 1) % n;
            // metres of skin per radian of theta, here
            section(o.u[i], o.t[i] - 1e-4, 0, p);
            section(o.u[i], o.t[i] + 1e-4, 0, q);
            double perRadian = Math.hypot(q[0] - p[0], q[1] - p[1]) / 2e-4;

            double ds = (o.u[next] - o.u[prev]) * LENGTH;
            double da = (o.t[next] - o.t[prev]) * perRadian;
            double len = Math.hypot(ds, da);
            // outward is to the right of an anticlockwise outline
            double ns = da / len, na = -ds / len;
            out[i] = new double[]{o.u[i] + ns * w / LENGTH, o.t[i] + na * w / perRadian};
        }
        return out;
    }


    /**
     * Cabin window stations.
     *
     * Window pitch follows the fuselage frame pitch, and the 787's is 24 inches - notably
     * wider than the 777's 21 or the 737's 20, and part of the same design decision as
     * the big windows. A one-piece composite barrel does not need frames as closely
     * spaced as a riveted aluminium shell, which is what buys both the extra spacing and
     * the room for a taller window between the frames.
     *
     * Positions are the side view's: the first window 8.26 m from the nose, then one
     * every frame, with four frames left blank around each of the second and third pairs
     * of doors. Behind the last of those the frames change and the final four windows sit
     * at their own spacing.
     */
    private static double[] windowStations() {
        double pitch = 24 * 0.0254;
        double[] aft = {46.09, 46.75, 47.40, 48.02};
        int grid = 61;
        List<Double> out = new ArrayList<>();
        for (int k = 0; k < grid + aft.length; k++) {
            double s = k < grid ? 8.258 + k * pitch : aft[k - grid];
            boolean blocked = false;
            for (double door : DOOR_STATIONS) if (Math.abs(s - door) < 1.3) blocked = true;
            if (!blocked) out.add(s);
        }
        double[] a = new double[out.size()];
        for (int i = 0; i < a.length; i++) a[i] = out.get(i);
        return a;
    }

    /** Cabin window centre height above the axis, off the side view. */
    private static final double WINDOW_CENTRE = 0.763;

    /**
     * Glass, reveals and seals for every window.
     *
     * The cabin windows are 27 by 47 centimetres, about sixty percent bigger than a
     * 737's, and set noticeably higher up the wall. The seal round each is narrow; the
     * flight deck windows get a wider dark frame.
     */
    private void buildWindows(B b) {
        for (Opening o : openings) {
            glaze(b, o, o.glass == materials.cockpitGlass ? 0.022 : 0.018);
        }
    }

    /**
     * A rounded rectangle centred at (s, h) in the side view - stations along, heights up -
     * as a closed outline, anticlockwise. Corners are quarter ellipses, rs along and rh up.
     */
    private static double[][] roundedRect(double s, double h, double halfS, double halfH,
                                          double rs, double rh, int perCorner) {
        double[][] out = new double[4 * (perCorner + 1)][];
        double[][] corner = {{+1, +1}, {-1, +1}, {-1, -1}, {+1, -1}};
        int k = 0;
        for (int c = 0; c < 4; c++) {
            double cs = s + corner[c][0] * (halfS - rs), ch = h + corner[c][1] * (halfH - rh);
            for (int i = 0; i <= perCorner; i++) {
                double a = (c + (double) i / perCorner) * Math.PI * 0.5;
                out[k++] = new double[]{cs + rs * Math.cos(a), ch + rh * Math.sin(a)};
            }
        }
        return out;
    }

    /**
     * A polygon with rounded corners, as a dense list of points.
     *
     * Each corner is (x, y, z, radius); every coordinate is carried through, so it works
     * equally on 3D points. Corners are rounded with a quadratic Bezier that starts and
     * ends radius along each of the two edges, which is tangent to both.
     */
    private static double[][] roundedOutline(double[][] corners, double step) {
        List<double[]> out = new ArrayList<>();
        int n = corners.length;

        for (int i = 0; i < n; i++) {
            double[] prev = corners[(i + n - 1) % n], p = corners[i], next = corners[(i + 1) % n];
            double[] a = towards(p, prev, p[3]), c = towards(p, next, p[3]);

            int k = 16;
            for (int j = 0; j < k; j++) {
                double t = (double) j / k, w0 = (1 - t) * (1 - t), w1 = 2 * t * (1 - t), w2 = t * t;
                out.add(new double[]{w0 * a[0] + w1 * p[0] + w2 * c[0],
                                     w0 * a[1] + w1 * p[1] + w2 * c[1],
                                     w0 * a[2] + w1 * p[2] + w2 * c[2]});
            }

            // the straight run to where the next corner's rounding begins
            double[] end = towards(next, p, next[3]);
            double len = Math.sqrt(sq(end[0] - c[0]) + sq(end[1] - c[1]) + sq(end[2] - c[2]));
            int m = Math.max(1, (int) Math.ceil(len / step));
            for (int j = 0; j < m; j++) {
                double t = (double) j / m;
                out.add(new double[]{lerp(c[0], end[0], t), lerp(c[1], end[1], t), lerp(c[2], end[2], t)});
            }
        }
        return out.toArray(new double[0][]);
    }

    /** The point distance r from p towards q, but never more than 45% of the way. */
    private static double[] towards(double[] p, double[] q, double r) {
        double dx = q[0] - p[0], dy = q[1] - p[1], dz = q[2] - p[2];
        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        double f = Math.min(r, 0.45 * len) / len;
        return new double[]{p[0] + dx * f, p[1] + dy * f, p[2] + dz * f};
    }

    private static double sq(double x) {
        return x * x;
    }

    /**
     * Four pairs of passenger doors plus three cargo doors, as seams cut in the skin.
     *
     * Every outline is Boeing's, off the side view: rounded rectangles with tight 15 cm
     * corners (18 cm on the second door), and the bulk cargo door with its sloping sill.
     * They were superellipses before, which rounded them into lozenges.
     */
    private void buildDoors(B b) {
        double[] cornerRadius = {0.152, 0.178, 0.152, 0.152};
        double halfH = (DOOR_TOP - DOOR_SILL) / 2, centreH = (DOOR_TOP + DOOR_SILL) / 2;

        for (int i = 0; i < DOOR_STATIONS.length; i++) {
            double r = cornerRadius[i];
            double[][] outline = roundedRect(DOOR_STATIONS[i], centreH, DOOR_WIDTH / 2, halfH, r, r, 6);
            for (int side = -1; side <= 1; side += 2) seam(b, outline, side, 0.022);
        }

        // cargo doors, starboard side only: forward and aft containerised holds
        for (double s : new double[]{11.001, 43.294}) {
            seam(b, roundedRect(s, -1.469, 1.346, 0.851, 0.152, 0.152, 6), +1, 0.022);
        }

        // and the bulk cargo door, whose sill slopes up towards the tail
        double[][] bulk = roundedOutline(new double[][]{
                {47.231, -2.303, 0, 0.155}, {48.247, -2.244, 0, 0.155},
                {48.247, -1.047, 0, 0.155}, {47.231, -1.047, 0, 0.155}}, 0.05);
        seam(b, bulk, +1, 0.022);
    }

    /**
     * A seam following an outline drawn in the side view, as a dark band on the skin.
     *
     * The outline is in (station, height); each point is put on the skin at that height,
     * which is exactly what the side view shows, and the band is the outline pushed
     * outward by the seam's width.
     */
    private void seam(B b, double[][] sh, int side, double width) {
        int n = sh.length;
        double[][] outer = new double[n][];
        for (int i = 0; i < n; i++) {
            double[] prev = sh[(i + n - 1) % n], next = sh[(i + 1) % n];
            double ds = next[0] - prev[0], dh = next[1] - prev[1], len = Math.hypot(ds, dh);
            // outward is to the right of an anticlockwise outline
            outer[i] = new double[]{sh[i][0] + dh / len * width, sh[i][1] - ds / len * width};
        }
        for (int i = 0; i < n; i++) {
            int j = (i + 1) % n;
            b.quad(seamPoint(b, sh[i], side), seamPoint(b, sh[j], side),
                   seamPoint(b, outer[j], side), seamPoint(b, outer[i], side), materials.darkSeam);
        }
    }

    private P seamPoint(B b, double[] sh, int side) {
        double u = sh[0] / LENGTH;
        return fus(b, u, side * thetaAtHeight(u, sh[1]), 0.005);
    }

    // ================================================================================
    // INTERIOR
    // ================================================================================

    /* Enough of the inside to be worth looking at through clear glass: the flight deck
     * behind the windscreen, and a lit economy cabin behind the windows. Floors sit where
     * the drawing puts them - the cabin floor is the door sill - and everything is kept
     * inside a lining that stands off the skin by the depth of the window reveals, so a
     * window reads as a hole through a wall rather than through a sheet of paper. */
    private static final double CABIN_FLOOR = DOOR_SILL;
    private static final double DECK_FLOOR = -0.50;
    private static final double CABIN_START = 5.2;    // the flight deck door
    private static final double CABIN_END = 51.0;     // the aft pressure bulkhead, near enough

    private void buildInterior(B b) {
        buildFlightDeck(b);
        buildCabin(b);
    }

    /**
     * The flight deck: glareshield, the instrument panel with the 787's five big displays,
     * pedestal, side consoles, two seats and an overhead panel. Lit only by its displays.
     */
    private void buildFlightDeck(B b) {
        floor(b, 1.90, CABIN_START, DECK_FLOOR, 0.08, materials.flightDeck);
        lining(b, 1.40, CABIN_START, DECK_FLOOR, 0.07, materials.flightDeck);
        bulkhead(b, CABIN_START, DECK_FLOOR, 0.07, materials.flightDeck);

        // glareshield, just below the windscreen, following the narrowing nose forward
        for (double s = 1.72; s < 2.42; s += 0.07) {
            double w = halfWidthAt(s + 0.035, 0.32) - 0.09;
            box(b, s, s + 0.07, -w, w, 0.27, 0.34, materials.glareshield);
        }

        // instrument panel, sloping back from under the glareshield to the floor
        double topS = 2.42, bottomS = 2.56, topH = 0.30, bottomH = DECK_FLOOR;
        double wt = halfWidthAt(topS, topH) - 0.09, wb = halfWidthAt(bottomS, bottomH) - 0.09;
        b.quad(deck(b, -wt, topH, topS), deck(b, wt, topH, topS), deck(b, wb, bottomH, bottomS), deck(b, -wb, bottomH, bottomS),
               materials.flightDeck);

        /* The five displays across it: primary flight displays outboard, navigation
         * displays inside them, engine indication in the middle. */
        double[] centres = {-0.86, -0.43, 0, 0.43, 0.86};
        for (int i = 0; i < centres.length; i++) {
            Material screen = (i == 0 || i == 4) ? materials.display : materials.displayDark;
            double h0 = 0.0, h1 = 0.25;
            double s0 = panelFace(h0, topS, bottomS, topH, bottomH) + 0.012;
            double s1 = panelFace(h1, topS, bottomS, topH, bottomH) + 0.012;
            b.quad(deck(b, centres[i] - 0.17, h1, s1), deck(b, centres[i] + 0.17, h1, s1),
                   deck(b, centres[i] + 0.17, h0, s0), deck(b, centres[i] - 0.17, h0, s0), screen);
        }

        // the pedestal between the seats, with the thrust levers on it
        box(b, 2.56, 3.25, -0.20, 0.20, DECK_FLOOR, -0.05, materials.flightDeck);

        for (int side = -1; side <= 1; side += 2) {
            double x = side * 0.56;
            box(b, 3.15, 3.45, x - 0.14, x + 0.14, DECK_FLOOR, -0.12, materials.flightDeck);   // seat base
            box(b, 3.05, 3.55, x - 0.25, x + 0.25, -0.12, 0.00, materials.pilotSeat);           // cushion
            box(b, 3.52, 3.66, x - 0.24, x + 0.24, 0.00, 0.80, materials.pilotSeat);            // back
            box(b, 3.54, 3.66, x - 0.15, x + 0.15, 0.80, 0.98, materials.pilotSeat);            // headrest

            // side console, under the side window, in steps that follow the widening nose
            for (double s = 2.60; s < 3.85; s += 0.25) {
                double outer = Math.min(halfWidthAt(s, DECK_FLOOR), halfWidthAt(s, -0.08)) - 0.10;
                box(b, s, s + 0.25, Math.min(side * 1.02, side * outer), Math.max(side * 1.02, side * outer),
                    DECK_FLOOR, -0.08, materials.flightDeck);
            }
        }

        /* Overhead panel, hung under the roof and stepping up with it. Its height is set
         * by the roof at the panel's outer edges, not at the crown - the top of the nose
         * falls away to the sides fast enough that a panel hung from the crown pokes its
         * corners out through the skin. Its underside is the backlit switch panel. */
        Material backlit = cabinLightsOn ? materials.panelLight : materials.flightDeck;
        for (double s = 2.75; s < 3.75; s += 0.25) {
            double top = Math.min(roofAt(s, 0.42), roofAt(s + 0.25, 0.42)) - 0.10;
            box(b, s, s + 0.25, -0.42, 0.42, top - 0.26, top, materials.flightDeck);
            double under = top - 0.263;
            b.quad(deck(b, -0.40, under, s), deck(b, 0.40, under, s),
                   deck(b, 0.40, under, s + 0.25), deck(b, -0.40, under, s + 0.25), backlit);
        }

        // the pedestal's backlit top
        b.quad(deck(b, -0.18, -0.047, 2.58), deck(b, 0.18, -0.047, 2.58),
               deck(b, 0.18, -0.047, 3.23), deck(b, -0.18, -0.047, 3.23), backlit);

        if (cabinLightsOn) {
            // the dome light, filling the ceiling behind the overhead panel
            double h = Math.min(roofAt(3.80, 0.60), roofAt(5.05, 0.60)) - 0.10;
            b.quad(deck(b, -0.60, h, 3.80), deck(b, 0.60, h, 3.80), deck(b, 0.60, h, 5.05), deck(b, -0.60, h, 5.05),
                   materials.domeLight);

            // floodlight under the glareshield's lip, washing down over the panel
            double w = halfWidthAt(2.36, 0.27) - 0.15;
            b.quad(deck(b, -w, 0.265, 2.30), deck(b, w, 0.265, 2.30), deck(b, w, 0.265, 2.42), deck(b, -w, 0.265, 2.42),
                   materials.floodLight);

            // a map light on each side wall, above the side console
            for (int side = -1; side <= 1; side += 2) {
                double x = side * (Math.min(halfWidthAt(3.0, 0.10), halfWidthAt(3.6, 0.22)) - 0.12);
                b.quad(deck(b, x, 0.10, 3.00), deck(b, x, 0.22, 3.00), deck(b, x, 0.22, 3.60), deck(b, x, 0.10, 3.60),
                       materials.floodLight);
            }
        }
    }

    /** Height of the skin's upper half at station s, a lateral distance out from the centreline. */
    private static double roofAt(double s, double lateral) {
        double u = s / LENGTH, lo = 0, hi = Math.PI / 2;
        double[] p = new double[2];
        for (int i = 0; i < 40; i++) {
            double mid = (lo + hi) / 2;
            section(u, mid, 0, p);
            if (p[0] < lateral) lo = mid;
            else hi = mid;
        }
        section(u, lo, 0, p);
        return p[1];
    }

    /** Station of the instrument panel's face at height h. */
    private static double panelFace(double h, double topS, double bottomS, double topH, double bottomH) {
        return lerp(topS, bottomS, (topH - h) / (topH - bottomH));
    }

    /**
     * An economy cabin, nine abreast: carpet, lining, overhead bins down both sides and the
     * middle, the ceiling between them lit along both aisles, and rows of seats at a 32 inch
     * pitch, left clear around the doors.
     */
    private void buildCabin(B b) {
        floor(b, CABIN_START, CABIN_END, CABIN_FLOOR, 0.11, materials.carpet);
        lining(b, CABIN_START, CABIN_END, CABIN_FLOOR, 0.11, materials.cabinLining);
        bulkhead(b, CABIN_END, CABIN_FLOOR, 0.11, materials.cabinLining);

        double ceiling = 2.12, binBottom = 1.42, binFront = 1.62, centreBin = 0.60;
        Material light = cabinLightsOn ? materials.cabinLight : materials.cabinLining;

        double s0 = 6.9, s1 = 48.9;
        int n = (int) Math.ceil((s1 - s0) / 1.4);
        for (int i = 0; i < n; i++) {
            double a = lerp(s0, s1, (double) i / n), c = lerp(s0, s1, (double) (i + 1) / n);
            // sized by whichever end is narrower, so nothing pokes through the lining
            double wallLow = Math.min(halfWidthAt(a, binBottom), halfWidthAt(c, binBottom)) - 0.11;
            double wallHigh = Math.min(halfWidthAt(a, ceiling), halfWidthAt(c, ceiling)) - 0.11;
            double front = Math.min(binFront, wallHigh - 0.05);

            for (int side = -1; side <= 1; side += 2) {
                // side bins: front, underside, and the strip of ceiling above them
                b.quad(deck(b, side * front, binBottom, a), deck(b, side * front, ceiling, a),
                       deck(b, side * front, ceiling, c), deck(b, side * front, binBottom, c), materials.binPlastic);
                b.quad(deck(b, side * front, binBottom, a), deck(b, side * front, binBottom, c),
                       deck(b, side * wallLow, binBottom, c), deck(b, side * wallLow, binBottom, a), materials.binPlastic);
                b.quad(deck(b, side * front, ceiling, a), deck(b, side * wallHigh, ceiling, a),
                       deck(b, side * wallHigh, ceiling, c), deck(b, side * front, ceiling, c), materials.cabinLining);

                // the lit ceiling over this aisle
                b.quad(deck(b, side * centreBin, ceiling, a), deck(b, side * front, ceiling, a),
                       deck(b, side * front, ceiling, c), deck(b, side * centreBin, ceiling, c), light);

                // and the wash light under the bins, which is what lights the windows
                double washIn = side * (front + 0.04), washOut = side * (front + 0.30);
                b.quad(deck(b, washIn, binBottom - 0.005, a), deck(b, washIn, binBottom - 0.005, c),
                       deck(b, washOut, binBottom - 0.005, c), deck(b, washOut, binBottom - 0.005, a), light);
            }

            // centre bins
            box(b, a, c, -centreBin, centreBin, 1.58, ceiling, materials.binPlastic);
        }

        // ends of the bin runs
        for (double s : new double[]{s0, s1}) {
            for (int side = -1; side <= 1; side += 2) {
                double wallLow = halfWidthAt(s, binBottom) - 0.11, wallHigh = halfWidthAt(s, ceiling) - 0.11;
                double front = Math.min(binFront, wallHigh - 0.05);
                b.quad(deck(b, side * front, binBottom, s), deck(b, side * wallLow, binBottom, s),
                       deck(b, side * wallHigh, ceiling, s), deck(b, side * front, ceiling, s), materials.binPlastic);
            }
        }

        // seats: three blocks of three, 46 cm wide, at a 32 inch pitch
        double[] seatX = {-2.30, -1.84, -1.38, -0.46, 0.0, 0.46, 1.38, 1.84, 2.30};
        double pitch = 32 * 0.0254;
        for (double s = 8.0; s < 48.0; s += pitch) {
            boolean clear = false;
            for (double door : DOOR_STATIONS) if (Math.abs(s + 0.3 - door) < 1.25) clear = true;
            if (clear) continue;
            for (double x : seatX) {
                box(b, s, s + 0.46, x - 0.21, x + 0.21, CABIN_FLOOR + 0.30, CABIN_FLOOR + 0.43, materials.seatFabric);
                box(b, s + 0.46, s + 0.56, x - 0.22, x + 0.22, CABIN_FLOOR + 0.43, CABIN_FLOOR + 1.10, materials.seatFabric);
                box(b, s + 0.15, s + 0.35, x - 0.05, x + 0.05, CABIN_FLOOR, CABIN_FLOOR + 0.30, materials.flightDeck);
            }
        }
    }

    /** A point inside the fuselage: lateral x, height h above the axis, station s. */
    private P deck(B b, double x, double h, double s) {
        return b.v(x, CENTRE_HEIGHT + h, NOSE_Z + s);
    }

    /** An axis-aligned box inside the fuselage, in stations, lateral offsets and heights. */
    private void box(B b, double s0, double s1, double x0, double x1, double h0, double h1, Material m) {
        b.box(x0, x1, CENTRE_HEIGHT + h0, CENTRE_HEIGHT + h1, NOSE_Z + s0, NOSE_Z + s1, m);
    }

    /** A flat floor at height h, between stations s0 and s1, out to the lining. */
    private void floor(B b, double s0, double s1, double h, double inset, Material m) {
        int n = Math.max(2, (int) Math.ceil((s1 - s0) / 0.5));
        double prevS = s0, prevW = halfWidthAt(s0, h) - inset;
        for (int i = 1; i <= n; i++) {
            double s = lerp(s0, s1, (double) i / n), w = halfWidthAt(s, h) - inset;
            b.quad(deck(b, -prevW, h, prevS), deck(b, prevW, h, prevS), deck(b, w, h, s), deck(b, -w, h, s), m);
            prevS = s;
            prevW = w;
        }
    }

    /**
     * The inside wall: the skin again, stood off inward by depth and cut by the same
     * windows, so each window's reveal runs from the skin to the lining and closes up.
     * Only above the floor; below it nothing can be seen.
     */
    private void lining(B b, double s0, double s1, double floorH, double depth, Material m) {
        List<Double> us = new ArrayList<>();
        us.add(s0 / LENGTH);
        for (double u : fuselageStations()) if (u > s0 / LENGTH && u < s1 / LENGTH) us.add(u);
        us.add(s1 / LENGTH);

        double[] grid = new double[us.size()];
        for (int i = 0; i < grid.length; i++) grid[i] = us.get(i);

        int segments = fusSegments;
        double[] p = new double[2];
        skinGrid(b, grid, 0, -depth, m, (i, j) -> {
            double[] a = cellAngles(j, segments);
            section((grid[i] + grid[i + 1]) / 2, (a[0] + a[1]) / 2, -depth, p);
            return p[1] >= floorH - 0.15;
        });
    }

    /** A wall across the fuselage at station s, from the floor up. */
    private void bulkhead(B b, double s, double floorH, double inset, Material m) {
        double u = s / LENGTH;
        double tf = thetaAtHeight(u, floorH);
        int n = 48;
        P centre = deck(b, 0, (floorH + CROWN.at(u)) / 2, s);
        P first = fus(b, u, -tf, -inset), prev = first;
        for (int i = 1; i <= n; i++) {
            P p = fus(b, u, -tf + 2 * tf * i / n, -inset);
            b.tri(centre, prev, p, m);
            prev = p;
        }
        b.tri(centre, prev, first, m);   // across the floor
    }

    // ================================================================================
    // AIRFOILS AND LOFTING
    // ================================================================================

    /**
     * Half thickness of a NACA four-digit section at chord fraction x. The last
     * coefficient is -0.1036 rather than the textbook -0.1015, which is the variant that
     * closes the trailing edge instead of leaving it blunt.
     */
    private static double thickness(double x, double t) {
        double sqrtX = Math.sqrt(x);
        return 5 * t * (0.2969 * sqrtX - 0.1260 * x - 0.3516 * x * x + 0.2843 * x * x * x - 0.1036 * x * x * x * x);
    }

    /**
     * Camber line, loaded towards the back.
     *
     * That rear loading is the giveaway of a supercritical section - the flat topped,
     * aft cambered shape every modern airliner wing uses to hold its shockwave further
     * aft and cruise closer to the speed of sound.
     */
    private static double camber(double x, double amount) {
        return amount * (Math.sin(Math.PI * Math.pow(x, 0.80)) * 0.80 + x * x * x * (1 - x) * 2.6);
    }

    /**
     * One point around a closed airfoil, for parameter a in [0, 2pi).
     *
     * x comes from a cosine, so points bunch at the leading and trailing edges where the
     * curvature is - even spacing wastes most of them on the flat middle and then renders
     * the nose of the section as a visible polygon.
     */
    private static void airfoil(double a, double thicknessRatio, double camberAmount, double[] out) {
        double x = 0.5 * (1 - Math.cos(a));
        double halfT = thickness(x, thicknessRatio);
        out[0] = x;
        out[1] = camber(x, camberAmount) + (a < Math.PI ? halfT : -halfT);
    }

    /** Fills a section of n points at the station for eta. */
    private interface SectionFn {
        void section(double eta, int n, P[] out);
    }

    /**
     * Stitch consecutive cross sections into a surface.
     *
     * The first half of each section's points are the upper surface and the second half
     * the lower, which is how the two materials get split without the caller saying so.
     */
    private void loft(B b, int stations, int n, Material upper, Material lower, SectionFn fn, boolean capTip) {
        P[] previous = new P[n];
        P[] current = new P[n];

        fn.section(0, n, previous);

        for (int i = 1; i <= stations; i++) {
            fn.section((double) i / stations, n, current);

            for (int k = 0; k < n; k++) {
                int k2 = (k + 1) % n;
                b.quad(previous[k], previous[k2], current[k2], current[k], k < n / 2 ? upper : lower);
            }

            P[] swap = previous;
            previous = current;
            current = swap;
        }

        if (capTip) {
            double cx = 0, cy = 0, cz = 0;
            for (int k = 0; k < n; k++) { cx += previous[k].x; cy += previous[k].y; cz += previous[k].z; }
            P centre = new P(cx / n, cy / n, cz / n);
            for (int k = 0; k < n; k++) b.tri(centre, previous[k], previous[(k + 1) % n], upper);
        }
    }

    // ================================================================================
    // WING
    // ================================================================================

    /** Leading edge position along the fuselage at spanwise eta. */
    private static double leadingEdgeZ(double eta) {
        return NOSE_Z + WING_LE.at(eta);
    }

    private static double wingChord(double eta) {
        return WING_TE.at(eta) - WING_LE.at(eta);
    }

    /**
     * Wing twist, as the angle the section is rotated about its leading edge.
     *
     * Two things at once. The root sits at the published 2 degrees of INCIDENCE - the
     * whole wing is pitched nose-up relative to the fuselage, so the fuselage flies level
     * in cruise instead of nose-high. On top of that is 4.4 degrees of WASHOUT, the tip
     * progressively twisted nose-down relative to the root, so the root stalls first and
     * the ailerons are still in clean air when the inner wing lets go.
     *
     * Positive here rotates the trailing edge up, which is nose-down, so incidence is
     * negative and washout is positive. An earlier version had this backwards and gave
     * the tip MORE incidence than the root, which is wash-in - the arrangement that makes
     * an aeroplane drop a wing at the stall.
     */
    private static double wingTwist(double eta) {
        return Math.toRadians(-2.0 + 4.4 * Math.pow(eta, 1.25));
    }

    /** A point on the wing surface at spanwise eta and airfoil parameter a, pushed out by offset. */
    private P wingPoint(B b, int side, double eta, double a, double offset) {
        double x = SEMI_SPAN * eta;
        double chord = wingChord(eta);
        double zLE = leadingEdgeZ(eta);
        double y = WING_ROOT_Y + WING_RISE.at(eta);
        double t = WING_THICKNESS.at(eta);

        /* Camber as a fraction of the chord, which is what airfoil() takes. This used to
         * be 0.021 * chord, which airfoil() then multiplied by the chord again - so camber
         * grew with the square of the chord, and the 11.9 m root section was arched 2.8 m,
         * 24% of its chord. That hump is what made the inner wing look as if it climbed
         * steeply up out of the fuselage. */
        double cam = 0.020;

        double[] p = new double[2];
        airfoil(a, t, cam, p);

        double cx = p[0] * chord;
        double cy = p[1] * chord;

        if (offset != 0) {
            // outward normal of the section, from a short step along the outline
            double[] q = new double[2];
            airfoil(a + 0.004, t, cam, q);
            double dx = (q[0] - p[0]) * chord, dy = (q[1] - p[1]) * chord;
            double len = Math.sqrt(dx * dx + dy * dy);
            if (len > 1e-12) {
                cx += (dy / len) * offset;
                cy += (-dx / len) * offset;
            }
        }

        double twist = wingTwist(eta);
        double cosT = Math.cos(twist), sinT = Math.sin(twist);

        return b.v(side * x, y + cx * sinT + cy * cosT, zLE + cx * cosT - cy * sinT);
    }

    /** Airfoil parameter for a chord fraction, on the upper or lower surface. */
    private static double chordToParam(double chordFraction, boolean upper) {
        double a = Math.acos(1 - 2 * Math.min(1, Math.max(0, chordFraction)));
        return upper ? a : 2 * Math.PI - a;
    }

    private void buildWing(B b, int side) {
        loft(b, wingStations, airfoilPoints, materials.wingUpper, materials.wingLower, (eta, count, out) -> {
            for (int k = 0; k < count; k++) out[k] = wingPoint(b, side, eta, 2 * Math.PI * k / count, 0);
        }, true);

        buildWingSeams(b, side);
        buildFlapTrackFairings(b, side);
        buildStaticWicks(b, side);
    }

    /**
     * The lines where the wing comes apart: slats along the leading edge, spoilers and
     * flaps and ailerons along the trailing edge.
     *
     * Modelled as thin strips laid on the surface rather than parts that really hinge. A
     * wing rendered as one unbroken shell looks like a model aeroplane; these lines are
     * most of what makes it look like hardware, for a tiny fraction of the geometry that
     * actually articulating everything would cost.
     */
    private void buildWingSeams(B b, int side) {
        // leading edge slats - five segments with a break between each
        for (double[] s : new double[][]{{0.115, 0.300}, {0.310, 0.455}, {0.465, 0.600}, {0.610, 0.735}, {0.745, 0.845}}) {
            wingSeam(b, side, s[0], s[1], 0.135, true);
            wingSeam(b, side, s[0], s[1], 0.165, false);
            wingSpanSeam(b, side, s[0], 0.135, 0.165, true);
            wingSpanSeam(b, side, s[1], 0.135, 0.165, true);
        }

        // spoiler panels on the upper surface
        for (int i = 0; i < 6; i++) {
            double e0 = 0.135 + i * 0.098, e1 = e0 + 0.085;
            wingSeam(b, side, e0, e1, 0.585, true);
            wingSeam(b, side, e0, e1, 0.700, true);
            wingSpanSeam(b, side, e0, 0.585, 0.700, true);
            wingSpanSeam(b, side, e1, 0.585, 0.700, true);
        }

        // trailing edge: inboard flap, outboard flap, then aileron
        for (double[] s : new double[][]{{0.130, 0.330}, {0.350, 0.640}, {0.665, 0.850}}) {
            wingSeam(b, side, s[0], s[1], 0.738, true);
            wingSeam(b, side, s[0], s[1], 0.762, false);
            wingSpanSeam(b, side, s[0], 0.738, 0.985, true);
            wingSpanSeam(b, side, s[1], 0.738, 0.985, true);
        }
    }

    /** A seam running along the span at a fixed chord fraction. */
    private void wingSeam(B b, int side, double eta0, double eta1, double chordFraction, boolean upper) {
        int steps = Math.max(4, (int) ((eta1 - eta0) * wingStations * 2));
        double a = chordToParam(chordFraction, upper);
        double da = 0.010;

        for (int i = 0; i < steps; i++) {
            double e0 = eta0 + (eta1 - eta0) * i / steps;
            double e1 = eta0 + (eta1 - eta0) * (i + 1) / steps;
            b.quad(wingPoint(b, side, e0, a - da, 0.004), wingPoint(b, side, e0, a + da, 0.004),
                   wingPoint(b, side, e1, a + da, 0.004), wingPoint(b, side, e1, a - da, 0.004), materials.wingSeam);
        }
    }

    /** A seam running fore and aft at a fixed spanwise station. */
    private void wingSpanSeam(B b, int side, double eta, double chord0, double chord1, boolean upper) {
        int steps = 6;
        double a0 = chordToParam(chord0, upper), a1 = chordToParam(chord1, upper);
        double de = 0.010 / SEMI_SPAN;

        for (int i = 0; i < steps; i++) {
            double aa = a0 + (a1 - a0) * i / steps;
            double ab = a0 + (a1 - a0) * (i + 1) / steps;
            b.quad(wingPoint(b, side, eta - de, aa, 0.004), wingPoint(b, side, eta + de, aa, 0.004),
                   wingPoint(b, side, eta + de, ab, 0.004), wingPoint(b, side, eta - de, ab, 0.004), materials.wingSeam);
        }
    }

    /**
     * Flap track fairings: the teardrop pods hanging under each wing's trailing edge,
     * housing the rails the flaps run out along.
     *
     * Three per wing, where the plan view shows them poking out behind the trailing edge,
     * each with its width and depth from the front view. There were four, starting with
     * one at 5 m out where the real wing has none - the inboard flap's track is inside
     * the body fairing - and all of them hung nearly twice as deep as they should.
     */
    private void buildFlapTrackFairings(B b, int side) {
        int rings = 18, segments = 16;

        // metres out, half width, bottom below the leading edge line, overhang behind the trailing edge
        double[][] fairings = {
                {8.86, 0.280, 0.42, 0.65},
                {14.62, 0.275, 0.39, 1.10},
                {18.94, 0.265, 0.45, 1.00},
        };

        for (double[] f : fairings) {
            double eta = f[0] / SEMI_SPAN;
            double chord = wingChord(eta);
            double zLE = leadingEdgeZ(eta);
            double x = side * f[0];

            double zFront = zLE + chord * 0.45;
            double zBack = zLE + chord + f[3];
            double halfHeight = 0.30;
            double centre = WING_ROOT_Y + WING_RISE.at(eta) - f[2] + halfHeight;

            for (int i = 0; i < rings; i++) {
                double s0 = (double) i / rings, s1 = (double) (i + 1) / rings;
                double z0 = lerp(zFront, zBack, s0), z1 = lerp(zFront, zBack, s1);
                double k0 = fairingRadius(s0) / 0.60, k1 = fairingRadius(s1) / 0.60;

                /* A superellipse section, a little boxier than an ellipse: the front view
                 * shows each fairing with a flat bottom rather than a round one. */
                for (int j = 0; j < segments; j++) {
                    double t0 = 2 * Math.PI * j / segments, t1 = 2 * Math.PI * (j + 1) / segments;
                    double sx0 = boxy(Math.sin(t0)), cy0 = boxy(Math.cos(t0));
                    double sx1 = boxy(Math.sin(t1)), cy1 = boxy(Math.cos(t1));
                    b.quad(b.v(x + f[1] * k0 * sx0, centre - halfHeight * k0 * cy0, z0),
                           b.v(x + f[1] * k0 * sx1, centre - halfHeight * k0 * cy1, z0),
                           b.v(x + f[1] * k1 * sx1, centre - halfHeight * k1 * cy1, z1),
                           b.v(x + f[1] * k1 * sx0, centre - halfHeight * k1 * cy0, z1),
                           materials.wingLower);
                }
            }
        }
    }

    /** sin or cos raised to 2/3, keeping its sign: turns a circle into a superellipse of exponent 3. */
    private static double boxy(double v) {
        return Math.copySign(Math.pow(Math.abs(v), 2.0 / 3.0), v);
    }

    private static double fairingRadius(double s) {
        if (s < 0.30) return 0.60 * Math.pow(s / 0.30, 0.55);
        return 0.60 * Math.pow(1 - (s - 0.30) / 0.70, 0.80);
    }

    /**
     * Static wicks: the little spikes along the trailing edges that bleed off the static
     * charge an airframe builds up in flight. Tiny, but they break up what would
     * otherwise be an unnaturally clean edge, and the eye notices.
     */
    private void buildStaticWicks(B b, int side) {
        for (int i = 0; i < 14; i++) {
            double eta = 0.36 + i * 0.044;
            P root = wingPoint(b, side, eta, Math.PI, 0);
            double back = leadingEdgeZ(eta) + wingChord(eta) + 0.17;
            spike(b, root, b.v(side * SEMI_SPAN * eta, root.y - 0.015, back), 0.007, materials.darkSeam);
        }
    }

    /**
     * The wing-to-body fairing.
     *
     * An offset from the fuselage skin, which fades to nothing above the wing root and
     * swells to its full depth below it. Because the offset goes smoothly to zero, the
     * fairing merges into the side of the fuselage with no edge to see - which is what the
     * real one does, and the reason a wing root on an airliner looks like one continuous
     * surface rather than two parts meeting. How far it stands out, at each angle and
     * along the length, is FAIRING_SECTION and FAIRING_LENGTH.
     */
    private void buildWingBodyFairing(B b) {
        double sFront = 19.2, sBack = 37.2;
        int stations = 60, segments = 120;   // 3 degrees, fine enough for the wall and its corner

        for (int i = 0; i < stations; i++) {
            double s0 = lerp(sFront, sBack, (double) i / stations);
            double s1 = lerp(sFront, sBack, (double) (i + 1) / stations);
            double u0 = s0 / LENGTH, u1 = s1 / LENGTH;
            double p0 = FAIRING_LENGTH.at(s0), p1 = FAIRING_LENGTH.at(s1);

            for (int j = 0; j < segments; j++) {
                double a0 = -Math.PI + 2 * Math.PI * j / segments;
                double a1 = -Math.PI + 2 * Math.PI * (j + 1) / segments;

                double o00 = fairingOffset(a0, p0), o01 = fairingOffset(a1, p0);
                double o10 = fairingOffset(a0, p1), o11 = fairingOffset(a1, p1);

                // nothing to draw where the fairing has faded into the skin
                if (o00 == 0 && o01 == 0 && o10 == 0 && o11 == 0) continue;

                b.quad(fus(b, u0, a0, o00), fus(b, u0, a1, o01),
                       fus(b, u1, a1, o11), fus(b, u1, a0, o10), materials.white);
            }
        }
    }

    /** How proud of the skin the fairing stands at this angle round from the crown. */
    private static double fairingOffset(double theta, double depth) {
        double excess = FAIRING_SECTION.at(Math.toDegrees(Math.abs(normaliseAngle(theta)))) * depth;
        if (excess < 0.001) return 0;

        // a hair of lift everywhere it is drawn, so it never lands exactly on the skin
        // underneath and fights with it for the same pixels
        return 0.003 + excess;
    }

    // ================================================================================
    // TAIL
    // ================================================================================

    /** Tailplane chord at x metres out from the centreline. */
    private static double tailChord(double x) {
        return TAIL_TE.at(x) - TAIL_LE.at(x);
    }

    private void buildHorizontalStabiliser(B b, int side) {
        /* Chord line 0.45 m above the axis at the centreline - right on the midline of
         * the tail cone there - and rising at 7.6 degrees, both off the front view. */
        double rootY = CENTRE_HEIGHT + 0.45;
        double rise = Math.tan(Math.toRadians(7.6));

        loft(b, tailStations, airfoilPoints, materials.wingUpper, materials.wingLower, (eta, n, out) -> {
            double[] p = new double[2];
            double x = TAIL_SEMI_SPAN * eta;
            double chord = tailChord(x);
            double zLE = NOSE_Z + TAIL_LE.at(x);
            double y = rootY + rise * x;

            for (int k = 0; k < n; k++) {
                // symmetric section - a tailplane has to work both ways up
                airfoil(2 * Math.PI * k / n, 0.115 - 0.028 * eta, 0, p);
                out[k] = b.v(side * x, y + p[1] * chord, zLE + p[0] * chord);
            }
        }, true);

        // elevator hinge line, from where the tailplane leaves the tail cone
        double[] p = new double[2];
        int steps = 20;
        for (boolean upper : new boolean[]{true, false}) {
            double a = chordToParam(0.70, upper);
            airfoil(a, 0.10, 0, p);
            for (int i = 0; i < steps; i++) {
                double x0 = TAIL_SEMI_SPAN * (0.19 + 0.77 * i / steps);
                double x1 = TAIL_SEMI_SPAN * (0.19 + 0.77 * (i + 1) / steps);
                double c0 = tailChord(x0), c1 = tailChord(x1);
                double y0 = rootY + rise * x0 + p[1] * c0, y1 = rootY + rise * x1 + p[1] * c1;
                double z0 = NOSE_Z + TAIL_LE.at(x0) + p[0] * c0, z1 = NOSE_Z + TAIL_LE.at(x1) + p[0] * c1;
                double off = upper ? 0.012 : -0.012;
                b.quad(b.v(side * x0, y0 + off, z0 - 0.03), b.v(side * x0, y0 + off, z0 + 0.03),
                       b.v(side * x1, y1 + off, z1 + 0.03), b.v(side * x1, y1 + off, z1 - 0.03), materials.wingSeam);
            }
        }
    }

    /** Fin chord at height h above the axis. */
    private static double finChord(double h) {
        return FIN_TE.at(h) - FIN_LE.at(h);
    }

    /**
     * Fin thickness to chord at height h.
     *
     * Taken from the front view, where the fin's outline is its thickness: 0.67 m across
     * 3 m above the axis, tapering in a straight line to 0.23 m at 11 m. A thickness ratio
     * applied to the chord instead - which is how this started - made the base of the
     * fin a third too fat, because the chord there includes the long dorsal fillet. Near
     * the tip the thickness is capped so the rounded tip closes thin.
     */
    private static double finThickness(double h) {
        double chord = finChord(h);
        double half = Math.min(0.334 - (h - 3.0) * (0.334 - 0.115) / 8.0, 0.045 * chord);
        return 2 * half / chord;
    }

    private void buildVerticalStabiliser(B b) {
        loft(b, tailStations * 2, airfoilPoints, materials.white, materials.white, (eta, n, out) -> {
            double[] p = new double[2];
            double h = lerp(FIN_ROOT_H, FIN_TIP_H, eta);
            double chord = finChord(h);
            double zLE = NOSE_Z + FIN_LE.at(h);
            double t = finThickness(h);

            for (int k = 0; k < n; k++) {
                airfoil(2 * Math.PI * k / n, t, 0, p);
                // a wing stood on its edge, so thickness runs across X
                out[k] = b.v(p[1] * chord, CENTRE_HEIGHT + h, zLE + p[0] * chord);
            }
        }, true);

        // rudder hinge line - a straight line on the drawing, from 55.78 m at the root to
        // 61.06 m near the tip
        double[] p = new double[2];
        int steps = 26;
        for (int sign = -1; sign <= 1; sign += 2) {
            for (int i = 0; i < steps; i++) {
                double h0 = lerp(2.6, 11.3, (double) i / steps), h1 = lerp(2.6, 11.3, (double) (i + 1) / steps);
                double z0 = rudderHinge(h0), z1 = rudderHinge(h1);
                double c0 = finChord(h0), c1 = finChord(h1);

                airfoil(chordToParam((z0 - FIN_LE.at(h0)) / c0, sign > 0), finThickness(h0), 0, p);
                double x0 = p[1] * c0 + sign * 0.012;
                airfoil(chordToParam((z1 - FIN_LE.at(h1)) / c1, sign > 0), finThickness(h1), 0, p);
                double x1 = p[1] * c1 + sign * 0.012;

                b.quad(b.v(x0, CENTRE_HEIGHT + h0, NOSE_Z + z0 - 0.04),
                       b.v(x0, CENTRE_HEIGHT + h0, NOSE_Z + z0 + 0.04),
                       b.v(x1, CENTRE_HEIGHT + h1, NOSE_Z + z1 + 0.04),
                       b.v(x1, CENTRE_HEIGHT + h1, NOSE_Z + z1 - 0.04), materials.wingSeam);
            }
        }
    }

    private static double rudderHinge(double h) {
        return 55.78 + (h - 2.43) * (61.06 - 55.78) / (11.47 - 2.43);
    }

    // ================================================================================
    // ENGINES
    // ================================================================================

    private void buildEngine(B b, int side) {
        double cx = side * ENGINE_X;
        double cy = ENGINE_Y;
        double eta = ENGINE_X / SEMI_SPAN;
        double zFront = NOSE_Z + ENGINE_LIP + 0.10;   // the cowl profile starts 0.10 ahead of its origin

        buildNacelle(b, cx, cy, zFront);
        buildPylon(b, cx, cy, zFront, eta);
    }

    /** Triangle wave in [0,1], peaking in the middle of each of count repeats. */
    private static double chevronWave(double theta, int count) {
        double frac = (theta * count / (2 * Math.PI)) % 1.0;
        if (frac < 0) frac += 1;
        return Math.pow(1.0 - Math.abs(2 * frac - 1.0), 0.85);
    }

    private void buildNacelle(B b, double cx, double cy, double zFront) {
        int segments = nacelleSegments;

        /* Outer cowl: distance aft of the lip plane, and the radius there. It swells fast
         * behind the lip and then tapers. That fat, almost blunt leading lip is what lets
         * a high bypass fan swallow air cleanly at low speed with the nose high.
         *
         * Radii were already right - the drawing's lip and maximum radii are 1.56 and
         * 1.95 - but the cowl was 14% too short: on the drawing the chevrons start 5.31 m
         * behind the lip, not 4.65. */
        double[] outZ = {-0.10, 0.014, 0.128, 0.357, 0.700, 1.271, 2.070, 2.984, 3.898, 4.697, 5.210};
        double[] outR = {1.555, 1.642, 1.744, 1.841, 1.914, 1.950, 1.944, 1.903, 1.828, 1.726, 1.642};

        // inner duct, from the lip back to the fan face
        double[] inZ = {-0.10, 0.014, 0.151, 0.414, 0.871, 1.442, 1.842};
        double[] inR = {1.555, 1.420, 1.390, 1.372, 1.372, 1.404, 1.410};

        for (int i = 0; i < outZ.length - 1; i++) {
            Material m = i <= 1 ? materials.nacelleLip : materials.nacelle;
            for (int j = 0; j < segments; j++) {
                double t0 = 2 * Math.PI * j / segments, t1 = 2 * Math.PI * (j + 1) / segments;
                cowlRing(b, cx, cy, zFront, outZ[i], outR[i], outZ[i + 1], outR[i + 1], t0, t1, m);
            }
        }

        /* The chevrons. Serrating the trailing edge lets the fast fan air and the slow
         * outside air mix gradually instead of all at once, and that is most of why a 787
         * is so much quieter on departure than the generation before it. It is also the
         * single most recognisable thing about the aeroplane from the ground. */
        double zBase = outZ[outZ.length - 1];
        double rBase = outR[outR.length - 1];
        double depth = 0.36;
        int count = 16;
        double k = cowlSquash(zBase);

        for (int j = 0; j < segments; j++) {
            double t0 = 2 * Math.PI * j / segments, t1 = 2 * Math.PI * (j + 1) / segments;
            double z0 = zBase + depth * chevronWave(t0, count);
            double z1 = zBase + depth * chevronWave(t1, count);
            double rTip0 = rBase - 0.055 * chevronWave(t0, count);
            double rTip1 = rBase - 0.055 * chevronWave(t1, count);

            // outer skin of the serration
            b.quad(b.v(cx + k * rBase * Math.cos(t0), cy + rBase * Math.sin(t0), zFront + zBase),
                   b.v(cx + k * rBase * Math.cos(t1), cy + rBase * Math.sin(t1), zFront + zBase),
                   b.v(cx + k * rTip1 * Math.cos(t1), cy + rTip1 * Math.sin(t1), zFront + z1),
                   b.v(cx + k * rTip0 * Math.cos(t0), cy + rTip0 * Math.sin(t0), zFront + z0),
                   materials.nacelle);

            // and its inner face, so the serration has thickness
            double rIn0 = rTip0 - 0.055, rIn1 = rTip1 - 0.055;
            b.quad(b.v(cx + k * rTip0 * Math.cos(t0), cy + rTip0 * Math.sin(t0), zFront + z0),
                   b.v(cx + k * rTip1 * Math.cos(t1), cy + rTip1 * Math.sin(t1), zFront + z1),
                   b.v(cx + k * rIn1 * Math.cos(t1), cy + rIn1 * Math.sin(t1), zFront + z1),
                   b.v(cx + k * rIn0 * Math.cos(t0), cy + rIn0 * Math.sin(t0), zFront + z0),
                   materials.intake);
        }

        // thrust reverser cowl seam
        for (int j = 0; j < segments; j++) {
            double t0 = 2 * Math.PI * j / segments, t1 = 2 * Math.PI * (j + 1) / segments;
            double r = interp(outZ, outR, 2.23) + 0.004;
            cowlRing(b, cx, cy, zFront, 2.23, r, 2.255, r, t0, t1, materials.panelLine);
        }

        // inner duct
        for (int i = 0; i < inZ.length - 1; i++) {
            for (int j = 0; j < segments; j++) {
                double t0 = 2 * Math.PI * j / segments, t1 = 2 * Math.PI * (j + 1) / segments;
                ring(b, cx, cy, zFront, inZ[i], inR[i], inZ[i + 1], inR[i + 1], t0, t1,
                        i == 0 ? materials.nacelleLip : materials.intake);
            }
        }

        buildFan(b, cx, cy, zFront + inZ[inZ.length - 1], inR[inR.length - 1]);

        // core cowl and the exhaust nozzle behind the fan cowl, ending 6.2 m behind the lip
        double[] coreZ = {3.60, 4.70, 5.57, 6.11};
        double[] coreR = {1.100, 0.970, 0.800, 0.540};
        for (int i = 0; i < coreZ.length - 1; i++) {
            Material m = i < 2 ? materials.nacelle : materials.hotMetal;
            for (int j = 0; j < segments; j++) {
                double t0 = 2 * Math.PI * j / segments, t1 = 2 * Math.PI * (j + 1) / segments;
                ring(b, cx, cy, zFront, coreZ[i], coreR[i], coreZ[i + 1], coreR[i + 1], t0, t1, m);
            }
        }

        // the plug in the middle of the core nozzle, its tip 7.75 m behind the lip
        for (int j = 0; j < segments; j++) {
            double t0 = 2 * Math.PI * j / segments, t1 = 2 * Math.PI * (j + 1) / segments;
            b.tri(b.v(cx, cy, zFront + 7.65),
                  b.v(cx + 0.45 * Math.cos(t1), cy + 0.45 * Math.sin(t1), zFront + 5.95),
                  b.v(cx + 0.45 * Math.cos(t0), cy + 0.45 * Math.sin(t0), zFront + 5.95), materials.exhaust);
        }
    }

    /** The fan: a disc of swept blades, with the spinner in front of them. */
    private void buildFan(B b, double cx, double cy, double fanZ, double fanR) {
        int segments = nacelleSegments;
        double hubR = 0.46;

        // dark disc behind the blades, so you cannot see out the back of the engine
        for (int j = 0; j < segments; j++) {
            double t0 = 2 * Math.PI * j / segments, t1 = 2 * Math.PI * (j + 1) / segments;
            b.tri(b.v(cx, cy, fanZ + 0.55),
                  b.v(cx + fanR * Math.cos(t0), cy + fanR * Math.sin(t0), fanZ + 0.55),
                  b.v(cx + fanR * Math.cos(t1), cy + fanR * Math.sin(t1), fanZ + 0.55), materials.intake);
        }

        /* Eighteen blades, swept and twisted. The sweep is not decoration - a swept fan
         * blade meets the air at a shallower effective angle, which is how a fan tip can
         * run supersonic without the shock tearing its efficiency apart. */
        int blades = 18, span = 7;

        for (int i = 0; i < blades; i++) {
            double base = 2 * Math.PI * i / blades;

            for (int k = 0; k < span; k++) {
                double s0 = (double) k / span, s1 = (double) (k + 1) / span;
                double r0 = hubR + (fanR - hubR) * s0, r1 = hubR + (fanR - hubR) * s1;

                // the blade leans round the disc as it goes out, and untwists
                double a0 = base + 0.30 * s0 * s0, a1 = base + 0.30 * s1 * s1;
                double tw0 = 0.44 * (1 - s0) + 0.09, tw1 = 0.44 * (1 - s1) + 0.09;

                double c0 = Math.cos(a0), sn0 = Math.sin(a0);
                double c1 = Math.cos(a1), sn1 = Math.sin(a1);

                b.quad(b.v(cx + r0 * c0, cy + r0 * sn0, fanZ - tw0),
                       b.v(cx + r1 * c1, cy + r1 * sn1, fanZ - tw1),
                       b.v(cx + r1 * c1, cy + r1 * sn1, fanZ + tw1),
                       b.v(cx + r0 * c0, cy + r0 * sn0, fanZ + tw0), materials.fanBlade);
            }
        }

        // spinner, with the painted spiral that tells ground crew it is turning
        for (int j = 0; j < segments; j++) {
            double t0 = 2 * Math.PI * j / segments, t1 = 2 * Math.PI * (j + 1) / segments;
            boolean mark = (j * 7 / segments) % 7 == 0;
            b.tri(b.v(cx, cy, fanZ - 0.86),
                  b.v(cx + hubR * Math.cos(t1), cy + hubR * Math.sin(t1), fanZ),
                  b.v(cx + hubR * Math.cos(t0), cy + hubR * Math.sin(t0), fanZ),
                  mark ? materials.spinnerMark : materials.spinner);
        }
    }

    /**
     * How much narrower than tall the outer cowl is at distance z behind the lip.
     *
     * The front view draws the cowl 3.81 m wide and 3.89 m tall, but the inlet lip
     * round, so the cowl starts round at the lip and is 2.2% narrower by the time it
     * reaches full radius.
     */
    private static double cowlSquash(double z) {
        double s = Math.max(0, Math.min(1, z / 1.271));
        return 1 - 0.022 * s * s * (3 - 2 * s);
    }

    /** One quad of the outer cowl: like ring(), but narrowed sideways by cowlSquash. */
    private void cowlRing(B b, double cx, double cy, double zBase,
                          double z0, double r0, double z1, double r1, double t0, double t1, Material m) {
        double k0 = cowlSquash(z0), k1 = cowlSquash(z1);
        b.quad(b.v(cx + k0 * r0 * Math.cos(t0), cy + r0 * Math.sin(t0), zBase + z0),
               b.v(cx + k0 * r0 * Math.cos(t1), cy + r0 * Math.sin(t1), zBase + z0),
               b.v(cx + k1 * r1 * Math.cos(t1), cy + r1 * Math.sin(t1), zBase + z1),
               b.v(cx + k1 * r1 * Math.cos(t0), cy + r1 * Math.sin(t0), zBase + z1), m);
    }

    /** One quad of a surface of revolution about an axis running fore and aft. */
    private void ring(B b, double cx, double cy, double zBase,
                      double z0, double r0, double z1, double r1, double t0, double t1, Material m) {
        b.quad(b.v(cx + r0 * Math.cos(t0), cy + r0 * Math.sin(t0), zBase + z0),
               b.v(cx + r0 * Math.cos(t1), cy + r0 * Math.sin(t1), zBase + z0),
               b.v(cx + r1 * Math.cos(t1), cy + r1 * Math.sin(t1), zBase + z1),
               b.v(cx + r1 * Math.cos(t0), cy + r1 * Math.sin(t0), zBase + z1), m);
    }

    private static double interp(double[] xs, double[] ys, double x) {
        for (int i = 0; i < xs.length - 1; i++) {
            if (x >= xs[i] && x <= xs[i + 1]) {
                double s = (x - xs[i]) / (xs[i + 1] - xs[i]);
                return ys[i] + (ys[i + 1] - ys[i]) * s;
            }
        }
        return ys[ys.length - 1];
    }

    /** The strut carrying the engine up and forward of the wing, with an airfoil section. */
    private void buildPylon(B b, double cx, double cy, double zFront, double eta) {
        double wingY = WING_ROOT_Y + WING_RISE.at(eta);
        double wingZ = leadingEdgeZ(eta);

        int steps = 16, n = 20;
        double[] p = new double[2];
        P[] previous = new P[n], current = new P[n];

        for (int i = 0; i <= steps; i++) {
            double s = (double) i / steps;

            double y = lerp(cy + NACELLE_MAX_R * 0.78, wingY + 0.25, s);
            double zLE = lerp(zFront + 1.05, wingZ - 0.45, s);
            double chord = lerp(3.95, 4.60, s);
            double thick = lerp(0.20, 0.11, s);

            for (int k = 0; k < n; k++) {
                airfoil(2 * Math.PI * k / n, thick, 0, p);
                current[k] = b.v(cx + p[1] * chord, y, zLE + p[0] * chord);
            }

            if (i > 0) {
                for (int k = 0; k < n; k++) {
                    int k2 = (k + 1) % n;
                    b.quad(previous[k], previous[k2], current[k2], current[k], materials.pylon);
                }
            }

            P[] swap = previous;
            previous = current;
            current = swap;
        }
    }

    // ================================================================================
    // LANDING GEAR
    // ================================================================================

    /** A capped cylinder between two points, with an arbitrary axis. */
    private void tube(B b, P from, P to, double radius, int segments, Material m) {
        double ax = to.x - from.x, ay = to.y - from.y, az = to.z - from.z;
        double len = Math.sqrt(ax * ax + ay * ay + az * az);
        if (len < 1e-9) return;
        ax /= len; ay /= len; az /= len;

        // any direction not parallel to the axis will do to start the frame
        double ux, uy, uz;
        if (Math.abs(ay) < 0.9) { ux = -az; uy = 0; uz = ax; }
        else { ux = 1; uy = 0; uz = 0; }
        double ul = Math.sqrt(ux * ux + uy * uy + uz * uz);
        ux /= ul; uy /= ul; uz /= ul;

        double vx = ay * uz - az * uy, vy = az * ux - ax * uz, vz = ax * uy - ay * ux;

        for (int j = 0; j < segments; j++) {
            double t0 = 2 * Math.PI * j / segments, t1 = 2 * Math.PI * (j + 1) / segments;
            double c0 = Math.cos(t0) * radius, s0 = Math.sin(t0) * radius;
            double c1 = Math.cos(t1) * radius, s1 = Math.sin(t1) * radius;

            P a0 = new P(from.x + ux * c0 + vx * s0, from.y + uy * c0 + vy * s0, from.z + uz * c0 + vz * s0);
            P a1 = new P(from.x + ux * c1 + vx * s1, from.y + uy * c1 + vy * s1, from.z + uz * c1 + vz * s1);
            P b0 = new P(to.x + ux * c0 + vx * s0, to.y + uy * c0 + vy * s0, to.z + uz * c0 + vz * s0);
            P b1 = new P(to.x + ux * c1 + vx * s1, to.y + uy * c1 + vy * s1, to.z + uz * c1 + vz * s1);

            b.rawQuad(a0, a1, b1, b0, m);
            b.rawTri(from, a1, a0, m);
            b.rawTri(to, b0, b1, m);
        }
    }

    /** A thin spike, for probes and static wicks. */
    private void spike(B b, P from, P to, double radius, Material m) {
        tube(b, from, to, radius, 5, m);
    }

    /* LANDING GEAR - reworked in the port. The CPU builder's gear had the main strut fat at the
     * bottom and slim at the top (a real oleo is the other way round), a bogie beam floating above
     * its axles, single-rod torque links off to one side, a straight side brace, a strut door that
     * was a 2.3 m slab hiding the whole leg, and flat discs for hubs. This version builds:
     *   - shock struts as a fat outer barrel, a gland nut, and the chrome sliding tube below it,
     *     ending in a fork that carries the axle (nose) or the bogie pivot (main)
     *   - torque links as a scissor: two arms of paired plates meeting at an apex bolt, ahead
     *     of the strut
     *   - main gear: the bogie beam running through its pivot and both axles, a folding side
     *     brace with its knee and lock link, the drag strut, a truck positioner holding the
     *     bogie's tilt, and a properly sized strut door with a thickness
     *   - nose gear: a steering collar, and an A-frame drag brace with its lock link
     *   - wheels: a near-square tyre section, a dished hub with tie bolts and an axle cap
     *     outboard, the brake stack inboard on the main gear (nose wheels have no brakes) */

    /** A wheel: a tyre with a rounded profile, a dished hub with its tie bolts and axle cap on the
     * outboard face, and - main gear only, nose wheels have none - the brake stack inboard. The
     * axle runs along x; inboardSign says which way is inboard. */
    private void wheel(B b, double cx, double cy, double cz, double radius, double halfWidth, int inboardSign, boolean brakes) {
        int segments = wheelSegments;
        /* The tyre's section from the middle of the tread out to the bead, as {lateral, in half
         * widths; radius, in tyre radii}. Aircraft tyres are nearly square in section: a wide flat
         * tread, a tight shoulder, a straight sidewall. */
        double[][] tyre = {{0.0, 1.0}, {0.62, 1.0}, {0.84, 0.988}, {0.96, 0.955}, {1.0, 0.90}, {1.0, 0.74}, {0.95, 0.64}, {0.90, 0.60}};
        // then the hub, from the rim flange in: dished towards the axle, finished with a domed cap
        double[][] hub = {{0.92, 0.565}, {0.80, 0.50}, {0.72, 0.36}, {0.72, 0.16}, {0.80, 0.14}, {0.84, 0.10}, {0.86, 0.0}};

        for (int s = -1; s <= 1; s += 2) {
            P[] previous = null;
            for (int k = 0; k < tyre.length + hub.length; k++) {
                boolean isTyre = k < tyre.length;
                double[] pt = isTyre ? tyre[k] : hub[k - tyre.length];
                double lx = cx + s * pt[0] * halfWidth, r = pt[1] * radius;
                P[] ring;
                if (r == 0) {
                    ring = new P[]{b.v(lx, cy, cz)};
                } else {
                    ring = new P[segments];
                    for (int j = 0; j < segments; j++) {
                        double t = 2 * Math.PI * j / segments;
                        ring[j] = b.v(lx, cy + r * Math.cos(t), cz + r * Math.sin(t));
                    }
                }
                if (previous != null) {
                    Material m = isTyre ? materials.tyre : materials.hub;
                    for (int j = 0; j < segments; j++) {
                        int j2 = (j + 1) % segments;
                        if (ring.length == 1) b.tri(previous[j], previous[j2], ring[0], m);
                        else                  b.quad(previous[j], previous[j2], ring[j2], ring[j], m);
                    }
                }
                previous = ring;
            }
        }

        // tie bolts round the outboard hub face, standing just proud of the dish
        int outboard = -inboardSign;
        for (int n = 0; n < 12; n++) {
            double t = 2 * Math.PI * n / 12, y = cy + 0.42 * radius * Math.cos(t), z = cz + 0.42 * radius * Math.sin(t);
            tube(b, b.v(cx + outboard * 0.72 * halfWidth, y, z), b.v(cx + outboard * 0.80 * halfWidth, y, z), 0.022, 6, materials.gearDark);
        }

        // the brake stack, inboard
        if (brakes) tube(b,b.v(cx + inboardSign * halfWidth * 0.55, cy, cz),
                b.v(cx + inboardSign * halfWidth * 1.02, cy, cz), radius * 0.48, segments, materials.brake);
    }

    /** Torque links: the scissor that stops a strut's sliding tube turning in its barrel. Two arms,
     * each a pair of plates 2 * gap apart, run from lugs on the barrel (yTop) and the fork (yBottom)
     * - lugOut from the strut's axis - to an apex bolt 'reach' ahead (dir -1) or behind (+1). */
    private void scissor(B b, double x, double z, double yTop, double yBottom, int dir,
                         double lugOut, double reach, double gap, double plate, Material m) {
        double yApex = (yTop + yBottom) * 0.5, zLug = z + dir * lugOut, zApex = z + dir * reach;
        for (int p = -1; p <= 1; p += 2) {
            double px = x + p * gap;
            tube(b, b.v(px, yTop, zLug), b.v(px, yApex, zApex), plate, 7, m);
            tube(b, b.v(px, yApex, zApex), b.v(px, yBottom, zLug), plate, 7, m);
        }
        tube(b, b.v(x - gap - 0.03, yApex, zApex), b.v(x + gap + 0.03, yApex, zApex), plate * 1.25, 8, m); // apex bolt
    }

    /** A flat panel with a thickness: four corners in aircraft coordinates, in order, and the
     * offset (tx, ty, tz) to its back face. */
    private void slab(B b, double[][] c, double tx, double ty, double tz, Material m) {
        P[] front = new P[4], back = new P[4];
        for (int i = 0; i < 4; i++) {
            front[i] = b.v(c[i][0], c[i][1], c[i][2]);
            back[i] = b.v(c[i][0] + tx, c[i][1] + ty, c[i][2] + tz);
        }
        b.quad(front[0], front[1], front[2], front[3], m);
        b.quad(back[0], back[1], back[2], back[3], m);
        for (int i = 0; i < 4; i++) {
            int j = (i + 1) % 4;
            b.quad(front[i], front[j], back[j], back[i], m);
        }
    }

    private void buildNoseGear(B b) {
        double z = NOSE_Z + NOSE_GEAR_U * LENGTH;
        double keel = CENTRE_HEIGHT + KEEL.at(NOSE_GEAR_U);

        /* The aircraft is built level and then pitched nose down about the main wheels,
         * which drops the nose gear by the wheelbase times tan(pitch). Build the leg that
         * much longer, so after the pitch the tyres land exactly on the ground. */
        double axle = NOSE_WHEEL_R + (MAIN_GEAR_U - NOSE_GEAR_U) * LENGTH * Math.tan(PITCH);
        double glandY = axle + 0.80; // foot of the outer barrel

        // the bay the leg comes out of
        for (int j = 0; j < cylinderSegments; j++) {
            double t0 = 2 * Math.PI * j / cylinderSegments, t1 = 2 * Math.PI * (j + 1) / cylinderSegments;
            b.quad(b.v(0.52 * Math.cos(t0), keel + 0.02, z + 0.52 * Math.sin(t0)),
                   b.v(0.52 * Math.cos(t1), keel + 0.02, z + 0.52 * Math.sin(t1)),
                   b.v(0.40 * Math.cos(t1), keel + 0.55, z + 0.40 * Math.sin(t1)),
                   b.v(0.40 * Math.cos(t0), keel + 0.55, z + 0.40 * Math.sin(t0)), materials.gearDark);
        }

        // shock strut: barrel, steering collar, gland nut, chrome sliding tube, axle fork
        tube(b, b.v(0, keel + 0.10, z), b.v(0, glandY, z), 0.150, cylinderSegments, materials.gearStrut);
        tube(b, b.v(0, glandY + 0.46, z), b.v(0, glandY + 0.30, z), 0.205, cylinderSegments, materials.gearDark);
        tube(b, b.v(0, glandY + 0.03, z), b.v(0, glandY - 0.05, z), 0.172, cylinderSegments, materials.gearDark);
        tube(b, b.v(0, glandY, z), b.v(0, axle + 0.16, z), 0.105, cylinderSegments, materials.chrome);
        b.box(-0.13, 0.13, axle - 0.09, axle + 0.20, z - 0.15, z + 0.15, materials.gearStrut);

        // torque links ahead of the strut, between the tyres
        scissor(b, 0.0, z, glandY - 0.03, axle + 0.18, -1, 0.15, 0.42, 0.045, 0.036, materials.gearDark);

        /* Drag brace: the leg retracts forwards, so it is braced by an A-frame running forward and
         * up from the barrel to a hinge tube across the front of the bay, with a lock link from
         * the A-frame's crossbar back down to the barrel. */
        P braceFoot = b.v(0, glandY + 0.20, z - 0.14);
        double hingeY = keel + 0.12, hingeZ = z - 1.35;
        for (int s = -1; s <= 1; s += 2) {
            tube(b, braceFoot, b.v(s * 0.38, hingeY, hingeZ), 0.068, 8, materials.gearDark);
        }
        tube(b, b.v(-0.44, hingeY, hingeZ), b.v(0.44, hingeY, hingeZ), 0.06, 8, materials.gearDark);
        double cy = lerp(glandY + 0.20, hingeY, 0.5), cz = lerp(z - 0.14, hingeZ, 0.5);
        tube(b, b.v(-0.19, cy, cz), b.v(0.19, cy, cz), 0.045, 8, materials.gearDark);            // crossbar
        P lockKnee = b.v(0, cy - 0.30, lerp(cz, z - 0.15, 0.55));
        tube(b, b.v(0, cy, cz), lockKnee, 0.032, 6, materials.gearStrut);
        tube(b, lockKnee, b.v(0, glandY + 0.06, z - 0.15), 0.032, 6, materials.gearStrut);

        // axle and twin wheels, centred 0.36 m either side as the front view draws them
        tube(b, b.v(-0.40, axle, z), b.v(0.40, axle, z), 0.075, 8, materials.gearStrut);
        wheel(b, -0.362, axle, z, NOSE_WHEEL_R, NOSE_WHEEL_HALF, +1, false);
        wheel(b, +0.362, axle, z, NOSE_WHEEL_R, NOSE_WHEEL_HALF, -1, false);

        // taxi and take-off lights on the leg: a lamp housing each, lens facing forward
        for (int side = -1; side <= 1; side += 2) {
            double x = side * 0.21, y = axle + 1.15;
            tube(b, b.v(x, y, z - 0.04), b.v(x, y, z - 0.18), 0.085, 12, materials.gearDark);
            lens(b, x, y, z - 0.185, 0, 0, -1, 0.07, landingLightsOn ? materials.landing : materials.lensOff);
        }

        /* The doors hanging open either side of the bay: small panels ahead of the leg,
         * 1.3 m long and hanging 0.3 m, as the side view draws them. They were 2.1 m long
         * and hung 0.75 m, which read as a grey box bolted under the nose. */
        for (int side = -1; side <= 1; side += 2) {
            b.quad(b.v(side * 0.40, keel + 0.03, z - 1.13), b.v(side * 0.40, keel + 0.03, z + 0.15),
                   b.v(side * 0.47, keel - 0.30, z + 0.12), b.v(side * 0.47, keel - 0.30, z - 1.08),
                   materials.white);
        }
    }

    private void buildMainGear(B b, int side) {
        double x = side * MAIN_GEAR_X;
        double z = NOSE_Z + MAIN_GEAR_U * LENGTH;
        double axle = MAIN_WHEEL_R;
        double tilt = Math.tan(PITCH);
        double topY = WING_ROOT_Y + 0.55;   // the trunnion, up inside the wing
        double pivotY = axle + 0.10;        // bogie pivot, just above the axle line
        double glandY = pivotY + 1.00;      // foot of the outer barrel

        /* Shock strut: a fat outer barrel from the trunnion down, a gland nut at its foot, the
         * chrome sliding tube showing below it, and the fork that carries the bogie pivot. */
        tube(b, b.v(x, topY, z), b.v(x, glandY, z), 0.205, cylinderSegments, materials.gearStrut);
        tube(b, b.v(x, glandY + 0.03, z), b.v(x, glandY - 0.06, z), 0.235, cylinderSegments, materials.gearDark);
        tube(b, b.v(x, glandY, z), b.v(x, pivotY + 0.26, z), 0.145, cylinderSegments, materials.chrome);
        b.box(x - 0.17, x + 0.17, pivotY - 0.14, pivotY + 0.32, z - 0.21, z + 0.21, materials.gearStrut);

        // torque links ahead of the strut
        scissor(b, x, z, glandY - 0.02, pivotY + 0.26, -1, 0.21, 0.58, 0.055, 0.05, materials.gearDark);

        /* Side brace: a folding stay from the lower barrel up and inboard to the wheel well,
         * locked almost straight - two members meeting at a knee - with the lock link from the
         * knee back down to the barrel. */
        double sx = x - side * 0.19, sy = glandY + 0.45;
        double wx = side * 2.95, wy = CENTRE_HEIGHT - 1.45, wz = z + 0.30;
        double kx = lerp(sx, wx, 0.55), ky = lerp(sy, wy, 0.55) - 0.10, kz = lerp(z, wz, 0.55);
        P knee = b.v(kx, ky, kz);
        tube(b, b.v(wx, wy, wz), knee, 0.105, 9, materials.gearDark);
        tube(b, knee, b.v(sx, sy, z), 0.090, 9, materials.gearDark);
        b.sphere(knee, 0.13, materials.gearDark);
        P lockKnee = b.v(lerp(kx, sx, 0.5), ky - 0.32, lerp(kz, z, 0.5));
        tube(b, knee, lockKnee, 0.04, 6, materials.gearStrut);
        tube(b, lockKnee, b.v(sx, glandY + 0.12, z), 0.04, 6, materials.gearStrut);

        // drag strut, running forward and up into the wing
        tube(b, b.v(x, glandY + 0.55, z), b.v(x - side * 0.20, WING_ROOT_Y + 0.15, z - 1.95), 0.095, 9, materials.gearDark);

        /* Bogie beam, through the pivot fork and both axles. It pivots on the strut, so with the
         * aircraft pitched nose down it tilts nose up by the same angle relative to the airframe
         * and all four tyres stay on the ground. */
        tube(b, b.v(x, pivotY + 0.95 * tilt, z - 0.95), b.v(x, pivotY - 0.95 * tilt, z + 0.95),
                0.12, cylinderSegments, materials.gearStrut);

        /* Each axle carries its two tyres 0.76 m either side of the leg - the front view
         * puts their sidewalls at 3.88-4.42 and 5.41-5.94 m out. */
        double spread = 0.7625;
        for (int a = -1; a <= 1; a += 2) {
            double az = z + a * 0.78;
            double ay = axle - a * 0.78 * tilt;
            tube(b, b.v(x - spread - 0.22, ay, az), b.v(x + spread + 0.22, ay, az), 0.09, 9, materials.gearStrut);
            wheel(b, x - spread, ay, az, MAIN_WHEEL_R, MAIN_WHEEL_HALF, +1, true);
            wheel(b, x + spread, ay, az, MAIN_WHEEL_R, MAIN_WHEEL_HALF, -1, true);
        }

        // truck positioner: an actuator from the back of the barrel to the aft end of the beam
        P tpTop = b.v(x, glandY - 0.05, z + 0.21);
        P tpMid = b.v(x, lerp(glandY - 0.05, pivotY + 0.10, 0.5), z + 0.42);
        tube(b, tpTop, tpMid, 0.07, 8, materials.gearDark);
        tube(b, tpMid, b.v(x, pivotY - 0.62 * tilt + 0.10, z + 0.62), 0.04, 8, materials.chrome);

        /* The strut door, carried on the outboard side of the leg: a panel from up inside the wing
         * - so no barrel shows above it - down to the barrel's foot, 1.4 m long, splayed out a
         * little at the bottom. It was a 2.3 m sheet hanging to the tyres, which hid the leg. */
        double xo = x + side * 0.30, splay = side * 0.08;
        double dTop = topY - 0.15, dBottom = glandY + 0.10;
        slab(b, new double[][]{{xo, dTop, z - 0.72}, {xo, dTop, z + 0.72}, {xo + splay, dBottom, z + 0.62}, {xo + splay, dBottom, z - 0.62}},
             -side * 0.035, 0, 0, materials.white);
    }

    // ================================================================================
    // ANTENNAS AND PROBES
    // ================================================================================

    /** A blade antenna: a thin swept fin standing off the skin. */
    private void blade(B b, double u, double theta, double height, double length) {
        P base = fus(b, u, theta, 0);
        P baseAft = fus(b, u + length / LENGTH, theta, 0);
        P tip = fus(b, u + length * 0.55 / LENGTH, theta, height);
        P tipAft = fus(b, u + length * 0.92 / LENGTH, theta, height * 0.85);

        b.tri(base, baseAft, tipAft, materials.antenna);
        b.tri(base, tipAft, tip, materials.antenna);
    }

    private void buildAntennas(B b) {
        // VHF blades on the crown and the belly, plus the TCAS blades
        blade(b, 0.205, 0, 0.34, 0.96);
        blade(b, 0.640, 0, 0.34, 0.96);
        blade(b, 0.620, Math.PI, 0.30, 0.86);
        blade(b, 0.300, 0, 0.21, 0.58);
        blade(b, 0.275, Math.PI, 0.21, 0.58);

        // satcom radome on the crown, behind the wing
        double u = 0.560;
        P base = fus(b, u, 0, 0);
        int segments = cylinderSegments;
        for (int i = 0; i < 6; i++) {
            double s0 = (double) i / 6, s1 = (double) (i + 1) / 6;
            double h0 = 0.22 * Math.sin(s0 * Math.PI * 0.5), h1 = 0.22 * Math.sin(s1 * Math.PI * 0.5);
            double r0 = 0.62 * Math.cos(s0 * Math.PI * 0.5), r1 = 0.62 * Math.cos(s1 * Math.PI * 0.5);
            for (int j = 0; j < segments; j++) {
                double t0 = 2 * Math.PI * j / segments, t1 = 2 * Math.PI * (j + 1) / segments;
                b.quad(b.v(r0 * Math.cos(t0), base.y + h0, base.z + r0 * Math.sin(t0)),
                       b.v(r0 * Math.cos(t1), base.y + h0, base.z + r0 * Math.sin(t1)),
                       b.v(r1 * Math.cos(t1), base.y + h1, base.z + r1 * Math.sin(t1)),
                       b.v(r1 * Math.cos(t0), base.y + h1, base.z + r1 * Math.sin(t0)), materials.antenna);
            }
        }

        // pitot probes and angle of attack vanes on the nose
        for (int side = -1; side <= 1; side += 2) {
            for (int i = 0; i < 2; i++) {
                double pu = 0.072 + i * 0.012;
                double pt = side * Math.toRadians(80 + i * 16);
                spike(b, fus(b, pu, pt, 0), fus(b, pu - 0.0085, pt, 0.30), 0.022, materials.probe);
            }
            // angle of attack vane, ahead of the first door
            spike(b, fus(b, 0.079, side * Math.toRadians(62), 0),
                     fus(b, 0.0775, side * Math.toRadians(62), 0.17), 0.030, materials.probe);
        }

        // static wicks down the trailing edge of the fin
        for (int i = 0; i < 6; i++) {
            double h = lerp(FIN_ROOT_H, FIN_TIP_H, 0.30 + i * 0.11);
            double zTE = NOSE_Z + FIN_TE.at(h);
            spike(b, b.v(0, CENTRE_HEIGHT + h, zTE), b.v(0, CENTRE_HEIGHT + h + 0.03, zTE + 0.18), 0.007, materials.darkSeam);
        }
    }

    // ================================================================================
    // LIGHTS
    // ================================================================================

    /**
     * Exterior lights, where a 787 has them and about the size they really are.
     *
     *   - position lights at each wingtip: red to port and green to starboard in the
     *     leading edge of the raked tip, white at each tip's trailing edge. (The 787 has
     *     no tail light on the APU cone; its aft position lights are these two.)
     *   - strobes beside the white lights
     *   - red anti-collision beacons, low lens domes on the crown and the belly
     *   - landing lights in the wing root leading edges, runway turnoff lights just
     *     inboard of them angled outward, taxi and take-off lights on the nose gear leg
     *
     * Every fitting is built whether it is on or off; off, it is a dark lens.
     */
    private void buildLights(B b) {
        for (int side = -1; side <= 1; side += 2) {
            Material forward = navLightsOn ? (side < 0 ? materials.navRed : materials.navGreen) : materials.lensOff;
            b.sphere(wingPoint(b, side, 0.997, 0.0, 0.0), 0.055, forward);
            b.sphere(wingPoint(b, side, 0.995, Math.PI, 0.0), 0.045, navLightsOn ? materials.navWhite : materials.lensOff);
            if (strobesOn) b.sphere(wingPoint(b, side, 0.985, Math.PI, 0.0), 0.040, materials.strobe);
        }

        Material beacon = beaconsOn ? materials.beaconRed : materials.lensOff;
        crownDome(b, 0.440, +1, 0.10, 0.065, beacon);
        crownDome(b, 16.5 / LENGTH, -1, 0.10, 0.065, beacon);

        Material lamp = landingLightsOn ? materials.landing : materials.lensOff;
        for (int side = -1; side <= 1; side += 2) {
            // landing light, facing straight ahead out of the leading edge
            double e = 3.6 / SEMI_SPAN;
            double x = side * 3.6, y = WING_ROOT_Y + WING_RISE.at(e), z = leadingEdgeZ(e);
            tube(b, b.v(x, y, z + 0.06), b.v(x, y, z - 0.005), 0.10, 16, materials.wingLower);
            lens(b, x, y, z - 0.01, 0, 0, -1, 0.09, lamp);

            // runway turnoff light, inboard of it and angled 30 degrees outward
            double f = 3.3 / SEMI_SPAN;
            double tx = side * 3.3, ty = WING_ROOT_Y + WING_RISE.at(f) - 0.06, tz = leadingEdgeZ(f);
            double ox = side * Math.sin(Math.toRadians(30)), oz = -Math.cos(Math.toRadians(30));
            tube(b, b.v(tx - ox * 0.06, ty, tz - oz * 0.06), b.v(tx + ox * 0.005, ty, tz + oz * 0.005), 0.075, 16, materials.wingLower);
            lens(b, tx + ox * 0.01, ty, tz + oz * 0.01, ox, 0, oz, 0.065, lamp);
        }
    }

    /**
     * A flat round lens at a point, facing a direction, in aircraft coordinates. Its back
     * is inside whatever it is set into, so though emission here is two-sided it only
     * shows forward.
     */
    private void lens(B b, double x, double y, double z, double nx, double ny, double nz, double radius, Material m) {
        // two directions square to the facing direction
        double ax = Math.abs(ny) < 0.9 ? 0 : 1, ay = Math.abs(ny) < 0.9 ? 1 : 0;
        double e1x = ay * nz, e1y = -ax * nz, e1z = ax * ny - ay * nx;
        double l1 = Math.sqrt(e1x * e1x + e1y * e1y + e1z * e1z);
        e1x /= l1; e1y /= l1; e1z /= l1;
        double e2x = ny * e1z - nz * e1y, e2y = nz * e1x - nx * e1z, e2z = nx * e1y - ny * e1x;

        int n = 20;
        P centre = b.v(x, y, z);
        for (int i = 0; i < n; i++) {
            double a0 = 2 * Math.PI * i / n, a1 = 2 * Math.PI * (i + 1) / n;
            double c0 = Math.cos(a0) * radius, s0 = Math.sin(a0) * radius;
            double c1 = Math.cos(a1) * radius, s1 = Math.sin(a1) * radius;
            b.tri(centre,
                  b.v(x + e1x * c0 + e2x * s0, y + e1y * c0 + e2y * s0, z + e1z * c0 + e2z * s0),
                  b.v(x + e1x * c1 + e2x * s1, y + e1y * c1 + e2y * s1, z + e1z * c1 + e2z * s1), m);
        }
    }

    /**
     * A low lens dome on the crown (up = +1) or the belly (up = -1) at station u: a
     * beacon's housing. The whole dome is the lens, so lit, the whole of it glows.
     */
    private void crownDome(B b, double u, int up, double radius, double height, Material m) {
        double[] p = new double[2];
        section(u, up > 0 ? 0 : Math.PI, -0.003, p);   // a hair into the skin, so no gap shows under the rim
        double y0 = CENTRE_HEIGHT + p[1], z0 = NOSE_Z + u * LENGTH;
        int rings = 6, segments = 20;
        for (int r = 0; r < rings; r++) {
            double f0 = Math.PI / 2 * r / rings, f1 = Math.PI / 2 * (r + 1) / rings;
            for (int j = 0; j < segments; j++) {
                double a0 = 2 * Math.PI * j / segments, a1 = 2 * Math.PI * (j + 1) / segments;
                b.quad(domePoint(b, y0, z0, up, radius, height, f0, a0), domePoint(b, y0, z0, up, radius, height, f0, a1),
                       domePoint(b, y0, z0, up, radius, height, f1, a1), domePoint(b, y0, z0, up, radius, height, f1, a0), m);
            }
        }
    }

    private P domePoint(B b, double y0, double z0, int up, double radius, double height, double elevation, double around) {
        double flat = Math.cos(elevation) * radius;
        return b.v(flat * Math.cos(around), y0 + up * Math.sin(elevation) * height, z0 + flat * Math.sin(around));
    }

    // ================================================================================
    // SELF CHECK
    // ================================================================================

    /**
     * What this model actually measures, next to the published figures for a 787-9.
     *
     * Worth having as real code rather than a comment, because the geometry is generated
     * from curves and it is genuinely hard to tell by eye whether a wing drawn from ten
     * control points comes out at the right area. It does not, unless you check: an
     * earlier version of this file had a tip chord of 0.88 m against an actual 1.75, and
     * the wing came out ten percent short on area without looking obviously wrong from
     * any single angle.
     *
     * Sources are Boeing's published figures and the standard derived quantities.
     */
    public static String dimensionReport() {
        StringBuilder out = new StringBuilder();
        out.append(String.format("%-38s %12s %12s %9s%n", "", "model", "published", "error"));

        // --- wing -----------------------------------------------------------
        int steps = 4000;
        double area = 0, chordSquared = 0, h = 1.0 / steps;
        for (int i = 0; i < steps; i++) {
            double eta = (i + 0.5) * h;
            double c = wingChord(eta);
            area += c * h;
            chordSquared += c * c * h;
        }
        double wingArea = 2 * area * SEMI_SPAN;
        double mac = chordSquared / area;
        double span = 2 * SEMI_SPAN;
        double side = HALF_WIDTH / SEMI_SPAN;

        /* The published 32.2 degrees is the quarter chord sweep of the reference
         * trapezoid, which has a straight trailing edge the real wing does not. So convert
         * the real wing's leading edge sweep - straight from the root to 22 m - to the
         * trapezoid's quarter chord, using the published aspect ratio and taper:
         *   tan(c/4) = tan(LE) - (4/AR)(0.25)(1-taper)/(1+taper) */
        double leSweep = Math.atan((WING_LE.at(22 / SEMI_SPAN) - WING_LE.at(4 / SEMI_SPAN)) / 18.0);
        double sweepQuarter = Math.toDegrees(Math.atan(Math.tan(leSweep) - (4 / 9.59) * 0.25 * (1 - 0.147) / (1 + 0.147)));

        row(out, "wingspan (m)", span, 60.12);
        row(out, "wing area (m2)", wingArea, 377.0);
        row(out, "wing chord at fuselage side (m)", wingChord(side), 11.93);
        note(out, "wing tip chord (m)", wingChord(1), "published 1.75 is the reference trapezoid's");
        row(out, "wing aspect ratio", span * span / wingArea, 9.59);
        /* Published tables list "mean aerodynamic chord 6.27 m", but 6.27 is exactly
         * area over span - that is the mean GEOMETRIC chord, which those tables have
         * mislabelled. The true MAC, the chord-squared weighted average, is necessarily
         * larger than it on any tapered wing. Both are reported so neither is mistaken
         * for the other. */
        row(out, "mean geometric chord, S/b (m)", wingArea / span, 6.27);
        note(out, "true mean aerodynamic chord (m)", mac, "");
        note(out, "leading edge sweep (deg)", Math.toDegrees(leSweep), "Boeing 3-view");
        row(out, "sweep at quarter chord (deg)", sweepQuarter, 32.2);
        note(out, "dihedral, 40-90% semi span (deg)",
                Math.toDegrees(Math.atan((WING_RISE.at(0.9) - WING_RISE.at(0.4)) / (0.5 * SEMI_SPAN))),
                "7.5 off Boeing's front view");
        row(out, "root incidence (deg)", -Math.toDegrees(wingTwist(0)), 2.0);

        // --- fuselage -------------------------------------------------------
        double maxWidth = 0, maxHeight = 0;
        for (int i = 0; i <= steps; i++) {
            double u = TAIL_U * i / steps;
            maxWidth = Math.max(maxWidth, 2 * WIDTH.at(u));
            maxHeight = Math.max(maxHeight, CROWN.at(u) - KEEL.at(u));
        }
        row(out, "length, nose to tailplane tip (m)", NOSE_Z + LENGTH / 2 + TAIL_TE.at(TAIL_SEMI_SPAN), 62.81);
        note(out, "fuselage length, to APU exhaust (m)", TAIL_U * LENGTH, "Boeing 3-view");
        row(out, "fuselage width (m)", maxWidth, 5.77);
        row(out, "fuselage height (m)", maxHeight, 5.97);
        note(out, "nose tip below fuselage axis (m)", -CROWN.at(0), "Boeing 3-view: 1.28");

        // --- tail -----------------------------------------------------------
        double tailArea = 0, finArea = 0;
        for (int i = 0; i < steps; i++) {
            double f = (i + 0.5) * h;
            tailArea += tailChord(f * TAIL_SEMI_SPAN) * h * TAIL_SEMI_SPAN;
            double fh = lerp(HALF_HEIGHT * 0.85, FIN_TIP_H, f);
            finArea += finChord(fh) * h * (FIN_TIP_H - HALF_HEIGHT * 0.85);
        }
        note(out, "tailplane span (m)", 2 * TAIL_SEMI_SPAN, "Boeing 3-view");
        // sources disagree badly on tail areas - 52.1 m2 is quoted for the tailplane and
        // 81.3 for the fin, but neither says whether that is exposed, gross, or carried
        // through to the centreline, so there is nothing solid to compare against
        note(out, "tailplane area, gross (m2)", 2 * tailArea, "published 52.1 to 67, convention unclear");
        note(out, "fin area, above the crown (m2)", finArea, "published 81.3, convention unclear");

        /* Overall height is the fin tip above the ground, once the aircraft is pitched
         * 0.8 degrees nose down about its main wheels - which lifts the tail a little. */
        double tipBehindGear = FIN_TE.at(FIN_TIP_H) - MAIN_GEAR_U * LENGTH;
        double height = (CENTRE_HEIGHT + FIN_TIP_H) * Math.cos(PITCH) + tipBehindGear * Math.sin(PITCH);
        row(out, "overall height (m)", height, 17.02);

        // --- gear and doors -------------------------------------------------
        row(out, "wheelbase (m)", (MAIN_GEAR_U - NOSE_GEAR_U) * LENGTH, 25.85);
        row(out, "nose gear aft of nose (m)", NOSE_GEAR_U * LENGTH, 5.401);
        row(out, "main gear aft of nose (m)", MAIN_GEAR_U * LENGTH, 31.254);
        row(out, "main gear track (m)", 2 * MAIN_GEAR_X, 9.80);
        row(out, "main tyre diameter (m)", 2 * MAIN_WHEEL_R, 1.370);
        row(out, "nose tyre diameter (m)", 2 * NOSE_WHEEL_R, 1.143);
        double sill = (CENTRE_HEIGHT + DOOR_SILL) * Math.cos(PITCH)
                + (DOOR_STATIONS[0] - MAIN_GEAR_U * LENGTH) * Math.sin(PITCH);
        note(out, "door 1 sill above ground (m)", sill, "from the 3-view's door outline and ground line");
        row(out, "window pitch (m)", 24 * 0.0254, 0.6096);
        row(out, "window height (m)", 0.470, 0.4674);

        // --- engines --------------------------------------------------------
        row(out, "fan diameter (m)", 2 * 1.410, 2.82);
        note(out, "engine spanwise station (m)", ENGINE_X, "Boeing 3-view, front and plan agree");

        return out.toString();
    }

    /** A measurement with no trustworthy published figure to check it against. */
    private static void note(StringBuilder out, String label, double model, String comment) {
        out.append(String.format("%-38s %12.3f %12s %9s  %s%n", label, model, "-", "", comment));
    }

    private static void row(StringBuilder out, String label, double model, double published) {
        double error = published == 0 ? 0 : (model / published - 1) * 100;
        out.append(String.format("%-38s %12.3f %12.3f %8.2f%%%s%n",
                label, model, published, error, Math.abs(error) > 2.0 ? "  <-- off" : ""));
    }

    // ================================================================================
    // HELPERS
    // ================================================================================

    private static double lerp(double a, double b, double t) {
        return a + (b - a) * t;
    }

    /**
     * Collects geometry, applying the aircraft's position and heading as it goes.
     *
     * Everything above works in the aircraft's own coordinates and never thinks about
     * where the aircraft actually is - v() does that, once, for every vertex.
     *
     * The P objects handed around here are temporary. Triangle copies the numbers out
     * and keeps none of them, so at a third of a million triangles they are a million
     * short lived objects the collector sweeps up immediately, rather than a million live
     * ones sitting in memory for the whole render.
     */
    private static final class B {
        private final List<Triangle> tris;
        private final List<Sphere> balls;
        private final double cos, sin;
        private final double cosPitch, sinPitch, pivotZ;
        private final double ox, oy, oz;

        B(List<Triangle> tris, List<Sphere> balls, P origin, double heading, double pitch, double pivotZ) {
            this.tris = tris;
            this.balls = balls;
            this.cos = Math.cos(heading);
            this.sin = Math.sin(heading);
            this.cosPitch = Math.cos(pitch);
            this.sinPitch = Math.sin(pitch);
            this.pivotZ = pivotZ;
            this.ox = origin.x;
            this.oy = origin.y;
            this.oz = origin.z;
        }

        /**
         * Local aircraft coordinates to world: pitch nose down about the main gear contact
         * patch (y = 0 at pivotZ), rotate about the vertical axis, then move.
         */
        P v(double x, double y, double z) {
            double dz = z - pivotZ;
            double py = y * cosPitch + dz * sinPitch;
            double pz = pivotZ + dz * cosPitch - y * sinPitch;
            return new P(ox + x * cos + pz * sin, oy + py, oz - x * sin + pz * cos);
        }

        /** Emit a triangle whose points are already in world space. */
        void rawTri(P a, P b, P c, Material m) {
            /* Skip triangles with no area. Lofted shapes produce plenty wherever a ring
             * collapses to a point, and each one would otherwise sit in the BVH forever,
             * occupying a leaf slot and failing every test it is given. */
            double e0x = b.x - a.x, e0y = b.y - a.y, e0z = b.z - a.z;
            double e1x = c.x - a.x, e1y = c.y - a.y, e1z = c.z - a.z;

            double nx = e0y * e1z - e0z * e1y;
            double ny = e0z * e1x - e0x * e1z;
            double nz = e0x * e1y - e0y * e1x;

            if (nx * nx + ny * ny + nz * nz < 1e-18) return;

            tris.add(new Triangle(new Vec3((float) a.x, (float) a.y, (float) a.z),
                                  new Vec3((float) b.x, (float) b.y, (float) b.z),
                                  new Vec3((float) c.x, (float) c.y, (float) c.z), m));
        }

        void rawQuad(P a, P b, P c, P d, Material m) {
            rawTri(a, b, c, m);
            rawTri(a, c, d, m);
        }

        // v() has already transformed these, so tri/quad are just the raw versions
        void tri(P a, P b, P c, Material m) { rawTri(a, b, c, m); }
        void quad(P a, P b, P c, P d, Material m) { rawQuad(a, b, c, d, m); }

        /** An axis-aligned box in aircraft coordinates. */
        void box(double x0, double x1, double y0, double y1, double z0, double z1, Material m) {
            P a = v(x0, y0, z0), b = v(x1, y0, z0), c = v(x1, y1, z0), d = v(x0, y1, z0);
            P e = v(x0, y0, z1), f = v(x1, y0, z1), g = v(x1, y1, z1), h = v(x0, y1, z1);
            quad(a, b, c, d, m);
            quad(e, h, g, f, m);
            quad(a, e, f, b, m);
            quad(d, c, g, h, m);
            quad(a, d, h, e, m);
            quad(b, f, g, c, m);
        }

        void sphere(P centre, double radius, Material m) {
            balls.add(new Sphere(new Vec3((float) centre.x, (float) centre.y, (float) centre.z), (float) radius, m));
        }
    }

    /** A point in space, in double precision while the aircraft is being built (the CPU
     * renderer's Vec3 played this part). Colours and the finished triangles use the CUDA
     * build's float Vec3. */
    private static final class P {
        final double x, y, z;
        P(double x, double y, double z) { this.x = x; this.y = y; this.z = z; }
    }
}
