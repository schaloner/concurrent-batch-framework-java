package be.objectify.batch.concurrent.protocol.worker;

import akka.actor.ActorRef;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class WorkIsDone extends AbstractWorkerMessage
{

    public WorkIsDone(final ActorRef actorRef)
    {
        super(actorRef);
    }
}
