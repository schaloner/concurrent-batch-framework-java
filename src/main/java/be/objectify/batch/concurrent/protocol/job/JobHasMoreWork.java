package be.objectify.batch.concurrent.protocol.job;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class JobHasMoreWork extends WorkStatus
{

    public static final JobHasMoreWork INSTANCE = new JobHasMoreWork();

    private JobHasMoreWork()
    {
        // internal static construction only
    }
}
