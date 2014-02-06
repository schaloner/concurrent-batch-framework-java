package be.objectify.batch.concurrent;

import java.util.List;
import java.util.concurrent.Callable;
import akka.actor.ActorSystem;
import akka.actor.UntypedActor;
import akka.dispatch.Futures;
import akka.dispatch.OnComplete;
import akka.japi.Procedure;
import akka.pattern.Patterns;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;

import be.objectify.batch.concurrent.protocol.job.CheckJobForWork;
import be.objectify.batch.concurrent.protocol.job.JobHasMoreWork;
import be.objectify.batch.concurrent.protocol.job.LoadWork;
import be.objectify.batch.concurrent.protocol.job.LoadWorkFinished;
import be.objectify.batch.concurrent.protocol.job.NoRemainingWork;
import be.objectify.batch.concurrent.protocol.job.WorkStatus;
import be.objectify.batch.concurrent.protocol.listener.JobFinished;

public abstract class AbstractJobActor extends UntypedActor {

    private static final Logger LOGGER = LoggerFactory.getLogger(AbstractJobActor.class);

    private final Procedure<Object> active = new Active();

    private final Procedure<Object> idle = new Idle();

    @Override
    public void preStart() throws Exception {
        getContext().become(idle);
    }

    @Override
    public void onReceive(final Object message) throws Exception {
        // no-op - see Active and Idle for behaviour
    }

    /**
     * Override this if you're expecting interactions from non-framework senders
     * @param message the message
     */
    public void onCustomMessage(final Object message) {
        unhandled(message);
    }

    public abstract Future<WorkStatus> hasMoreWork(final long alreadyProcessed);

    public abstract Future<List> getWork(final long alreadyProcessed);

    private final class Active implements Procedure<Object> {
        @Override
        public void apply(final Object message) {
            final ActorSystem system = context().system();
            if (message instanceof LoadWorkFinished) {
                LOGGER.info("[Active] Work loaded, becoming idle");
                getContext().become(idle);
            } else if (message instanceof CheckJobForWork) {
                LOGGER.info("[Active] Loading work, ignoring message");
            } else if (message instanceof JobHasMoreWork) {
                LOGGER.info("[Active] More work is available");
                system.actorSelection(system.child("batchListener")).tell(message,
                                                                          self());
                LOGGER.info("[Active] Becoming idle");
                getContext().become(idle);
            } else if (message instanceof NoRemainingWork) {
                LOGGER.info("[Active] No remaining work");
                system.actorSelection(system.child("batchListener")).tell(JobFinished.INSTANCE,
                                                                          self());
                LOGGER.info("[Active] Becoming idle");
                getContext().become(idle);
            } else if (message instanceof LoadWork) {
                LOGGER.info("[Active] We're already loading work, learn patience");
            } else {
                onCustomMessage(message);
            }
        }
    }

    private final class Idle implements Procedure<Object> {
        @Override
        public void apply(final Object message) {
            final ActorSystem system = context().system();
            if (message instanceof CheckJobForWork) {
                LOGGER.info("[Idle] Checking for work");
                final CheckJobForWork checkJobForWork = (CheckJobForWork)message;
                final long processed = checkJobForWork.getProcessed();
                LOGGER.info("Checking job for more work");

                final Future<WorkStatus> future = hasMoreWork(processed);
                getContext().become(active);
                Patterns.pipe(future,
                              system.dispatcher())
                        .to(self(),
                            sender());
            } else if (message instanceof JobHasMoreWork) {
                LOGGER.info("[Idle] More work is available");
                system.actorSelection(system.child("batchListener")).tell(message,
                                                                          self());
            } else if (message instanceof NoRemainingWork) {
                LOGGER.info("[Idle] No remaining work");
                system.actorSelection(system.child("batchListener")).tell(JobFinished.INSTANCE,
                                                                          self());
            } else if (message instanceof LoadWork) {
                LOGGER.info("[Idle] More work available, dispatching into system");
                LoadWork loadWork = (LoadWork)message;
                final Future<List> future = getWork(loadWork.getProcessed());
                Patterns.pipe(future,
                              system.dispatcher())
                        .to(system.actorSelection(system.child("batchMasterActor")),
                            sender());
                final ExecutionContext dispatcher = system.dispatcher();
                final OnComplete<List> onComplete = new OnComplete<List>() {
                    @Override
                    public void onComplete(Throwable failure,
                                           List success) throws Throwable {
                        Patterns.pipe(Futures.future(new Callable<LoadWorkFinished>() {
                            @Override
                            public LoadWorkFinished call() throws Exception {
                                return LoadWorkFinished.INSTANCE;
                            }
                        }, dispatcher), dispatcher).to(self(),
                                                       sender());
                    }
                };
                future.onComplete(onComplete,
                                  dispatcher);
                LOGGER.info("[Idle] Becoming active");
                getContext().become(active);
            } else {
                onCustomMessage(message);
            }
        }
    }
}
