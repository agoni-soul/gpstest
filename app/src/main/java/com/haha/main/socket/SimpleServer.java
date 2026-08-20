package com.haha.main.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

/**
 * @auther: haha
 * @Date: 2025/8/23
 * @Detail:
 */
public class SimpleServer {
    private ServerSocket serverSocket;
    private int port;

    public SimpleServer(int port) {
        this.port = port;
    }

    public void start() {
        try {
            // 创建服务器Socket，监听指定端口
            serverSocket = new ServerSocket(port);
            System.out.println("服务器已启动，监听端口: " + port);

            // 持续接受客户端连接
            while (true) {
                // 等待客户端连接（阻塞方法）
                Socket clientSocket = serverSocket.accept();
                System.out.println("新的客户端连接: " + clientSocket.getInetAddress().getHostAddress());

                // 为每个客户端创建新线程处理
                new ClientHandler(clientSocket).start();
            }
        } catch (IOException e) {
            System.err.println("服务器启动失败: " + e.getMessage());
        } finally {
            stop();
        }
    }

    public void stop() {
        try {
            if (serverSocket != null && !serverSocket.isClosed()) {
                serverSocket.close();
            }
        } catch (IOException e) {
            System.err.println("关闭服务器时出错: " + e.getMessage());
        }
    }

    // 内部类：处理客户端请求的线程
    private static class ClientHandler extends Thread {
        private final Socket clientSocket;

        public ClientHandler(Socket socket) {
            this.clientSocket = socket;
        }

        public void run() {
            try (
                    // 获取输入流，用于接收客户端数据
                    BufferedReader in = new BufferedReader(
                            new InputStreamReader(clientSocket.getInputStream()));
                    // 获取输出流，用于向客户端发送数据
                    PrintWriter out = new PrintWriter(
                            clientSocket.getOutputStream(), true)
            ) {
                String inputLine;
                // 读取客户端发送的数据
                while ((inputLine = in.readLine()) != null) {
                    System.out.println("收到客户端消息: " + inputLine);

                    // 处理请求并准备响应
                    String response = processRequest(inputLine);

                    // 发送响应回客户端
                    out.println(response);
                    System.out.println("发送响应: " + response);
                }
            } catch (IOException e) {
                System.err.println("处理客户端请求时出错: " + e.getMessage());
            } finally {
                try {
                    // 关闭客户端连接
                    clientSocket.close();
                } catch (IOException e) {
                    System.err.println("关闭客户端连接时出错: " + e.getMessage());
                }
            }
        }

        private String processRequest(String request) {
            // 简单的请求处理逻辑
            if (request.equalsIgnoreCase("time")) {
                return "当前时间: " + new Date().toString();
            } else if (request.equalsIgnoreCase("hello")) {
                return "你好，客户端!";
            } else {
                return "服务器已收到您的消息: " + request;
            }
        }
    }

    public static void main(String[] args) {
        SimpleServer server = new SimpleServer(12345);
        server.start();
    }
}
