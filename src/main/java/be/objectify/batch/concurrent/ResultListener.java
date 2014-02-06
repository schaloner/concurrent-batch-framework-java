package be.objectify.batch.concurrent;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.UntypedActor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import be.objectify.batch.concurrent.protocol.job.CheckJobForWork;
import be.objectify.batch.concurrent.protocol.job.JobHasMoreWork;
import be.objectify.batch.concurrent.protocol.job.LoadWork;
import be.objectify.batch.concurrent.protocol.listener.JobFinished;
import be.objectify.batch.concurrent.protocol.listener.JobStatus;
import be.objectify.batch.concurrent.protocol.listener.QueryJobStatus;
import be.objectify.batch.concurrent.protocol.listener.WorkError;
import be.objectify.batch.concurrent.protocol.listener.WorkQueueEmpty;
import be.objectify.batch.concurrent.protocol.listener.WorkSuccess;

public abstract class ResultListener extends UntypedActor {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResultListener.class);

    private int processed = 0;
    private int errors = 0;

    @Override
    public final void onReceive(final Object message) throws Exception {
        final ActorSystem system = context().system();
        if (message instanceof WorkSuccess) {
            final WorkSuccess workSuccess = (WorkSuccess)message;
            onSuccess(workSuccess.getWork(),
                      workSuccess.getMessage());
        } else if (message instanceof WorkError) {
            final WorkError workError = (WorkError)message;
            onError(workError.getWork(),
                    workError.getMessage());
        } else if (message instanceof WorkQueueEmpty) {
            onQueueEmpty(system.actorFor(system.child("jobActor")));
        } else if (message instanceof JobHasMoreWork) {
            onLoadWork(system.actorFor(system.child("jobActor")));
        } else if (message instanceof JobFinished) {
            onJobFinished();
        } else if (message instanceof QueryJobStatus) {
            onQueryJobStatus(message);
        } else {
            onCustomMessage(message);
        }
    }

    public void onQueryJobStatus(final Object message) {
        final JobStatus jobStatus = new JobStatus(getProcessed(),
                                                  getErrors());
        sender().tell(jobStatus,
                      self());
    }

    public void onLoadWork(ActorRef jobActor) {
        jobActor.tell(new LoadWork(processed),
                      self());
    }

    public void onQueueEmpty(ActorRef jobActor) {
        final int processed = getProcessed();
        LOGGER.info("Current state: Processed: [{}]   Errors: [{}]",
                    processed,
                    getErrors());
        jobActor.tell(new CheckJobForWork(processed),
                      self());
    }

    public abstract void onCustomMessage(final Object message);

    public abstract void onError(final Object work,
                                 final String message);

    public abstract void onSuccess(final Object work,
                                   final String message);

    public abstract void onJobFinished();

    public int getProcessed() {
        return processed;
    }

    public int getErrors() {
        return errors;
    }

    public int incrementProcessed() {
        return ++processed;
    }

    public int incrementErrors() {
        return ++errors;
    }
}
