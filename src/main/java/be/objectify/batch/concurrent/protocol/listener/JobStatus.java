package be.objectify.batch.concurrent.protocol.listener;

import java.io.Serializable;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class JobStatus implements Serializable
{

    private final int processed;
    private final int errors;

    public JobStatus(int processed,
                     int errors)
    {
        this.processed = processed;
        this.errors = errors;
    }

    public int getProcessed()
    {
        return processed;
    }

    public int getErrors()
    {
        return errors;
    }
}
