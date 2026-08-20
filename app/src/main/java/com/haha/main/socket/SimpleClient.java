package com.haha.main.socket;

import java.io.*;
import java.net.*;
import java.util.Scanner;

/**
 * @auther: haha
 * @Date: 2025/8/23
 * @Detail:
 */
public class SimpleClient {
    private String hostname;
    private int port;

    public SimpleClient(String hostname, int port) {
        this.hostname = hostname;
        this.port = port;
    }

    public void start() {
        try (
                // 创建客户端Socket，连接到服务器
                Socket socket = new Socket(hostname, port);
                // 获取输出流，用于向服务器发送数据
                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
                // 获取输入流，用于接收服务器响应
                BufferedReader in = new BufferedReader(
                        new InputStreamReader(socket.getInputStream()));
                // 读取用户输入
                Scanner scanner = new Scanner(System.in)
        ) {
            System.out.println("已连接到服务器 " + hostname + ":" + port);
            System.out.println("输入消息发送给服务器 (输入 'exit' 退出):");

            String userInput;
            while (true) {
                // 读取用户输入
                userInput = scanner.nextLine();

                // 检查是否退出
                if (userInput.equalsIgnoreCase("exit")) {
                    break;
                }

                // 发送消息到服务器
                out.println(userInput);
                System.out.println("已发送: " + userInput);

                // 接收服务器响应
                String response = in.readLine();
                System.out.println("服务器响应: " + response);
            }
        } catch (UnknownHostException e) {
            System.err.println("无法找到主机: " + hostname);
        } catch (IOException e) {
            System.err.println("I/O错误: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("用法: java SimpleClient <hostname> <port>");
            System.exit(1);
        }

        String hostname = args[0];
        int port = Integer.parseInt(args[1]);

        SimpleClient client = new SimpleClient(hostname, port);
        client.start();
    }
}
