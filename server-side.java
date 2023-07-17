import java.io.*;
import java.net.*;
import java.sql.*;

public class Server {
    private static final int PORT = 1234;

    public static void main(String[] args) {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            System.out.println("Server started. Listening on port " + PORT);

            while (true) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("Client connected: " + clientSocket);

                    // Handle client connection in a separate thread
                    ClientHandler clientHandler = new ClientHandler(clientSocket);
                    clientHandler.start();
                } catch (IOException e) {
                    System.err.println("Error accepting client connection: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.err.println("Error starting the server: " + e.getMessage());
        }
    }
}

class ClientHandler extends Thread {
    private Socket clientSocket;
    private Connection dbConnection;

    public ClientHandler(Socket clientSocket) {
        this.clientSocket = clientSocket;
    }

    public void run() {
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            establishDatabaseConnection();
            handleClientRequests(in, out);

        } catch (IOException e) {
            System.err.println("Error handling client request: " + e.getMessage());
        } finally {
            closeDatabaseConnection();
            closeClientSocket();
        }
    }

    private void establishDatabaseConnection() {
        try {
            dbConnection = DriverManager.getConnection("jdbc:mariadb://localhost:3306/mydb", "username", "password");
        } catch (SQLException e) {
            System.err.println("Error connecting to the database: " + e.getMessage());
        }
    }

    private void handleClientRequests(BufferedReader in, PrintWriter out) throws IOException {
        String request;
        while ((request = in.readLine()) != null) {
            String response = processRequest(request);
            out.println(response);
        }
    }

    private String processRequest(String request) {
        try {
            // Process the client request and interact with the database
            // Implement your business logic here and return appropriate responses
            // You can use JDBC to execute SQL queries and updates
            // For example:
            // ResultSet resultSet = dbConnection.createStatement().executeQuery("SELECT * FROM assets");
            // Process the result set and generate a response
            // Return the response as a string
            return "Response to the client request";
        } catch (SQLException e) {
            System.err.println("Error processing client request: " + e.getMessage());
            return "Error occurred. Please try again.";
        }
    }

    private void closeDatabaseConnection() {
        try {
            if (dbConnection != null) {
                dbConnection.close();
            }
        } catch (SQLException e) {
            System.err.println("Error closing the database connection: " + e.getMessage());
        }
    }

    private void closeClientSocket() {
        try {
            clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error closing the client socket: " + e.getMessage());
        }
    }
}
