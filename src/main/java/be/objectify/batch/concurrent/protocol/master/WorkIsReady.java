package be.objectify.batch.concurrent.protocol.master;

import java.io.Serializable;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class WorkIsReady implements Serializable
{

    public static final WorkIsReady INSTANCE = new WorkIsReady();

    private WorkIsReady()
    {
        // internal static construction only
    }
}
