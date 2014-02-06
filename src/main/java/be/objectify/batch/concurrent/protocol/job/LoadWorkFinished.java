package be.objectify.batch.concurrent.protocol.job;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class LoadWorkFinished extends WorkStatus
{

    public static final LoadWorkFinished INSTANCE = new LoadWorkFinished();

    private LoadWorkFinished()
    {
        // internal static construction only
    }
}
