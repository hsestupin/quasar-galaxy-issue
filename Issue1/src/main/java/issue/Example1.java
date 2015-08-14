package issue;

import co.paralleluniverse.actors.ActorRef;
import co.paralleluniverse.actors.ActorRegistry;
import co.paralleluniverse.actors.behaviors.Behavior;
import co.paralleluniverse.actors.behaviors.BehaviorActor;
import co.paralleluniverse.common.monitoring.FlightRecorder;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.galaxy.Grid;
import org.apache.curator.test.TestingServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

/**
 * @author s.stupin
 */
public class Example1 {

  private static final Logger log = LoggerFactory.getLogger(Example1.class);
  public static final String ACTOR_NAME = "my-actor";

  public static void main(String[] args) throws Exception {
    try (TestingServer ignored = new TestingServer(2181)) {
      final Properties props = new Properties();
      props.setProperty("galaxy.nodeId", "1");
      props.setProperty("galaxy.port", "7051");
      props.setProperty("galaxy.slave_port", "8051");
      props.setProperty("galaxy.multicast.address", "225.0.0.1");
      props.setProperty("galaxy.multicast.port", "7050");
      props.setProperty("galaxy.zkServers", "127.0.0.1:2181");

      final Grid grid = Grid.getInstance(null, props);
      grid.goOnline();

      final MyActor actor = new MyActor();
      final Behavior localRef = actor.spawnThread();
      actor.register(ACTOR_NAME);

      final ActorRef<Object> globalRef = ActorRegistry.getActor(ACTOR_NAME);
      globalRef.send(localRef);

      actor.printRecords();

      localRef.shutdown();
      grid.cluster().goOffline();
    }
  }

  public static class MyActor extends BehaviorActor {

    public void printRecords() {
      for (FlightRecorder.Record record : flightRecorder.getRecords()) {
        System.out.println(record);
      }
    }

    @Override
    protected void handleMessage(Object message) throws InterruptedException, SuspendExecution {
      System.out.println("Message received '" + message + '\'');
    }

    @Override
    public Logger log() {
      return LoggerFactory.getLogger(MyActor.class);
    }
  }

}
