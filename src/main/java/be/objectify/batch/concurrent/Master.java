package be.objectify.batch.concurrent;

import akka.actor.ActorRef;
import akka.actor.Terminated;
import akka.actor.UntypedActor;
import be.objectify.batch.concurrent.protocol.CheckForFinish;
import be.objectify.batch.concurrent.protocol.WorkRequest;
import be.objectify.batch.concurrent.protocol.listener.WorkQueueEmpty;
import be.objectify.batch.concurrent.protocol.master.NoWorkToBeDone;
import be.objectify.batch.concurrent.protocol.master.WorkIsReady;
import be.objectify.batch.concurrent.protocol.master.WorkToBeDone;
import be.objectify.batch.concurrent.protocol.worker.WorkIsDone;
import be.objectify.batch.concurrent.protocol.worker.WorkerCreated;
import be.objectify.batch.concurrent.protocol.worker.WorkerRequestsWork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import scala.Option;
import scala.Tuple2;

import java.util.Deque;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class Master extends UntypedActor
{

    private static final Logger LOGGER = LoggerFactory.getLogger(Master.class);

    private final Map<ActorRef, Option<Tuple2<ActorRef, Object>>> workers = new TreeMap<ActorRef, Option<Tuple2<ActorRef, Object>>>();

    private final Deque<Tuple2<ActorRef, Object>> workQueue = new LinkedList<Tuple2<ActorRef, Object>>();

    private final ActorRef resultListener;

    public Master(ActorRef resultListener)
    {
        this.resultListener = resultListener;
    }

    private void notifyWorkers()
    {
        if (!workQueue.isEmpty())
        {
            for (Map.Entry<ActorRef, Option<Tuple2<ActorRef, Object>>> entry : workers.entrySet())
            {
                if (entry.getValue()
                         .isEmpty())
                {
                    entry.getKey()
                         .tell(WorkIsReady.INSTANCE,
                               self());
                }
            }
        }
    }

    @Override
    public void onReceive(Object message) throws Exception
    {
        if (message instanceof WorkerCreated)
        {
            final WorkerCreated workerCreated = (WorkerCreated) message;
            final ActorRef worker = workerCreated.getActorRef();
            context().watch(worker);
            workers.put(worker,
                        Option.<Tuple2<ActorRef, Object>>empty());
            notifyWorkers();
        } else if (message instanceof WorkerRequestsWork)
        {
            final WorkerRequestsWork workerRequestsWork = (WorkerRequestsWork) message;
            final ActorRef worker = workerRequestsWork.getActorRef();
            if (workers.containsKey(worker))
            {
                if (workQueue.isEmpty())
                {
                    final ActorRef self = self();
                    worker.tell(NoWorkToBeDone.INSTANCE,
                                self);
                } else
                {
                    final Option<Tuple2<ActorRef, Object>> currentWork = workers.get(worker);
                    if (currentWork.isEmpty())
                    {
                        final Tuple2<ActorRef, Object> queuedWork = workQueue.pop();
                        workers.put(worker,
                                    Option.apply(queuedWork));
                        worker.tell(new WorkToBeDone(queuedWork._2()),
                                    queuedWork._1());
                    }
                }
            }
        } else if (message instanceof WorkIsDone)
        {
            final WorkIsDone workIsDone = (WorkIsDone) message;
            final ActorRef worker = workIsDone.getActorRef();
            if (!workers.containsKey(worker))
            {
                LOGGER.error("[{}] said it's done work but we didn't know about it",
                             worker);
            } else
            {
                workers.put(worker,
                            Option.<Tuple2<ActorRef, Object>>empty());
            }
        } else if (message instanceof Terminated)
        {
            final Terminated terminated = (Terminated) message;
            final ActorRef worker = terminated.actor();
            final Option<Tuple2<ActorRef, Object>> currentWorkOption = workers.get(worker);
            if (currentWorkOption != null && currentWorkOption.isDefined())
            {
                final Tuple2<ActorRef, Object> currentWork = currentWorkOption.get();
                final Object work = currentWork._2();
                LOGGER.error("[{}] died while processing [{}]",
                             worker,
                             work);
                self().tell(work,
                            currentWork._1());
            }
            workers.remove(worker);
        } else if (message instanceof WorkRequest)
        {
            final WorkRequest workRequest = (WorkRequest) message;
            LOGGER.info("Enqueuing [{}]",
                        message);
            workQueue.add(new Tuple2<ActorRef, Object>(sender(),
                                                       workRequest.getWork()));
            notifyWorkers();
        } else if (message instanceof List)
        {
            final List list = (List) message;
            LOGGER.info("Enqueuing [{}] items",
                        list.size());
            for (Object object : list)
            {
                workQueue.add(new Tuple2<ActorRef, Object>(sender(),
                                                           object));
            }
            notifyWorkers();
        } else if (message instanceof CheckForFinish)
        {
            boolean empty = workQueue.isEmpty();
            synchronized (workers)
            {
                for (Iterator<Option<Tuple2<ActorRef, Object>>> iterator = workers.values()
                                                                                  .iterator(); empty && iterator.hasNext(); )
                {
                    empty = iterator.next()
                                    .isEmpty();
                }
            }
            if (empty)
            {
                LOGGER.info("Queue empty and no engaged workers, informing listener");
                resultListener.tell(WorkQueueEmpty.INSTANCE,
                                    self());
            }
        } else
        {
            unhandled(message);
        }
    }
}
