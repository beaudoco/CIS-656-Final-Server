import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ThreadLocalRandom;

public class ProviderApp {


    public static void main(String[] args) {
        new ProviderApp().begin();
    }

    private void begin() {
        ClientList clientList = new ClientList();
        List<Socket> sockList = new ArrayList<>();
        System.out.println("The server is running!");
        StringRpcRequest stringRpcRequest;
        ObjectOutputStream out;
        new ServerWait(clientList, sockList).start();

        while (true) {
            System.out.println("Ask For Clients:");
            Scanner in = new Scanner(System.in);
            String s = in.nextLine();
            System.out.println("You said: " + s);
            for (int i = 0; i < clientList.getClients().size(); i++) {
                System.out.println(clientList.getClients().get(i));
            }

            if(s.isEmpty()) {
                for (int i = 0; i < sockList.size(); i++) {
                    try {
                        out = new ObjectOutputStream(sockList.get(i).getOutputStream());
                        stringRpcRequest = generateServerRequest(s);
                        out.writeObject(stringRpcRequest);
                        out.flush();
                        sockList.get(i).close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //sockList.remove(sockList.get(i));
                }
            }
        }
    }
    // CONTROLS COMMUNICATION BETWEEN MACHINES
    private StringRpcRequest generateServerRequest(String val) {
        StringRpcRequest stringRpcRequest = new StringRpcRequest();
        stringRpcRequest.setString(val);
        stringRpcRequest.setMethod("request");
        return stringRpcRequest;
    }
}

class ServerWait extends Thread {
    static int clientCount = 0;
    ClientList clientList;
    List<Socket> sockList;
    public ServerWait(ClientList clientList, List<Socket> sockList) {
        this.clientList = clientList;
        this.sockList = sockList;
    }

    public void run() {
        int maxPendingConn = 10;
        final int port = 4444;
        ServerSocket servsock = null;

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


            clientList.addClient(sock.getRemoteSocketAddress().toString());
            clientCount++;

            new ServerThread(sock, clientCount, clientList, sock.getRemoteSocketAddress().toString()).start();
            sockList.add(sock);

            System.out.println("List size: " + clientList.getClients().size());
        }
    }
}


class ServerThread extends Thread {
    protected Socket sock;
    protected int clientNumber;
    private Response response = new ResponseImpl();
    ClientList clientList;
    String clientName;

    public ServerThread(Socket clientSocket, int clientNumber, ClientList clientList, String clientName) {
        this.sock = clientSocket;
        this.clientNumber = clientNumber;
        this.clientList = clientList;
        this.clientName = clientName;
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
            if (clientList.getClients().size() == 1) {
                out.writeObject(response.welcomeMessage());
                out.flush();
            } else {
                List<String> tmpClientList = new ArrayList<>();
                tmpClientList.addAll(clientList.getClients());
                tmpClientList.remove(clientName);
                int randomNum = ThreadLocalRandom.current().nextInt(0, tmpClientList.size());
                out.writeObject(tmpClientList.get(randomNum));
            }
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
                        if (tmpString.isEmpty()) {
                            clientList.removeClient(clientName);
                            hasValue = false;
                            sock.close();
                            System.out.println("Socket closed!");
                        }
                        request = tmpString;

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
