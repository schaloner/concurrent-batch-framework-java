package be.objectify.batch.concurrent.protocol.worker;

import akka.actor.ActorRef;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class WorkerCreated extends AbstractWorkerMessage
{

    public WorkerCreated(final ActorRef actorRef)
    {
        super(actorRef);
    }
}
