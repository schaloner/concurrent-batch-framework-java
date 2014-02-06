package be.objectify.batch.concurrent.protocol.worker;

import akka.actor.ActorRef;

import be.objectify.batch.concurrent.protocol.worker.AbstractWorkerMessage;

/**
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public class WorkerCreated extends AbstractWorkerMessage {

    public WorkerCreated(final ActorRef actorRef) {
        super(actorRef);
    }
}
