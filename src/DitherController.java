/*
*   Object to control dithering of flat frames.
*   Dithering is moving the scope slightly between frames, so that any small irregularities in the
*   light source are moved around, and thus averaged out, in the final frame.  We do this by taking
*   the first frame exactly on-target, then the next several at points around a circle a certain distance
*   out from the target, then the next several at points around a larger circle, and so on.
*
*   This object manages the circle math and counters for this proces.
 */
public class DitherController {
    //  Starting point - the "true" target location.
    //  We use alt/az, not dec/ra, because flat targets are stationary and terrestrial-based
    private double startAltDeg ;                //  Altitude of target centre
    private double startAzDeg;                  //  Azimuth of target centre

    //  Radius of a dither circle, and maximum we're willing to grow to
    private double ditherRadiusArcSeconds;      //  Dither radius, arc-seconds
    private double ditherRadiusRadians;         //  Radius converted to radians for calculations
    private double ditherMaxRadiusArcSeconds;   //  Maximum radius before restart, in arc-seconds
    private double ditherMaxRadiusRadians;      //  Maximum converted to radians for calculations

    //  Track where we are in the set of points being distributed around the circle
    private int countInSet;
    private double angleRadians;                //  Angle between points around the circle
    private int steps;                          //  How many steps around the circle
    private double currentRadiusRadians;        //  Current dither radius (grows after we complete a circle)

    private static final double ARC_SECONDS_IN_DEGREE = 60.0 * 60.0;

    public DitherController(double startAltDeg, double startAzDeg,
                            double radiusArcSeconds, double maxRadiusArcSeconds) {
        this.startAltDeg = startAltDeg;
        this.startAzDeg = startAzDeg;
        this.ditherRadiusArcSeconds = radiusArcSeconds;
        this.ditherMaxRadiusArcSeconds = maxRadiusArcSeconds;

        //  Convert ref values to radians for easier math
        this.ditherRadiusRadians = Math.toRadians(this.ditherRadiusArcSeconds / ARC_SECONDS_IN_DEGREE);
        this.ditherMaxRadiusRadians = Math.toRadians(this.ditherMaxRadiusArcSeconds / ARC_SECONDS_IN_DEGREE);

        //  Set the position variables to force a new cycle to start on first use
        this.countInSet = 0;
        this.angleRadians = 3.0 * Math.PI;  // More than the 2-pi that is a complete circle
        this.steps = 4;                     // New cycle will double this to 8
        this.currentRadiusRadians = 0.0;    // Will be incremented to 1 radius
    }
}
