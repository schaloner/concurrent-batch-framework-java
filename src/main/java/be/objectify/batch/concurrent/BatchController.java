package be.objectify.batch.concurrent;

import java.util.concurrent.TimeUnit;
import akka.actor.Actor;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.actor.Props;
import akka.japi.Creator;
import akka.routing.SmallestMailboxRouter;
import org.springframework.context.ApplicationContext;
import scala.concurrent.duration.Duration;

import be.objectify.batch.concurrent.protocol.CheckForFinish;

/**
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
public class BatchController {

    private final ActorRef master;

    private final ActorSystem actorSystem;

    private final int checkForWorkFrequencyInMilliseconds;

    public BatchController(final ApplicationContext context,
                           final int checkForWorkFrequencyInMilliseconds) {
        this.checkForWorkFrequencyInMilliseconds = checkForWorkFrequencyInMilliseconds;

        final ActorSystemBean actorSystemBean = context.getBean(ActorSystemBean.class);
        actorSystem = actorSystemBean.actorSystem();

        actorSystem.actorOf(Props.create(new ActorCreator(context,
                                                          "batchListener")),
                            "batchListener");
        ActorRef listener = actorSystem.actorFor(actorSystem.child("batchListener"));

        actorSystem.actorOf(Props.create(new ActorCreator(context,
                                                          "jobActor")),
                            "jobActor");

        actorSystem.actorOf(Props.create(new MasterCreator(listener)),
                            "batchMasterActor");
        master = actorSystem.actorFor(actorSystem.child("batchMasterActor"));

        final String[] workerNames = context.getBeanNamesForType(Worker.class);
        for (final String key : workerNames) {
            actorSystem.actorOf(Props.create(new ActorCreator(context,
                                                               key)).withRouter(new SmallestMailboxRouter(10)),
                                key);
        }
    }

    public void execute() {
        actorSystem.scheduler().schedule(Duration.create(1, TimeUnit.SECONDS),
                                         Duration.create(checkForWorkFrequencyInMilliseconds, TimeUnit.MILLISECONDS),
                                         master,
                                         CheckForFinish.INSTANCE,
                                         actorSystem.dispatcher(),
                                         ActorRef.noSender());
    }

    private static final class MasterCreator implements Creator<Master> {

        private final ActorRef resultListener;

        private MasterCreator(final ActorRef resultListener) {
            this.resultListener = resultListener;
        }

        @Override
        public Master create() throws Exception {
            return new Master(resultListener);
        }
    }

    private static final class ActorCreator implements Creator<Actor> {

        private final ApplicationContext context;
        private final String name;

        private ActorCreator(final ApplicationContext context,
                             final String name) {
            this.context = context;
            this.name = name;
        }

        @Override
        public Actor create() throws Exception {
            return context.getBean(name,
                                   Actor.class);
        }
    }
}
