package be.objectify.batch.concurrent.protocol.worker;

import java.io.Serializable;
import akka.actor.ActorRef;

public abstract class AbstractWorkerMessage implements Serializable {

    private final ActorRef actorRef;

    public AbstractWorkerMessage(final ActorRef actorRef) {
        this.actorRef = actorRef;
    }

    public ActorRef getActorRef() {
        return actorRef;
    }
}
