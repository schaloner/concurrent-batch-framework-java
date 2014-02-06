package be.objectify.batch.concurrent;

import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import com.typesafe.config.ConfigFactory;

public class ActorSystemBean {
    private final ActorSystem actorSystem;

    public ActorSystemBean(final String systemName)
    {
        this.actorSystem = ActorSystem.create(systemName,
                                              ConfigFactory.load());
    }

    public ActorSystem actorSystem()
    {
        return actorSystem;
    }
}
