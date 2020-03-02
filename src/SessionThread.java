import java.util.ArrayList;

public class SessionThread implements Runnable {

    private final Session parent;
    private final DataModel dataModel;
    private final ArrayList<FlatSet> flatsToAcquire;

    public SessionThread(Session parent, DataModel dataModel, ArrayList<FlatSet> flatsToAcquire) {
        this.parent = parent;
        this.dataModel = dataModel;
        this.flatsToAcquire = flatsToAcquire;
    }

    @Override
    public void run() {
        System.out.println("SessionThread/run entered");
        // todo SessionThread/run
        try {
            Thread.sleep(10*1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.out.println("SessionThread/run exits");
        this.parent.acquisitionThreadEnded();
    }
}
