package be.objectify.batch.concurrent.batch;

import java.io.Serializable;

public class GetReceivedWork implements Serializable {

    public static final GetReceivedWork INSTANCE = new GetReceivedWork();

    private GetReceivedWork(){
        // no-op
    }
}
