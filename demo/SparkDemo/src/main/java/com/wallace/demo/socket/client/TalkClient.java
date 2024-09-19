package com.wallace.demo.socket.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.util.Objects;


/**
 * Created by Wallace on 2016/11/4.
 * Server-Client模型之客户端
 */
public class TalkClient {

    private static Logger log = LoggerFactory.getLogger(TalkClient.class);

    public static void main(String args[]) {
        try {
            Socket server = new Socket(InetAddress.getLocalHost(), 9999);
            server.setSoTimeout(60000);
            BufferedReader in = new BufferedReader(new InputStreamReader(server.getInputStream()));
            PrintWriter out = new PrintWriter(server.getOutputStream());
            BufferedReader wt = new BufferedReader(new InputStreamReader(System.in));
            String str = wt.readLine();
            while (!Objects.equals(str, "end")) {
                out.println(str);
                out.flush();
                System.out.println("[Client] Client: " + str);
                System.out.println("[Client] Server: " + in.readLine());
                str = wt.readLine();
            }
            server.close();
        } catch (IOException e) {
            log.error("Failed to connect socket", e);
        }
    }
}
