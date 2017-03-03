package Server;

import java.io.*;
import java.net.Socket;

public class SocketHandler implements Runnable{
    //Define local running variable
    private volatile boolean running = true;

    //Define dependency variables
    private ServerCore serverProcess;
    private Socket socket;

    //Define data streams
    private ObjectOutputStream out;
    private ObjectInputStream in;

    SocketHandler(ServerCore serverProcess, Socket clientSocket) {
        //Initialize dependency variables
        this.serverProcess = serverProcess;
        socket = clientSocket;
    }

    public void run() {
        //Open data streams
        openStreams();

        //Listen for incoming data
        listen();
    }

    private void openStreams() {
        try {
            //Initialize data streams
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            //Catch and output exceptions when opening data streams
            System.err.println("Failed to open data streams...");
            e.printStackTrace();

            //Terminate client in case of error
            serverProcess.removeClientSocket(this);
        }
    }

    private void listen() {
        while (running) {
            //Listen for incoming data
            processIncomingData();
        }
    }

    private void processIncomingData() {
        try {
            //Try to handle input
            serverProcess.sendToAllExceptSender(this, (Message) in.readObject());
        } catch (IOException | ClassNotFoundException ignored) {
            //Detect client disconnect
            serverProcess.removeClientSocket(this);
        }
    }

    void send(Message message) {
        try {
            //Try to send message to client
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            //Catch and output exceptions when sending message
            System.err.println("Failed to send message to client " + serverProcess.getClientIndex(this) + "...");
            e.printStackTrace();

            //Terminate client in case of error
            serverProcess.removeClientSocket(this);
        }
    }

    private void closeStreams() {
        try {
            //Try to close data streams
            in.close();
            out.close();
        } catch (IOException e) {
            //Catch and output exceptions when closing streams
            System.err.println("Failed to close streams for client " + serverProcess.getClientIndex(this) + "...");
            e.printStackTrace();
        }
    }

    private void closeSocket() {
        try {
            //Try to close socket
            socket.close();
        } catch (IOException e) {
            //Catch and output exceptions when closing socket
            System.err.println("Failed to close streams for client " + serverProcess.getClientIndex(this) + "...");
            e.printStackTrace();
        }
    }

    void terminateClient() {
        //Close the client
        running = false;
        closeStreams();
        closeSocket();
        System.out.println(serverProcess.getClientIndex(this) + " disconnected...");
    }

}
