import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;
import java.util.concurrent.*;

public class ClientGUI {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 1234;
    private static final int TIMEOUT_MS = 5000;

    private JFrame frame;
    private JTextArea textArea;
    private JTextField inputField;
    private JButton sendButton;
    private JButton connectButton;
    private JButton disconnectButton;
    private JLabel statusLabel;
    
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;
    private ExecutorService executor;
    private boolean isConnected = false;

    public ClientGUI() {
        initializeGUI();
        setupEventHandlers();
    }

    private void initializeGUI() {
        frame = new JFrame("Chat Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(500, 400);
        frame.setLayout(new BorderLayout());

        // Status panel
        JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        statusLabel = new JLabel("Disconnected");
        statusLabel.setForeground(Color.RED);
        statusPanel.add(statusLabel);
        
        connectButton = new JButton("Connect");
        disconnectButton = new JButton("Disconnect");
        disconnectButton.setEnabled(false);
        statusPanel.add(connectButton);
        statusPanel.add(disconnectButton);

        // Text area for messages
        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setLineWrap(true);
        textArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(textArea);
        scrollPane.setPreferredSize(new Dimension(400, 250));

        // Input panel
        JPanel inputPanel = new JPanel(new BorderLayout());
        inputField = new JTextField();
        inputField.setEnabled(false);
        sendButton = new JButton("Send");
        sendButton.setEnabled(false);
        
        inputPanel.add(new JLabel("Message: "), BorderLayout.WEST);
        inputPanel.add(inputField, BorderLayout.CENTER);
        inputPanel.add(sendButton, BorderLayout.EAST);

        // Add components to frame
        frame.add(statusPanel, BorderLayout.NORTH);
        frame.add(scrollPane, BorderLayout.CENTER);
        frame.add(inputPanel, BorderLayout.SOUTH);

        // Center the window
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        
        // Initialize thread pool
        executor = Executors.newFixedThreadPool(2);
    }

    private void setupEventHandlers() {
        // Connect button action
        connectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                connectToServer();
            }
        });

        // Disconnect button action
        disconnectButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                disconnectFromServer();
            }
        });

        // Send button action
        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        // Enter key in input field
        inputField.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        // Window close listener for cleanup
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                cleanup();
            }
        });
    }

    private void connectToServer() {
        executor.execute(new Runnable() {
            public void run() {
                try {
                    updateStatus("Connecting...", Color.ORANGE);
                    
                    clientSocket = new Socket();
                    clientSocket.connect(new InetSocketAddress(SERVER_HOST, SERVER_PORT), TIMEOUT_MS);
                    out = new PrintWriter(clientSocket.getOutputStream(), true);
                    in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
                    
                    isConnected = true;
                    
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            updateStatus("Connected to " + SERVER_HOST + ":" + SERVER_PORT, Color.GREEN);
                            connectButton.setEnabled(false);
                            disconnectButton.setEnabled(true);
                            inputField.setEnabled(true);
                            sendButton.setEnabled(true);
                            inputField.requestFocus();
                        }
                    });
                    
                    // Start listening for server messages
                    startMessageListener();
                    
                    textArea.append("Connected to server successfully!\n");
                    
                } catch (SocketTimeoutException e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            showError("Connection timeout: Server not responding");
                            updateStatus("Connection failed", Color.RED);
                        }
                    });
                } catch (ConnectException e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            showError("Connection refused: Server may not be running");
                            updateStatus("Connection refused", Color.RED);
                        }
                    });
                } catch (IOException e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            showError("Connection error: " + e.getMessage());
                            updateStatus("Connection error", Color.RED);
                        }
                    });
                }
            }
        });
    }

    private void disconnectFromServer() {
        cleanup();
        updateStatus("Disconnected", Color.RED);
        connectButton.setEnabled(true);
        disconnectButton.setEnabled(false);
        inputField.setEnabled(false);
        sendButton.setEnabled(false);
        textArea.append("Disconnected from server.\n");
    }

    private void sendMessage() {
        String message = inputField.getText().trim();
        if (message.isEmpty()) {
            return;
        }
        
        if (!isConnected) {
            showError("Not connected to server");
            return;
        }

        executor.execute(new Runnable() {
            public void run() {
                try {
                    out.println(message);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            textArea.append("You: " + message + "\n");
                            inputField.setText("");
                            inputField.requestFocus();
                        }
                    });
                } catch (Exception e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            showError("Failed to send message: " + e.getMessage());
                        }
                    });
                }
            }
        });
    }

    private void startMessageListener() {
        executor.execute(new Runnable() {
            public void run() {
                try {
                    String serverMessage;
                    while (isConnected && (serverMessage = in.readLine()) != null) {
                        final String message = serverMessage;
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                textArea.append("Server: " + message + "\n");
                                // Auto-scroll to bottom
                                textArea.setCaretPosition(textArea.getDocument().getLength());
                            }
                        });
                    }
                } catch (IOException e) {
                    if (isConnected) {
                        SwingUtilities.invokeLater(new Runnable() {
                            public void run() {
                                showError("Connection lost: " + e.getMessage());
                                disconnectFromServer();
                            }
                        });
                    }
                }
            }
        });
    }

    private void updateStatus(String message, Color color) {
        statusLabel.setText(message);
        statusLabel.setForeground(color);
    }

    private void showError(String errorMessage) {
        JOptionPane.showMessageDialog(frame, errorMessage, "Error", JOptionPane.ERROR_MESSAGE);
    }

    private void cleanup() {
        isConnected = false;
        
        try {
            if (out != null) out.close();
            if (in != null) in.close();
            if (clientSocket != null) clientSocket.close();
        } catch (IOException e) {
            System.err.println("Error during cleanup: " + e.getMessage());
        }
        
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(2, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
    }

    public static void main(String[] args) {
        // Set system look and feel
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeel());
        } catch (Exception e) {
            System.err.println("Error setting look and feel: " + e.getMessage());
        }

        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ClientGUI();
            }
        });
    }
}
