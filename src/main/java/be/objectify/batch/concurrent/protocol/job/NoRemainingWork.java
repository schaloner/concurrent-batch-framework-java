package be.objectify.batch.concurrent.protocol.job;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class NoRemainingWork extends WorkStatus
{

    public static final NoRemainingWork INSTANCE = new NoRemainingWork();

    private NoRemainingWork()
    {
        // internal static construction only
    }
}
