import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.concurrent.*;
import java.util.Properties;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

public class Server {
    private static final int PORT = 1234;
    private static final int MAX_THREADS = 50;
    private static final int SOCKET_TIMEOUT = 30000; // 30 seconds
    private static final ServerConfig config = new ServerConfig();
    private static volatile boolean isRunning = true;
    private static ExecutorService threadPool;
    private static ConnectionPool connectionPool;

    public static void main(String[] args) {
        // Add shutdown hook for graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            shutdown();
        }));

        System.out.println("Starting server...");
        
        try {
            // Initialize connection pool
            connectionPool = new ConnectionPool(
                config.getDbUrl(),
                config.getDbUsername(),
                config.getDbPassword(),
                10 // pool size
            );

            // Initialize thread pool
            threadPool = Executors.newFixedThreadPool(MAX_THREADS);
            
            startServer();
            
        } catch (Exception e) {
            System.err.println("Failed to start server: " + e.getMessage());
            shutdown();
        }
    }

    private static void startServer() {
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            serverSocket.setSoTimeout(1000); // Accept timeout for graceful shutdown
            System.out.println("Server started successfully on port " + PORT);
            System.out.println("Database: " + config.getDbUrl());

            while (isRunning) {
                try {
                    Socket clientSocket = serverSocket.accept();
                    clientSocket.setSoTimeout(SOCKET_TIMEOUT);
                    
                    System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());
                    
                    // Handle client in thread pool
                    ClientHandler clientHandler = new ClientHandler(clientSocket, connectionPool);
                    threadPool.execute(clientHandler);
                    
                } catch (SocketTimeoutException e) {
                    // Expected during graceful shutdown, continue loop
                    continue;
                } catch (IOException e) {
                    if (isRunning) {
                        System.err.println("Error accepting client connection: " + e.getMessage());
                    }
                }
            }
        } catch (IOException e) {
            System.err.println("Error starting the server: " + e.getMessage());
        }
    }

    private static void shutdown() {
        System.out.println("Shutting down server...");
        isRunning = false;
        
        if (threadPool != null) {
            threadPool.shutdown();
            try {
                if (!threadPool.awaitTermination(5, TimeUnit.SECONDS)) {
                    threadPool.shutdownNow();
                }
            } catch (InterruptedException e) {
                threadPool.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        
        if (connectionPool != null) {
            connectionPool.close();
        }
        
        System.out.println("Server shutdown complete.");
    }
}

class ClientHandler implements Runnable {
    private Socket clientSocket;
    private ConnectionPool connectionPool;
    private Connection dbConnection;

    public ClientHandler(Socket clientSocket, ConnectionPool connectionPool) {
        this.clientSocket = clientSocket;
        this.connectionPool = connectionPool;
    }

    public void run() {
        String clientInfo = clientSocket.getInetAddress().getHostAddress() + ":" + clientSocket.getPort();
        
        try (BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
             PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true)) {

            // Get database connection from pool
            dbConnection = connectionPool.getConnection();
            
            // Send welcome message
            out.println("Connected to server. Type 'HELP' for commands.");
            
            handleClientRequests(in, out, clientInfo);

        } catch (SQLException e) {
            System.err.println("Database error for client " + clientInfo + ": " + e.getMessage());
            try {
                clientSocket.getOutputStream().write("Database error. Please try again.\n".getBytes());
            } catch (IOException ioe) {
                // Ignore
            }
        } catch (IOException e) {
            if (!e.getMessage().contains("reset") && !e.getMessage().contains("closed")) {
                System.err.println("I/O error with client " + clientInfo + ": " + e.getMessage());
            }
        } finally {
            cleanup(clientInfo);
        }
    }

    private void handleClientRequests(BufferedReader in, PrintWriter out, String clientInfo) throws IOException {
        String request;
        int requestCount = 0;
        final int MAX_REQUESTS = 1000; // Prevent abuse

        while ((request = in.readLine()) != null && requestCount < MAX_REQUESTS) {
            requestCount++;
            
            if (request.equalsIgnoreCase("QUIT") || request.equalsIgnoreCase("EXIT")) {
                out.println("Goodbye!");
                break;
            }
            
            if (request.trim().isEmpty()) {
                continue;
            }

            System.out.println("Request from " + clientInfo + ": " + request);
            
            String response = processRequest(request, clientInfo);
            out.println(response);
        }

        if (requestCount >= MAX_REQUESTS) {
            System.out.println("Client " + clientInfo + " reached request limit");
        }
    }

    private String processRequest(String request, String clientInfo) {
        try {
            // Basic command parsing
            String[] parts = request.trim().split("\\s+");
            String command = parts[0].toUpperCase();

            switch (command) {
                case "HELP":
                    return getHelpMessage();
                    
                case "TIME":
                    return "Server time: " + new java.util.Date();
                    
                case "STATS":
                    return getServerStats();
                    
                case "ECHO":
                    return request.substring(5); // Return everything after "ECHO "
                    
                case "USERCOUNT":
                    return getUserCount();
                    
                case "HASH":
                    if (parts.length > 1) {
                        return "Hash: " + generateHash(parts[1]);
                    } else {
                        return "Usage: HASH <text>";
                    }
                    
                default:
                    return executeCustomQuery(request, clientInfo);
            }
            
        } catch (Exception e) {
            System.err.println("Error processing request from " + clientInfo + ": " + e.getMessage());
            return "Error: " + e.getMessage();
        }
    }

    private String executeCustomQuery(String request, String clientInfo) throws SQLException {
        // Basic SQL injection prevention
        if (containsSqlInjection(request)) {
            System.out.println("Potential SQL injection attempt from " + clientInfo);
            return "Error: Invalid request format";
        }

        // Example: Handle SELECT queries
        if (request.trim().toUpperCase().startsWith("SELECT")) {
            try (Statement stmt = dbConnection.createStatement();
                 ResultSet rs = stmt.executeQuery(request)) {
                
                StringBuilder result = new StringBuilder();
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                
                // Build header
                for (int i = 1; i <= columnCount; i++) {
                    result.append(metaData.getColumnName(i));
                    if (i < columnCount) result.append(" | ");
                }
                result.append("\n");
                
                // Build rows
                int rowCount = 0;
                while (rs.next() && rowCount < 100) { // Limit to 100 rows
                    for (int i = 1; i <= columnCount; i++) {
                        result.append(rs.getString(i));
                        if (i < columnCount) result.append(" | ");
                    }
                    result.append("\n");
                    rowCount++;
                }
                
                if (rowCount == 0) {
                    return "No results found";
                }
                
                return result.toString();
            }
        }
        
        // Handle other query types
        try (Statement stmt = dbConnection.createStatement()) {
            int affectedRows = stmt.executeUpdate(request);
            return "Query executed successfully. Affected rows: " + affectedRows;
        }
    }

    private boolean containsSqlInjection(String input) {
        String[] dangerousPatterns = {
            "--", ";", "/*", "*/", "xp_", "sp_", "UNION", "DROP", "DELETE", "INSERT", "UPDATE", 
            "EXEC", "EXECUTE", "TRUNCATE", "CREATE", "ALTER"
        };
        
        String upperInput = input.toUpperCase();
        for (String pattern : dangerousPatterns) {
            if (upperInput.contains(pattern)) {
                return true;
            }
        }
        return false;
    }

    private String generateHash(String text) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(text.getBytes());
            return Base64.getEncoder().encodeToString(hash);
        } catch (NoSuchAlgorithmException e) {
            return "Error generating hash";
        }
    }

    private String getHelpMessage() {
        return "Available commands:\n" +
               "HELP - Show this message\n" +
               "TIME - Get server time\n" +
               "STATS - Get server statistics\n" +
               "ECHO <text> - Echo back text\n" +
               "USERCOUNT - Get user count from database\n" +
               "HASH <text> - Generate SHA-256 hash\n" +
               "QUIT - Disconnect from server";
    }

    private String getServerStats() {
        return "Server Statistics:\n" +
               "Port: " + Server.PORT + "\n" +
               "Uptime: " + "N/A\n" + // Could implement with startup time
               "Database: Connected";
    }

    private String getUserCount() throws SQLException {
        try (Statement stmt = dbConnection.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) as count FROM users")) {
            if (rs.next()) {
                return "User count: " + rs.getInt("count");
            }
        }
        return "User count: 0";
    }

    private void cleanup(String clientInfo) {
        System.out.println("Client disconnected: " + clientInfo);
        
        try {
            if (dbConnection != null) {
                connectionPool.releaseConnection(dbConnection);
            }
            if (clientSocket != null && !clientSocket.isClosed()) {
                clientSocket.close();
            }
        } catch (Exception e) {
            System.err.println("Error during cleanup for client " + clientInfo + ": " + e.getMessage());
        }
    }
}

// Connection pool implementation
class ConnectionPool {
    private final BlockingQueue<Connection> pool;
    private final String url;
    private final String username;
    private final String password;
    private final int maxSize;
    private int currentSize;

    public ConnectionPool(String url, String username, String password, int maxSize) throws SQLException {
        this.url = url;
        this.username = username;
        this.password = password;
        this.maxSize = maxSize;
        this.pool = new LinkedBlockingQueue<>(maxSize);
        
        // Initialize minimum connections
        for (int i = 0; i < Math.min(2, maxSize); i++) {
            pool.add(createNewConnection());
        }
    }

    public Connection getConnection() throws SQLException {
        Connection conn = pool.poll();
        if (conn != null && !conn.isClosed() && conn.isValid(2)) {
            return conn;
        }
        
        synchronized (this) {
            if (currentSize < maxSize) {
                return createNewConnection();
            }
        }
        
        // Wait for available connection
        try {
            return pool.take();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SQLException("Interrupted while waiting for connection");
        }
    }

    public void releaseConnection(Connection conn) {
        if (conn != null) {
            pool.offer(conn);
        }
    }

    private Connection createNewConnection() throws SQLException {
        currentSize++;
        return DriverManager.getConnection(url, username, password);
    }

    public void close() {
        for (Connection conn : pool) {
            try {
                if (conn != null && !conn.isClosed()) {
                    conn.close();
                }
            } catch (SQLException e) {
                System.err.println("Error closing connection: " + e.getMessage());
            }
        }
        pool.clear();
    }
}

// Configuration class
class ServerConfig {
    private String dbUrl = "jdbc:mariadb://localhost:3306/mydb";
    private String dbUsername = "username";
    private String dbPassword = "password";

    public ServerConfig() {
        // Load from properties file if available
        loadFromProperties();
    }

    private void loadFromProperties() {
        try (InputStream input = new FileInputStream("server.properties")) {
            Properties prop = new Properties();
            prop.load(input);
            
            dbUrl = prop.getProperty("db.url", dbUrl);
            dbUsername = prop.getProperty("db.username", dbUsername);
            dbPassword = prop.getProperty("db.password", dbPassword);
            
        } catch (IOException e) {
            System.out.println("No server.properties found, using default configuration");
        }
    }

    public String getDbUrl() { return dbUrl; }
    public String getDbUsername() { return dbUsername; }
    public String getDbPassword() { return dbPassword; }
}
