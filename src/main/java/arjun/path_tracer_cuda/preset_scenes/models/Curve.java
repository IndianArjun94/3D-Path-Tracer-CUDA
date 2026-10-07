package arjun.path_tracer_cuda.preset_scenes.models;

/**
 * A smooth curve through a handful of control points.
 *
 * Shapes like a fuselage profile are much easier to describe as "here are twenty points
 * it should pass through" than as a formula, but joining those points with straight
 * lines gives a faceted hull and joining them with a plain cubic spline gives bulges
 * where the spline overshoots between widely spaced points.
 *
 * This is monotone cubic interpolation (the Fritsch-Carlson method): an ordinary cubic
 * Hermite spline, except the slope at each point is chosen so the curve can never
 * overshoot the values on either side of it. Where the data rises, the curve rises;
 * where the data has a peak, the curve has a peak there and not just after it. For
 * shaping a hull that is exactly the behaviour you want - no accidental bulges.
 */
public class Curve {

    private final double[] x;
    private final double[] y;
    private final double[] slope;

    public Curve(double[] x, double[] y) {
        if (x.length != y.length || x.length < 2) {
            throw new IllegalArgumentException("need at least two matching control points");
        }

        this.x = x.clone();
        this.y = y.clone();
        this.slope = new double[x.length];

        int n = x.length;
        double[] h = new double[n - 1];     // spacing between control points
        double[] delta = new double[n - 1]; // straight line slope across each span

        for (int i = 0; i < n - 1; i++) {
            h[i] = x[i + 1] - x[i];
            if (h[i] <= 0) throw new IllegalArgumentException("control points must increase in x");
            delta[i] = (y[i + 1] - y[i]) / h[i];
        }

        slope[0] = delta[0];
        slope[n - 1] = delta[n - 2];

        for (int i = 1; i < n - 1; i++) {
            if (delta[i - 1] * delta[i] <= 0) {
                /* The data turns around here, so this is a local peak or trough. A flat
                 * slope puts the curve's own peak exactly on the control point instead of
                 * letting it sail past and come back. */
                slope[i] = 0;
            } else {
                // weighted harmonic mean of the neighbouring slopes, which keeps the
                // curve inside the range of the data on both sides
                double w1 = 2 * h[i] + h[i - 1];
                double w2 = h[i] + 2 * h[i - 1];
                slope[i] = (w1 + w2) / (w1 / delta[i - 1] + w2 / delta[i]);
            }
        }
    }

    /** Value at t, clamped to the ends of the control points. */
    public double at(double t) {
        int n = x.length;
        if (t <= x[0]) return y[0];
        if (t >= x[n - 1]) return y[n - 1];

        // binary search for the span containing t
        int lo = 0, hi = n - 1;
        while (hi - lo > 1) {
            int mid = (lo + hi) >>> 1;
            if (x[mid] > t) hi = mid;
            else lo = mid;
        }

        double h = x[lo + 1] - x[lo];
        double s = (t - x[lo]) / h;
        double s2 = s * s;
        double s3 = s2 * s;

        // cubic Hermite basis
        double h00 = 2 * s3 - 3 * s2 + 1;
        double h10 = s3 - 2 * s2 + s;
        double h01 = -2 * s3 + 3 * s2;
        double h11 = s3 - s2;

        return h00 * y[lo] + h10 * h * slope[lo] + h01 * y[lo + 1] + h11 * h * slope[lo + 1];
    }
}
