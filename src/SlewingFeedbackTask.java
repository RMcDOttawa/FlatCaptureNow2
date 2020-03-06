import java.util.TimerTask;

public class SlewingFeedbackTask extends TimerTask {
    private MainWindow parent;

    /**
     * Constructor for timer task to update the slewing message
     * @param parent        The main UI controller we're updating
     */
    public SlewingFeedbackTask(MainWindow parent) {
        super();
        this.parent = parent;
    }

    @Override
    public void run() {
        parent.fireSlewFeedbackTimer();
    }
}
