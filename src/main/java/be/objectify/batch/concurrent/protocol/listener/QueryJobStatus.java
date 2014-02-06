package be.objectify.batch.concurrent.protocol.listener;

import java.io.Serializable;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class QueryJobStatus implements Serializable
{

    public static final QueryJobStatus INSTANCE = new QueryJobStatus();

    private QueryJobStatus()
    {
        // internal static construction only
    }
}
