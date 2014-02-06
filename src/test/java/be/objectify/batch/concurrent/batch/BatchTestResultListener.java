package be.objectify.batch.concurrent.batch;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import akka.actor.ActorRef;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.objectify.batch.concurrent.ResultListener;

public class BatchTestResultListener extends ResultListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(BatchTestResultListener.class);

    private ActorRef testRef;

    private final List<Object> receivedWork = new LinkedList<Object>();

    @Override
    public void onQueryJobStatus(Object message) {
        super.onQueryJobStatus(message);
        if (testRef != null) {
            testRef.forward(message,
                            context());
        }
    }

    @Override
    public void onCustomMessage(Object message) {
        LOGGER.debug("Custom message: " + message);
        if (message instanceof ActorRef) {
            testRef = (ActorRef)message;
            sender().tell("custom message received",
                          self());
        } else if (message instanceof GetReceivedWork) {
            sender().tell(Collections.unmodifiableList(receivedWork),
                          self());
        } else {
            unhandled(message);
        }
    }

    @Override
    public void onError(Object work,
                        String message) {
        incrementProcessed();
        incrementErrors();
        receivedWork.add(work);
        System.out.println("Error: " + work + " : " + message);
    }

    @Override
    public void onSuccess(Object work,
                          String message) {
        incrementProcessed();
        receivedWork.add(work);
        System.out.println("Success: " + work + " : " + message);
    }

    @Override
    public void onJobFinished() {
        LOGGER.info("Processing finished: Processed: [{}]   Errors: [{}]",
                    getProcessed(),
                    getErrors());
    }
}
