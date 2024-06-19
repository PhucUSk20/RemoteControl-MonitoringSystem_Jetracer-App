package com.store.faskcamera;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;

public class ConnectionManager {
    private static ConnectionManager instance;
    private Socket socket;
    private PrintWriter printWriter;
    private boolean connected;
    private ConnectionListener connectionListener;

    public interface ConnectionListener {
        void onConnected();
    }

    private ConnectionManager() {}

    public static ConnectionManager getInstance() {
        if (instance == null) {
            instance = new ConnectionManager();
        }
        return instance;
    }

    public void setConnectionListener(ConnectionListener listener) {
        this.connectionListener = listener;
    }

    public synchronized void startConnection(String ip, int port) {
        stopConnection(); // Hủy kết nối hiện tại nếu tồn tại

        new Thread(() -> {
            try {
                socket = new Socket(ip, port);
                printWriter = new PrintWriter(socket.getOutputStream(), true);
                connected = true;
                if (connectionListener != null) {
                    connectionListener.onConnected();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public void sendMessage(String message) {
        new Thread(() -> {
            if (printWriter != null) {
                printWriter.println(message);
            }
        }).start();
    }

    public synchronized void stopConnection() {
        new Thread(() -> {
            try {
                if (socket != null && !socket.isClosed()) {
                    socket.close();
                }
                if (printWriter != null) {
                    printWriter.close();
                }
                connected = false;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();
    }

    public boolean isConnected() {
        return connected;
    }
}
