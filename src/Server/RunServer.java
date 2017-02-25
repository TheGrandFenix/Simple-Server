package Server;

public class RunServer {
    public static void main(String[] args) {
        System.out.println("Starting server...");
        ServerCore serverProcess = new ServerCore(2508);
        new Thread(serverProcess).start();
    }
}
