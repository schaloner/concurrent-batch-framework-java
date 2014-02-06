package be.objectify.batch.concurrent.batch;

import java.io.Serializable;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class GetReceivedWork implements Serializable
{

    public static final GetReceivedWork INSTANCE = new GetReceivedWork();

    private GetReceivedWork()
    {
        // no-op
    }
}
