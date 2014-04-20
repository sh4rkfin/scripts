package dave;


import java.nio.channels.SocketChannel;
import java.util.LinkedList;
import java.util.List;

/**
    User: dfinlay
    Date: 9/25/12
*/
public class EchoWorker implements Runnable
{

    static class ServerDataEvent
    {
        public Server server;
        public SocketChannel socket;
        public byte[] data;

        public ServerDataEvent (Server server, SocketChannel socket, byte[] data)
        {
            this.server = server;
            this.socket = socket;
            this.data = data;
        }
    }

    private List queue = new LinkedList();

    public void processData (Server server, SocketChannel socket, byte[] data, int count)
    {
        byte[] dataCopy = new byte[count];
        System.arraycopy(data, 0, dataCopy, 0, count);
        synchronized(queue) {
            queue.add(new ServerDataEvent(server, socket, dataCopy));
            queue.notify();
        }
    }

    public void run()
    {
        ServerDataEvent dataEvent;

        while(true) {
            // Wait for data to become available
            synchronized(queue) {
                while(queue.isEmpty()) {
                    try {
                        queue.wait();
                    } catch (InterruptedException e) {
                    }
                }
                dataEvent = (ServerDataEvent) queue.remove(0);
            }

            // Return to sender
            dataEvent.server.send(dataEvent.socket, dataEvent.data);
        }
    }
}
