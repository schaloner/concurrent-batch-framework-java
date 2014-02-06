package be.objectify.batch.concurrent.protocol.worker;

import akka.actor.ActorRef;

import java.io.Serializable;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public abstract class AbstractWorkerMessage implements Serializable
{

    private final ActorRef actorRef;

    public AbstractWorkerMessage(final ActorRef actorRef)
    {
        this.actorRef = actorRef;
    }

    public ActorRef getActorRef()
    {
        return actorRef;
    }
}
