import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Scanner;

public class ProviderApp {


    public static void main(String[] args) {
        new ProviderApp().begin();
    }

    private void begin() {
        System.out.println("The server is running!");
        new ServerWait().start();

        while (true) {
            System.out.println("Ask For Clients:");
            Scanner in = new Scanner(System.in);
            String s = in.nextLine();
            System.out.println("You said: " + s);
        }
    } 
}

class ServerWait extends Thread {
    static int clientCount = 0;
    public ServerWait() {

    }

    public void run() {
        int maxPendingConn = 10;
        final int port = 4444;
        ServerSocket servsock = null;
        ClientList clientList = new ClientList();
        try {
            servsock = new ServerSocket(port, maxPendingConn);
        } catch (IOException e) {
            e.printStackTrace();
        }

        while (true) {
            Socket sock = null;
            try {
                sock = servsock.accept();
            } catch (IOException e) {
                e.printStackTrace();
            }

            clientList.addClient(sock.getInetAddress().getHostAddress());
            clientCount++;

            new ServerThread(sock, clientCount, clientList).start();
            System.out.println(clientList.getClients().get(0));
        }
    }
}


class ServerThread extends Thread {
    protected Socket sock;
    protected int clientNumber;
    private Response response = new ResponseImpl();
    ClientList clientList;

    public ServerThread(Socket clientSocket, int clientNumber, ClientList clientList) {
        this.sock = clientSocket;
        this.clientNumber = clientNumber;
        this.clientList = clientList;
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
                            System.out.println(clientList.getClients().isEmpty());
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
