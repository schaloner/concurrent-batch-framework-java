package be.objectify.batch.concurrent.batch;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import akka.actor.ActorRef;
import akka.actor.ActorSystem;
import akka.testkit.JavaTestKit;
import org.junit.After;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import scala.Tuple2;
import scala.concurrent.duration.Duration;

import be.objectify.batch.concurrent.ActorSystemBean;
import be.objectify.batch.concurrent.BatchController;
import be.objectify.batch.concurrent.protocol.listener.JobStatus;
import be.objectify.batch.concurrent.protocol.listener.QueryJobStatus;

/**
 *
 * @author Steve Chaloner (steve@objectify.be)
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration("classpath:/test-batch-context.xml")
public class BatchControllerUnitTest implements ApplicationContextAware {

    private ApplicationContext context;

    @After
    public void tearDown() {
        final ActorSystemBean actorSystemBean = context.getBean(ActorSystemBean.class);
        final ActorSystem actorSystem = actorSystemBean.actorSystem();
        actorSystem.shutdown();
    }

    public static void main(String[] args) {
        ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext("classpath:/test-batch-context.xml");
        new BatchController(context, 500).execute();
    }

    @Test
    @DirtiesContext
    public void testBatchProcessing() {
        final ActorSystemBean actorSystemBean = context.getBean(ActorSystemBean.class);
        final ActorSystem actorSystem = actorSystemBean.actorSystem();
        new JavaTestKit(actorSystem) {{
            final JavaTestKit probe = new JavaTestKit(actorSystem);
            final BatchController batchController = new BatchController(context, 250);
            final ActorRef batchListener = actorSystem.actorFor(actorSystem.child("batchListener"));

            batchListener.tell(probe.getRef(), getRef());
            expectMsgEquals(duration("1 second"),
                            "custom message received");

            batchController.execute();

            // take into account the CheckForFinish ping occurs every 500 milliseconds, and we have 1000 items to process
            new Within(duration("6 seconds")) {
                protected void run() {

                    actorSystem.scheduler().scheduleOnce(Duration.create(5, TimeUnit.SECONDS),
                                                         batchListener,
                                                         QueryJobStatus.INSTANCE,
                                                         actorSystem.dispatcher(),
                                                         getRef());

                    final JobStatus jobStatus = new ExpectMsg<JobStatus>("check processing and error count") {
                        protected JobStatus match(Object in) {
                            if (in instanceof JobStatus) {
                                return (JobStatus)in;
                            } else {
                                throw noMatch();
                            }
                        }
                    }.get();

                    Assert.assertEquals(1000, jobStatus.getProcessed());
                    Assert.assertEquals(100, jobStatus.getErrors());

                    probe.expectMsgClass(Duration.Zero(), QueryJobStatus.class);
                    Assert.assertEquals(getRef(), probe.getLastSender());

                    batchListener.tell(GetReceivedWork.INSTANCE,
                                       getRef());
                    final List<String> receivedWork = new ExpectMsg<List<String>>("check received work") {
                        protected List<String> match(Object in) {
                            if (in instanceof List) {
                                return (List<String>)in;
                            } else {
                                throw noMatch();
                            }
                        }
                    }.get();
                    Assert.assertEquals(1000,
                                        receivedWork.size());

                    List<String> sorted = new ArrayList(receivedWork);
                    Collections.sort(sorted,
                                     new Comparator<String>() {
                                         @Override
                                         public int compare(String o1,
                                                            String o2) {
                                             int num1 = Integer.parseInt(o1.substring(o1.indexOf(" ") + 1));
                                             int num2 = Integer.parseInt(o2.substring(o2.indexOf(" ") + 1));
                                             return num1 < num2 ? -1 : 1;
                                         }
                                     });
                    for (int i = 0; i < sorted.size(); i++) {
                        Object o = sorted.get(i);
                        Assert.assertEquals("foo " + i,
                                            o);
                    }

                    expectNoMsg();
                }
            };
        }};
    }

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.context = applicationContext;
    }
}
