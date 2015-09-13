import javax.swing.*;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import java.awt.event.*;
import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;

public class TCPClient extends JDialog {
    private JPanel contentPane;
    private JButton buttonOK;
    private JButton buttonCancel;
    private JTextField textField1;
    private JButton connectButton;
    private JScrollPane scrollPane1;
    private JTextArea textArea1;

    private Socket client;
    private BufferedInputStream in;
    private Thread thread = new Thread() {
        public void run() {
            TCPClient.this.run();
        }
    };

    public TCPClient() {
        setContentPane(contentPane);
        setModal(true);
        getRootPane().setDefaultButton(buttonOK);

        buttonOK.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });

        buttonCancel.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        });

// call onCancel() when cross is clicked
        setDefaultCloseOperation(DO_NOTHING_ON_CLOSE);
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                onCancel();
            }
        });

// call onCancel() on ESCAPE
        contentPane.registerKeyboardAction(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                onCancel();
            }
        }, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);
        connectButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                onOK();
            }
        });
    }

    private void onOK() {
        if (connectButton.getText().equals("Connect")) {
            if (connect())
                connectButton.setText("Disconnect");
        } else {
            disconnect();
            connectButton.setText("Connect");
        }
    }

    private void onCancel() {
// add your code here if necessary
        dispose();
    }

    private boolean connect() {
        try{
            client = new Socket();
            client.connect(new InetSocketAddress(textField1.getText(), 4321), 4000);
            in = new BufferedInputStream(client.getInputStream());
            textArea1.setText("Connection established to " + textField1.getText() + "\n");
            thread.start();
            return true;
        } catch(UnknownHostException e) {
            log("Unknown host: " + textField1.getText() + " (" + e.getMessage() + ")");

        } catch(IOException e) {
            log("No I/O");
        }
        return false;
    }

    private void disconnect() {
        if (client != null) {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    ByteBuffer b = ByteBuffer.allocate(48);
    private void run() {
        while (!thread.isInterrupted()) {
            try {
                b.rewind();
                int size = 0;
                int maxSize = 4;
                while (size < maxSize) {
                    size += in.read(b.array(), size, maxSize - size);
                }
                b.rewind();
                int tmp = b.getInt();
                switch (tmp) {
                    case 1:
                        maxSize = 20;
                        break;
                    case 2:
                        maxSize = 32;
                        break;
                    default:
                        log("Received garbage: " + tmp);
                        return;
                }
                while (size < maxSize) {
                    size += in.read(b.array(), size, maxSize - size);
                }
                b.rewind();
                log("Bytes received: 0x" + bytesToHex(b.array(), maxSize));
                tmp = b.getInt();
                if (tmp == 1) {
                    log("Frame header received: Frame no. " + b.getInt() + ", timestamp: " + b.getLong());
                    //b.position(b.position() + 20);
                    size = b.getInt();
                    log("Size: " + size + " bytes. Current System time:" + System.currentTimeMillis());
                    while (size > 0) {
                        size -= in.skip(size);
                    }
                } else if (tmp == 2) {
                    b.position(b.position() + 12);
                    log("Frame metadata update: " + b.getInt() + "x" + b.getInt() + "@" + b.getInt() + "fps, format: " + b.getInt());
                }
                else {
                    log("Received garbage, this should never happen: " + tmp);
                    return;
                }
            } catch (IOException e) {
                log(e.getMessage());
                e.printStackTrace();
                return;
            }
        }
    }

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes, int length) {
        char[] hexChars = new char[length * 2];
        for ( int j = 0; j < length; j++ ) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    public static String bytesToHex(byte[] bytes) {
        return bytesToHex(bytes, bytes.length);
    }

    public void log(final String text) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Document doc = textArea1.getDocument();
                try {
                    doc.insertString(doc.getLength(), text + "\n", null);
                    JScrollBar vertical = scrollPane1.getVerticalScrollBar();
                    vertical.setValue(vertical.getMaximum());
                } catch (BadLocationException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public static void main(String[] args) {
        TCPClient dialog = new TCPClient();
        dialog.pack();
        dialog.setVisible(true);
        System.exit(0);
    }
}
