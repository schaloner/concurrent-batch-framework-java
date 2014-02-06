package be.objectify.batch.concurrent.batch;

import java.util.concurrent.Callable;
import akka.actor.ActorPath;
import akka.actor.ActorRef;
import akka.dispatch.Futures;
import scala.concurrent.Future;
import akka.pattern.Patterns;

import be.objectify.batch.concurrent.Worker;
import be.objectify.batch.concurrent.protocol.worker.WorkComplete;
import be.objectify.batch.concurrent.protocol.listener.WorkError;
import be.objectify.batch.concurrent.protocol.listener.WorkSuccess;
import be.objectify.batch.concurrent.protocol.master.WorkToBeDone;

public class BatchTestWorker extends Worker {

    public BatchTestWorker(ActorPath masterPath) {
        super(masterPath);
    }

    @Override
    public Future<Object> doWork(final ActorRef sender,
                                 final Object message) {
        return Futures.future(new Callable<Object>() {
            public Object call() {
                WorkToBeDone workToBeDone = (WorkToBeDone)message;
                final String work = workToBeDone.getWork().toString();
                if (work.endsWith("4")) {
                    sender.tell(new WorkError(work,
                                              "something bad happened"),
                                self());
                } else {
                    sender.tell(new WorkSuccess(work,
                                                "ok"),
                                self());
                }
                return new WorkComplete("done");
            }
        }, context().system().dispatcher());
    }
}
