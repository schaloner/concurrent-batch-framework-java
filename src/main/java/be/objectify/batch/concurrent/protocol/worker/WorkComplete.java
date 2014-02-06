package be.objectify.batch.concurrent.protocol.worker;

import java.io.Serializable;

public class WorkComplete implements Serializable {

    private final Object result;

    public WorkComplete(final Object result) {
        this.result = result;
    }

    public Object getResult() {
        return result;
    }
}
