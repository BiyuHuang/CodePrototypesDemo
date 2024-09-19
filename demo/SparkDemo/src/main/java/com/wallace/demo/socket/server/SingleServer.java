package com.wallace.demo.socket.server;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Objects;

/**
 * Created by Wallace on 2016/11/4.
 * 一对一：Server-Client模型
 */
public class SingleServer {
    private static Logger log = LoggerFactory.getLogger(SingleServer.class);

    public static void main(String args[]) {
        try {
            ServerSocket server = new ServerSocket(9999);
            Socket client = server.accept();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    client.getInputStream()));
            PrintWriter out = new PrintWriter(client.getOutputStream());

            /**The message from Server*/
            BufferedReader sin = new BufferedReader(new InputStreamReader(System.in));
            String client_msg = in.readLine();
            String response = sin.readLine();
            if (response != null) {
                response = sin.readLine();
            }
            while (client_msg != null && !Objects.equals(client_msg, "end")) {
                System.out.println("[Server] Message from Client: " + client_msg);
                String response_msg;
                if (response == null) {
                    response_msg = "Received an message successfully.";
                } else {
                    response_msg = "Received an message successfully." +
                            "<RESPONSE_MESSAGE>" + response;
                }
                System.out.println("[Server] Send an message to Client: " + response_msg);
                out.println(response_msg);
                out.flush();
                client_msg = in.readLine();
                response = sin.readLine();
            }
            client.close();
        } catch (Exception e) {
            System.out.println("Error:" + e);
        }
    }
}

