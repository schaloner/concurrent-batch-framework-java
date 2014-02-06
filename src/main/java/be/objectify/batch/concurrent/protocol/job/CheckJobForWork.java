package be.objectify.batch.concurrent.protocol.job;

import java.io.Serializable;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class CheckJobForWork implements Serializable
{

    private final long processed;

    public CheckJobForWork(final long processed)
    {
        this.processed = processed;
    }

    public long getProcessed()
    {
        return processed;
    }
}
