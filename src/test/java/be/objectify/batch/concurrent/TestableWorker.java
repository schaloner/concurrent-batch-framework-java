package be.objectify.batch.concurrent;

import akka.actor.ActorPath;
import akka.actor.ActorRef;
import akka.dispatch.Futures;
import scala.concurrent.Future;

import java.util.concurrent.Callable;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class TestableWorker extends Worker
{

    public TestableWorker(ActorPath masterPath)
    {
        super(masterPath);
    }

    @Override
    public Future<Object> doWork(final ActorRef sender,
                                 final Object message)
    {
        return Futures.future(new Callable<Object>()
        {
            public Object call()
            {
                return "doWork";
            }
        }, context().system()
                    .dispatcher());
    }
}
