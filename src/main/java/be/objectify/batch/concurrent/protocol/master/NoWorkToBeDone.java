package be.objectify.batch.concurrent.protocol.master;

import java.io.Serializable;

public class NoWorkToBeDone implements Serializable {

    public static final NoWorkToBeDone INSTANCE = new NoWorkToBeDone();

    private NoWorkToBeDone() {
        // internal static construction only
    }
}
