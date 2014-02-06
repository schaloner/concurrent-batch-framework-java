package be.objectify.batch.concurrent.protocol.listener;

import java.io.Serializable;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class JobFinished implements Serializable
{

    public static final JobFinished INSTANCE = new JobFinished();

    private JobFinished()
    {
        // internal static construction only
    }
}
