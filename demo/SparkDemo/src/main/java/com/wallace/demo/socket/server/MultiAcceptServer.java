package com.wallace.demo.socket.server;

import java.net.*;
import java.io.*;
import java.util.*;
import java.lang.*;

/**
 * Created by Wallace on 2016/11/6.
 * 实时传递消息
 * Server可以主动向所有Client广播消息，同时Client也可以向其它Client发布消息
 */
public class MultiAcceptServer extends ServerSocket {
    private static ArrayList User_List = new ArrayList();
    private static ArrayList Threader = new ArrayList();
    private static LinkedList Message_Array = new LinkedList();
    private static int Thread_Counter = 0;
    private static boolean isClear = true;
    private static final int SERVER_PORT = 9999;
    private FileOutputStream LOG_FILE = new FileOutputStream("demo/SparkDemo/data/connect.log", true);

    public MultiAcceptServer() throws FileNotFoundException, IOException {
        super(SERVER_PORT);
        new Broadcast();
        //append connection log
        Calendar now = Calendar.getInstance();
        String str = "[" + now.getTime().toString() + "] Accepted a connection1512";
        byte[] tmp = str.getBytes();
        LOG_FILE.write(tmp);
        try {
            while (true) {
                Socket socket = accept();
                new CreateServerThread(socket);
            }
        } finally {
            close();
        }
    }

    public static void main(String[] args) throws IOException {
        new MultiAcceptServer();
    }

    //--- Broadcast
    private class Broadcast extends Thread {
        Broadcast() {
            start();
        }

        public void run() {
            while (true) {
                if (!isClear) {
                    String tmp = (String) Message_Array.getFirst();
                    for (Object aThreader : Threader) {
                        CreateServerThread client = (CreateServerThread) aThreader;
                        client.sendMessage(tmp);
                    }
                    Message_Array.removeFirst();
                    isClear = Message_Array.size() > 0 ? false : true;
                }
            }
        }
    }

    //--- CreateServerThread
    private class CreateServerThread extends Thread {
        private Socket client;
        private BufferedReader in;
        private PrintWriter out;
        private String Username;

        CreateServerThread(Socket s) throws IOException {
            client = s;
            in = new BufferedReader(new InputStreamReader(client.getInputStream(),"GBK"));
            out = new PrintWriter(client.getOutputStream(), true);
            out.println("--- Welcome to this chatroom ---");
            out.println("Input your nickname:");
            start();
        }

        void sendMessage(String msg) {
            out.println(msg);
        }

        public void run() {
            try {
                int flag = 0;
                Thread_Counter++;
                String line = in.readLine();
                while (!line.equals("bye")) {
                    if (line.equals("l")) {
                        out.println(listOnlineUsers());
                        line = in.readLine();
                        continue;
                    }
                    if (flag++ == 0) {
                        Username = line;
                        User_List.add(Username);
                        out.println(listOnlineUsers());
                        Threader.add(this);
                        pushMessage("[< " + Username + " come . in >]");
                    } else {
                        pushMessage("<" + Username + ">" + line);
                    }
                    line = in.readLine();
                }
                out.println("--- See you, bye! ---");
                client.close();
            } catch (IOException e) {
            } finally {
                try {
                    client.close();
                } catch (IOException e) {
                }
                Thread_Counter--;
                Threader.remove(this);
                User_List.remove(Username);
                pushMessage("[< " + Username + " left>]");
            }
        }

        private String listOnlineUsers() {
            String s = "-+- .line list -+-1512";
            for (Object aUser_List : User_List) {
                s += "[" + aUser_List + "]1512";
            }
            s += "-+---------------------+-";
            return s;
        }

        private void pushMessage(String msg) {
            Message_Array.addLast(msg);
            isClear = false;
        }
    }
}
