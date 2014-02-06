package be.objectify.batch.concurrent;

import akka.actor.ActorPath;
import akka.actor.ActorRef;
import akka.actor.UntypedActor;
import akka.japi.Procedure;
import akka.pattern.Patterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.Future;

import be.objectify.batch.concurrent.protocol.master.NoWorkToBeDone;
import be.objectify.batch.concurrent.protocol.worker.WorkComplete;
import be.objectify.batch.concurrent.protocol.worker.WorkIsDone;
import be.objectify.batch.concurrent.protocol.master.WorkIsReady;
import be.objectify.batch.concurrent.protocol.master.WorkToBeDone;
import be.objectify.batch.concurrent.protocol.worker.WorkerCreated;
import be.objectify.batch.concurrent.protocol.worker.WorkerRequestsWork;

public abstract class Worker extends UntypedActor {

    private static final Logger LOGGER = LoggerFactory.getLogger(Worker.class);

    private final ActorRef master;

    private final Procedure<Object> active = new Active();

    private final Procedure<Object> idle = new Idle();

    protected Worker(final ActorPath masterPath) {
        master = context().actorFor(masterPath);
    }

    @Override
    public void preStart() throws Exception {
        final ActorRef self = self();
        master.tell(new WorkerCreated(self),
                    self);
        getContext().become(idle);
    }

    @Override
    public void onReceive(final Object message) throws Exception {
        // no-op - see Active and Idle for behaviour
    }

    public abstract Future<Object> doWork(final ActorRef sender,
                                          final Object message);

    private final class Active implements Procedure<Object> {
        @Override
        public void apply(final Object message) {
            if (message instanceof WorkIsReady) {
                LOGGER.info("Work is ready, but I'm already working.  Ignoring request");
            } else if (message instanceof NoWorkToBeDone) {
                LOGGER.info("No work to be done.  Ignoring request");
            } else if (message instanceof WorkToBeDone) {
                LOGGER.info("I've been given work, but I'm already busy.  This is not good.");
            } else if (message instanceof WorkComplete) {
                final WorkComplete workComplete = (WorkComplete)message;
                LOGGER.info("Finished work with result [{}]",
                            workComplete.getResult());
                final ActorRef self = self();
                master.tell(new WorkIsDone(self),
                            self);
                master.tell(new WorkerRequestsWork(self),
                            self);
                getContext().become(idle);
            }
        }
    }

    private final class Idle implements Procedure<Object> {
        @Override
        public void apply(final Object message) {
            if (message instanceof WorkIsReady) {
                LOGGER.info("Requesting work");
                master.tell(new WorkerRequestsWork(self()),
                            self());
            } else if (message instanceof WorkToBeDone) {
                final WorkToBeDone workToBeDone = (WorkToBeDone)message;
                LOGGER.info("Got work: [{}]",
                            workToBeDone.getWork());
                Future<Object> future = doWork(sender(),
                                               message);
                // route the result of the future back into self, which will be processed by Worker#onReceive
                Patterns.pipe(future,
                              context().system().dispatcher())
                        .to(self());
                getContext().become(active);
            } else if (message instanceof NoWorkToBeDone) {
                LOGGER.info("Requested work, but none available");
            }
        }
    }
}
