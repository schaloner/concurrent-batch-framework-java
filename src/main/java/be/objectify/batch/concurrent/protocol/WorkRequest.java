package be.objectify.batch.concurrent.protocol;

import java.io.Serializable;
import akka.actor.ActorRef;

/**
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public class WorkRequest implements Serializable {

    private final Object work;

    public WorkRequest(final Object work) {
        this.work = work;
    }

    public Object getWork() {
        return work;
    }
}
