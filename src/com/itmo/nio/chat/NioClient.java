package com.itmo.nio.chat;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Scanner;

/**
 * Created by xmitya on 10.01.17.
 */
public class NioClient {
    public static void main(String[] args) throws IOException {
        InetSocketAddress isa = new InetSocketAddress("localhost", 12345);

//        new Thread(new NioClientWorker(socket)).start();
          new NioClientWorker(isa).run();
    }

    private static class NioClientWorker extends Worker {
        private static final int BUF_SIZE = 1024;
        private ByteBuffer buf;
        private SocketChannel ch;
        private Selector sel;
        private InetSocketAddress isa;
        Scanner scanner;

        public NioClientWorker(InetSocketAddress isa) {
            this.isa = isa;
        }

        @Override
        protected void init() throws Exception {
            ch = SocketChannel.open();
            ch.configureBlocking(false);
            ch.connect(isa);
            buf = ByteBuffer.allocate(BUF_SIZE);
            sel = Selector.open();
            scanner = new Scanner(System.in);

            ch.register(sel, SelectionKey.OP_WRITE);
        }

        @Override
        protected void loop() throws Exception {
            String msg = scanner.nextLine();

            buf.put(msg.getBytes());
            // Записываем данны из буфера.
            ch.write(buf);

        }

        @Override
        protected void stop() throws Exception {
            ch.close();
            sel.close();
        }
    }

    private static class Sender extends Worker {
        private OutputStream out;
        private final Socket socket;
        private Scanner scanner;

        public Sender(Socket socket) {
            this.socket = socket;
        }

        @Override
        protected void loop() throws Exception {
            String msg = scanner.nextLine();

            out.write(msg.getBytes("utf-8"));
        }

        @Override
        protected void init() throws Exception {
            out = socket.getOutputStream();
            scanner = new Scanner(System.in);
        }

        @Override
        protected void stop() throws Exception {
            socket.close();
        }
    }
}
