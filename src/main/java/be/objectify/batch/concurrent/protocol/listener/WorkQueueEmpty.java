package be.objectify.batch.concurrent.protocol.listener;

import java.io.Serializable;

public class WorkQueueEmpty implements Serializable {

    public static final WorkQueueEmpty INSTANCE = new WorkQueueEmpty();

    private WorkQueueEmpty() {
        // internal static construction only
    }
}
