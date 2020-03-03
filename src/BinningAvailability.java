import java.io.Serializable;

/**
 * enum to record the possible ways a given binning value may be used in the application
 */
public enum BinningAvailability implements Serializable {
    OFF,            // Value not available at all
    AVAILABLE,      // Value available, but no frames selected by default
    DEFAULT;        // Value available and has a default number of frames selected

    public int codeNumber() {
        int result = -1;
        switch (this) {
            case OFF:
                result = 0;
                break;
            case AVAILABLE:
                result = 1;
                break;
            case DEFAULT:
                result = 2;
                break;
        }
        return result;
    }
}
