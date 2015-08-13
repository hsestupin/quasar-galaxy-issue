package issue;

import co.paralleluniverse.actors.ActorRegistry;
import co.paralleluniverse.actors.BasicActor;
import co.paralleluniverse.actors.behaviors.RequestMessage;
import co.paralleluniverse.actors.behaviors.RequestReplyHelper;
import co.paralleluniverse.fibers.SuspendExecution;
import co.paralleluniverse.galaxy.Grid;
import org.apache.curator.test.TestingServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ObjectStreamException;
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
      actor.spawnThread();
      actor.register(ACTOR_NAME);

      ActorRegistry.getActor(ACTOR_NAME);

      Throwable issue = null;
      try {
        actor.runFail();
      } catch (Throwable th) {
        issue = th;
      }
      if (issue == null) {
        throw new AssertionError();
      }

      log.error("FIRST ISSUE", issue);
    }
  }

  public static class MyActor extends BasicActor<HelloMessage, Void> {

    @Override
    protected Void doRun() throws InterruptedException, SuspendExecution {
      final HelloMessage message = receive();
      System.out.println("Message received '" + message + '\'');
      RequestReplyHelper.reply(message, 1);
      return null;
    }

    public void runFail() throws ObjectStreamException {
      writeReplace();
    }
  }

  public static class HelloMessage extends RequestMessage<Integer> {

    @Override
    public String toString() {
      return "Hello!";
    }
  }

}
