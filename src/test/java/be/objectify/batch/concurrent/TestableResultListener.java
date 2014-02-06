package be.objectify.batch.concurrent;

import akka.actor.ActorRef;

/**
 * @author Steve Chaloner (steve@objectify.be)
 */
public class TestableResultListener extends ResultListener
{

    @Override
    public void onQueryJobStatus(Object message)
    {
        sender().tell("onQueryJobStatus",
                      self());
    }

    @Override
    public void onCustomMessage(Object message)
    {
        sender().tell("onCustomMessage",
                      self());
    }

    @Override
    public void onError(Object work,
                        String message)
    {
        sender().tell("onError",
                      self());
    }

    @Override
    public void onSuccess(Object work,
                          String message)
    {
        sender().tell("onSuccess",
                      self());
    }

    @Override
    public void onQueueEmpty(ActorRef jobActor)
    {
        sender().tell("onQueueEmpty",
                      self());
    }

    @Override
    public void onJobFinished()
    {
        sender().tell("onJobFinished",
                      self());
    }
}
