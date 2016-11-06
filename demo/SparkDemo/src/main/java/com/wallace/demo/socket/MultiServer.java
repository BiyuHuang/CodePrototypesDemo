package com.wallace.demo.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

/**
 * Created by Wallace on 2016/11/6.
 */
public class MultiServer extends Thread {
    private Socket m_Server;

    public MultiServer(Socket s) {
        this.m_Server = s;
    }

    public void run() {
        try {
            System.out.println(m_Server.getClass().getName());
            BufferedReader in = new BufferedReader(new InputStreamReader(m_Server.getInputStream()));
            PrintWriter out = new PrintWriter(m_Server.getOutputStream());

            while (true) {
                String str = in.readLine();
                System.out.println("[Server] Client: " + str);
                System.out.println("[Server] Received an message successfully.");
                out.println("Received an message successfully.");
                out.flush();
                if (str.equals("end"))
                    break;
            }
            m_Server.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        ServerSocket server = new ServerSocket(9999);
        while (true) {
            MultiServer ms = new MultiServer(server.accept());
            ms.start();
        }
    }
}
