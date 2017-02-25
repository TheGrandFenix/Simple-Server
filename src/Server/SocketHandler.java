package Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;

public class SocketHandler implements Runnable{
    //Define username variable
    String username;

    //Define local running variable
    private volatile boolean running = true;

    //Define dependency variables
    private ServerCore serverProcess;
    private Socket socket;

    //Define data streams
    private DataInputStream in;
    private DataOutputStream out;

    SocketHandler(ServerCore serverProcess, Socket clientSocket) {
        //Initialize dependency variables
        this.serverProcess = serverProcess;
        socket = clientSocket;
    }

    public void run() {
        //Open data streams
        openStreams();

        //Request a username
        requestUsername();
        if (username != null) {
            System.out.println("[" + serverProcess.getClientIndex(this) + "] " + username + " connected...");
            serverProcess.broadcastToAllExceptSender(this,username + " connected...");
        }

        //Listen for incoming data
        listen();
    }

    private void openStreams() {
        try {
            //Initialize data streams
            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            //Catch and output exceptions when opening data streams
            System.err.println("Failed to open data streams...");
            e.printStackTrace();

            //Terminate client in case of error
            serverProcess.removeClientSocket(this);
        }
    }

    private void requestUsername() {
        //Request and update username
        send("Please enter a username...");
        updateUsername();
        send("Username set to \"" + username + "\", proceed to chat...");
    }

    private void updateUsername() {
        try {
            //Try to read username from input stream
            username = in.readUTF();
        } catch (IOException e) {
            //Catch and output exceptions when updating username
            System.err.println("Failed to read username...");
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
            serverProcess.sendToAllExceptSender(this, in.readUTF());
        } catch (IOException ignored) {
            //Detect client disconnect
            if (username != null) serverProcess.broadcastToAllExceptSender(this, username + " disconnected...");
            serverProcess.removeClientSocket(this);
        }
    }

    void send(String message) {
        try {
            //Try to send message to client
            out.writeUTF(message);
            out.flush();
        } catch (IOException e) {
            //Catch and output exceptions when sending message
            System.err.println("Failed to send message to client " + serverProcess.getClientIndex(this) + "[" + username + "]" + "...");
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
            System.err.println("Failed to close streams for client " + serverProcess.getClientIndex(this) + " [" + username + "]" + "...");
            e.printStackTrace();
        }
    }

    private void closeSocket() {
        try {
            //Try to close socket
            socket.close();
        } catch (IOException e) {
            //Catch and output exceptions when closing socket
            System.err.println("Failed to close streams for client " + serverProcess.getClientIndex(this) + " [" + username + "]" + "...");
            e.printStackTrace();
        }
    }

    void terminateClient() {
        //Close the client
        running = false;
        closeStreams();
        closeSocket();
        if (username!=null) System.out.println("[" + serverProcess.getClientIndex(this) + "] " + username + " disconnected...");
    }

}
