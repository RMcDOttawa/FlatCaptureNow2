import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.ImmutableTriple;

/*
*   Object to control dithering of flat frames.
*   Dithering is moving the scope slightly between frames, so that any small irregularities in the
*   light source are moved around, and thus averaged out, in the final frame.  We do this by taking
*   the first frame exactly on-target, then the next several at points around a circle a certain distance
*   out from the target, then the next several at points around a larger circle, and so on.
*
*   This object manages the circle math and counters for this process.
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
//        System.out.println(String.format("Creating DitherController at (%f,%f)", startAltDeg, startAzDeg));
        this.startAltDeg = startAltDeg;
        this.startAzDeg = startAzDeg;
        this.ditherRadiusArcSeconds = radiusArcSeconds;
        this.ditherMaxRadiusArcSeconds = maxRadiusArcSeconds;

        //  Convert ref values to radians for easier math
        this.ditherRadiusRadians = Math.toRadians(this.ditherRadiusArcSeconds / ARC_SECONDS_IN_DEGREE);
        this.ditherMaxRadiusRadians = Math.toRadians(this.ditherMaxRadiusArcSeconds / ARC_SECONDS_IN_DEGREE);

        //  Set the position variables to force a new cycle to start on first use
        this.reset();
    }

    /**
     * String description of the dither parameters for use in console log
     * @return (String)
     */
    public String description() {
        return String.format("radius %.2f arcseconds, maximum %.2f arcseconds", this.ditherRadiusArcSeconds,
                this.ditherMaxRadiusArcSeconds);
    }

    public double getStartAltDeg() {
        return startAltDeg;
    }

    public double getStartAzDeg() {
        return startAzDeg;
    }

    /**
     * Calculate what should happen for the next dithered frame.  Return whether the scope should be moved
     * and, if so, the Alt and Az coordinates to slew to
     * @return (triple)     Move scope?   Alt coordinates?   Az Coordinates?
     */
    public ImmutableTriple<Boolean, Double, Double> calculateNextFrame() {
        boolean slewScope;
        double targetAlt;
        double targetAz;
        this.countInSet += 1;
        if (this.countInSet == 1) {
            // First frame in set so we don't move the scope
            slewScope = false;
            targetAlt = this.startAltDeg;
            targetAz = this.startAzDeg;
        } else {
            //  We're beyond the first frame so we will do a dithering move
            ImmutablePair<Double,Double> ditherOffset = this.calcNextDitherOffset();
            double altOffset = ditherOffset.left;
            double azOffset = ditherOffset.right;
            //  Convert offset in radians to degrees, then offset original location
            slewScope = true;
            targetAlt = this.startAltDeg + Math.toDegrees(altOffset);
            targetAz = this.startAzDeg + Math.toDegrees(azOffset);
        }
        return ImmutableTriple.of(slewScope, targetAlt, targetAz);
    }

    /**
     * Calculate the next dithering offset, in radians, around the concentric circles that
     * build outward from the original point.
     * @return 2 doubles        x and y offsets to be added to Alt and Az (doesn't matter which)
     */
    private ImmutablePair<Double, Double> calcNextDitherOffset() {
        double azimuthOffset, altitudeOffset;
        // Calc next dither offset from (0,0) in radians
//        System.out.println(String.format("calc_next_dither_location old angle = %f", this.angleRadians));
        if (this.angleRadians > (2.0 * Math.PI)) {
            //  We have finished a circle, move to a bigger one.
//            System.out.println(String.format("  Angle %f > 2pi, resetting", this.angleRadians));
            this.angleRadians = 0.0;        //  Reset rotation
            this.steps *= 2;                //  Double steps on circle
//            System.out.println(String.format("  Steps increased to %d", this.steps));
            this.currentRadiusRadians += this.ditherRadiusRadians;       //  Increment radius of circle by dither space
//            System.out.println(String.format("  Radius increased to %f", this.currentRadiusRadians));
            if (this.currentRadiusRadians > this.ditherMaxRadiusRadians) {
                // We 've grown the circle larger than the specified maximum.  Reset to first
                this.steps = 8;  // First circle out from centre gets 8 divisions
                this.currentRadiusRadians = this.ditherRadiusRadians;
//                System.out.println(String.format("  Radius too large, reset to %f", this.currentRadiusRadians));
            }
        } else {
            //  We're still circumnavigating this circle
            this.angleRadians += ((2.0 * Math.PI) / this.steps);        //  Next step in same circle
//            System.out.println(String.format("  Angle in same circle rotated to %f", this.angleRadians));
        }

        //  Compute next dither location
        azimuthOffset = Math.cos(this.angleRadians) * this.currentRadiusRadians;
        altitudeOffset = Math.sin(this.angleRadians) * this.currentRadiusRadians;
//        System.out.println(String.format("Returning offsets ({%f},{%f})", xOffset, yOffset));

        return ImmutablePair.of(altitudeOffset, azimuthOffset);
    }

    /**
     * Reset the dithering parameters for a fresh run.
     */
    public void reset() {
        this.countInSet = 0;
        this.angleRadians = 3.0 * Math.PI;  // More than the 2-pi that is a complete circle
        this.steps = 4;                     // New cycle will double this to 8
        this.currentRadiusRadians = 0.0;    // Will be incremented to 1 radius
    }
}
