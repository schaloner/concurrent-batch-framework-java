package be.objectify.batch.concurrent.protocol.listener;

import java.io.Serializable;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class WorkError implements Serializable
{

    private final Object work;
    private final String message;

    public WorkError(final Object work,
                     final String message)
    {
        this.work = work;
        this.message = message;
    }

    public Object getWork()
    {
        return work;
    }

    public String getMessage()
    {
        return message;
    }
}
