import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.LocalDateTime;

public class ProviderApp {
    static int clientCount = 0;

    public static void main(String[] args) throws IOException {
        new ProviderApp().begin();
    }

    private void begin() throws IOException {
        int maxPendingConn = 10;
        final int port = 4444;
        ServerSocket servsock = new ServerSocket(port, maxPendingConn);
        System.out.println("The server is running!");
      
        while (true) {
            Socket sock = servsock.accept();
            clientCount++;

            new ServerThread(sock, clientCount).start();
        }
    } 
}


class ServerThread extends Thread {
    protected Socket sock;
    protected int clientNumber;
    private Response response = new ResponseImpl();

    public ServerThread(Socket clientSocket, int clientNumber) {
        this.sock = clientSocket;
        this.clientNumber = clientNumber;
    }

    public void run() {
        // Get I/O streams from the socket
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(sock.getOutputStream());
        } catch (Exception e) {
            System.out.println("error!");
        }

        boolean hasValue = true;

        try {
            out.writeObject(response.welcomeMessage(clientNumber));
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

        while(hasValue) {
            String request = null;
            try {
                ObjectInputStream isr = new ObjectInputStream(sock.getInputStream());
                Object object = isr.readObject();

                if (object instanceof  StringRpcRequest) {
                    StringRpcRequest stringRpcRequest = (StringRpcRequest) object;

                    String tmpString = stringRpcRequest.getString();

                    if ("request".equals(stringRpcRequest.getMethod())) {
                        if (tmpString.toLowerCase().equals("time")) {
                            request = response.timeString();
                        } else if (tmpString.isEmpty()) {
                            hasValue = false;
                            sock.close();
                            System.out.println("Socket closed!");
                        } else {
                            request = response.capitalizeString(tmpString);
                        }

                        if (hasValue) {
                            out = new ObjectOutputStream(sock.getOutputStream());
                            out.writeObject(request);
                            out.flush();
                        }
                    }

                } else {
                    System.out.println("error!");
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("error!");
            }
        }
    }
}
