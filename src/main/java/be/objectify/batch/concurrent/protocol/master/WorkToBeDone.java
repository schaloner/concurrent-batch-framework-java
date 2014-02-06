package be.objectify.batch.concurrent.protocol.master;

import java.io.Serializable;

public class WorkToBeDone implements Serializable {

    private final Object work;

    public WorkToBeDone(final Object work) {
        this.work = work;
    }

    public Object getWork() {
        return work;
    }
}
