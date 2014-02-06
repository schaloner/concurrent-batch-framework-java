package be.objectify.batch.concurrent.protocol.worker;

import akka.actor.ActorRef;

import be.objectify.batch.concurrent.protocol.worker.AbstractWorkerMessage;

public class WorkerCreated extends AbstractWorkerMessage {

    public WorkerCreated(final ActorRef actorRef) {
        super(actorRef);
    }
}
