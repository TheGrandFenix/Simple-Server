package Server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class ServerCore implements Runnable {
    //Define socket and client list
    private ServerSocket serverSocket;
    private ArrayList<SocketHandler> clientHandlers = new ArrayList<>();

    ServerCore(int port) {
        //Start server socket on given port
        startServerSocket(port);
    }

    public void run() {
        //Listen for incoming client requests
        listenIncomingRequests();
    }

    private void startServerSocket(int port) {
        try {
            //Try to create a new server socket on given port
            serverSocket = new ServerSocket(port);
        } catch (IOException e) {
            //Catch and output exceptions when creating server socket
            System.err.println("Failed to start server socket...");
            e.printStackTrace();
            System.exit(0);
        }
    }

    private void listenIncomingRequests() {
        while (true) {
            try {
                //Try to handle incoming request
                addClientSocket(serverSocket.accept());
            } catch (IOException e) {
                //Catch and output errors when accepting client socket
                System.err.println("Failed to accept client socket...");
                e.printStackTrace();
                System.exit(0);
            }
        }
    }

    private void addClientSocket(Socket socket) {
        //Create new handler for accepted socket and add it to the list
        SocketHandler newHandler = new SocketHandler(this, socket);
        clientHandlers.add(newHandler);

        //Start the handler on a new thread
        new Thread(newHandler).start();
    }

    void removeClientSocket(SocketHandler socketHandler) {
        //Remove client handler from list and end its processes
        socketHandler.terminateClient();
        clientHandlers.remove(socketHandler);
    }

    int getClientIndex(SocketHandler socketHandler) {
        //Get index of specific client handler
        return clientHandlers.indexOf(socketHandler);
    }

    void sendToAllExceptSender(SocketHandler sender, Message message) {
        //Relay a message to all clients except the sender
        for (SocketHandler handler : clientHandlers)
            if (handler != sender) handler.send(message);
    }

    void broadcastToAllExceptSender(SocketHandler sender, Message message) {
        //Relay message to all clients
        for (SocketHandler handler : clientHandlers)
            if (handler != sender) handler.send(message);
    }
}
