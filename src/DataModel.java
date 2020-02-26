import java.io.Serializable;

/**
 * Data Model for a flats capture plan.
 */
public class DataModel  implements Serializable {

    /**
     * Static constructor for data model.  Create a new data model with default values.
     */
    public static DataModel newInstance(AppPreferences preferences) {
        return new DataModel();
    }
}
