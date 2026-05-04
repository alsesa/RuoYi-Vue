package com.ruoyi.socket.bio;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;

public class BoiClient {
    public static void main(String[] args) throws IOException, InterruptedException {
        Thread tom = new Thread(() -> {
            try {
                sendHello();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        Thread jerry = new Thread(() -> {
            try {
                sendHello();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        });
        tom.start();
        jerry.start();
        tom.join();
        jerry.join();

    }

    private static void sendHello() throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress("localhost", 8888));
        OutputStream outputStream = socket.getOutputStream();
        for (int i = 0; i < 10; i++) {
            outputStream.write((Thread.currentThread().getName() + "hello" + i).getBytes());
            outputStream.flush();
        }
        socket.close();
    }

    public static class User{
        private String name
                ;
        private int age;
    }
}
