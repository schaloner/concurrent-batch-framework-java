package be.objectify.batch.concurrent.protocol.worker;

import akka.actor.ActorRef;

import be.objectify.batch.concurrent.protocol.worker.AbstractWorkerMessage;

public class WorkerRequestsWork extends AbstractWorkerMessage {

    public WorkerRequestsWork(final ActorRef actorRef) {
        super(actorRef);
    }
}
