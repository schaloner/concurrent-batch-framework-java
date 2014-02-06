package be.objectify.batch.concurrent;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.Callable;
import akka.actor.ActorSystem;
import akka.dispatch.Futures;
import scala.concurrent.ExecutionContext;
import scala.concurrent.Future;

import be.objectify.batch.concurrent.protocol.job.JobHasMoreWork;
import be.objectify.batch.concurrent.protocol.job.NoRemainingWork;
import be.objectify.batch.concurrent.protocol.job.WorkStatus;

public class TestableJobActor extends AbstractJobActor {

    private final int total = 100;

    @Override
    public Future<WorkStatus> hasMoreWork(final long alreadyProcessed) {
        final ActorSystem system = context().system();
        final ExecutionContext dispatcher = system.dispatcher();
        return Futures.future(new Callable<WorkStatus>() {
            @Override
            public WorkStatus call() throws Exception {
                return alreadyProcessed < total ? JobHasMoreWork.INSTANCE : NoRemainingWork.INSTANCE;
            }
        }, dispatcher);
    }

    @Override
    public Future<List> getWork(final long alreadyProcessed) {
        final ActorSystem system = context().system();
        final ExecutionContext dispatcher = system.dispatcher();
        return Futures.future(new Callable<List>() {
            @Override
            public List call() throws Exception {
                List<String> work = new LinkedList<String>();
                for (long i = alreadyProcessed; i < alreadyProcessed + 10; i++) {
                    work.add("foo " + i);
                }
                return work;
            }
        }, dispatcher);
    }
}
