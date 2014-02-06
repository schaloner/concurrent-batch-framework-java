package be.objectify.batch.concurrent;

import akka.actor.ActorPath;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class ActorPathFactory
{

    private final ActorSystemBean actorSystemBean;

    public ActorPathFactory(final ActorSystemBean actorSystemBean)
    {
        this.actorSystemBean = actorSystemBean;
    }

    public ActorPath master()
    {
        return actorSystemBean.actorSystem()
                              .child("batchMasterActor");
    }
}
