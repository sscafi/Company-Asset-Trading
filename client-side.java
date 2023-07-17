import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.net.*;

public class ClientGUI {
    private static final String SERVER_HOST = "localhost";
    private static final int SERVER_PORT = 1234;

    private JFrame frame;
    private JTextArea textArea;
    private JTextField inputField;
    private JButton sendButton;
    private Socket clientSocket;
    private PrintWriter out;
    private BufferedReader in;

    public ClientGUI() {
        initializeGUI();

        try {
            clientSocket = new Socket(SERVER_HOST, SERVER_PORT);
            out = new PrintWriter(clientSocket.getOutputStream(), true);
            in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        } catch (IOException e) {
            System.err.println("Error connecting to the server: " + e.getMessage());
        }

        sendButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String input = inputField.getText();
                out.println(input);

                try {
                    String response = in.readLine();
                    textArea.append(response + "\n");
                } catch (IOException ex) {
                    System.err.println("Error reading server response: " + ex.getMessage());
                }

                inputField.setText("");
            }
        });
    }

    private void initializeGUI() {
        frame = new JFrame("Client");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(400, 300);

        textArea = new JTextArea();
        textArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(textArea);

        inputField = new JTextField();
        sendButton = new JButton("Send");

        JPanel panel = new JPanel(new BorderLayout());
        panel.add(scrollPane, BorderLayout.CENTER);
        panel.add(inputField, BorderLayout.NORTH);
        panel.add(sendButton, BorderLayout.SOUTH);

        frame.getContentPane().add(panel);
        frame.setVisible(true);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                new ClientGUI();
            }
        });
    }
}
