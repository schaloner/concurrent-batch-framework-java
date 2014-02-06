package be.objectify.batch.concurrent.protocol;

import java.io.Serializable;

/**
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public class CheckForFinish implements Serializable {
    public static final CheckForFinish INSTANCE = new CheckForFinish();

    private CheckForFinish() {
        // internal static construction only
    }
}
