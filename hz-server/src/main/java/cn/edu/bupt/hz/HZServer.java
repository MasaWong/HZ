package cn.edu.bupt.hz;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Arrays;
import java.util.Iterator;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HZServer {

    private static final int PORT = 8988;
    private static final int SIZE = 5;

    private Selector selector;

    private boolean running = true;

    private ExecutorService service;
    private Reader[] readers;
    private int index;

    public HZServer() throws IOException {
        service = Executors.newFixedThreadPool(SIZE);
        readers = new Reader[SIZE];
        for (int i = 0; i < SIZE; ++i) {
            readers[i] = new Reader();
            service.submit(readers[i]);
        }

        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        serverSocketChannel.configureBlocking(false);

        InetSocketAddress address = new InetSocketAddress(PORT);
        serverSocketChannel.bind(address, 128);

        selector = Selector.open();
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
    }

    public void start() throws InterruptedException, IOException {
        while (running) {
            selector.select(); // FindBugs IS2_INCONSISTENT_SYNC
            Iterator<SelectionKey> keyIterator = selector.selectedKeys().iterator();
            while (keyIterator.hasNext()) {
                SelectionKey key = keyIterator.next();
                keyIterator.remove();

                if (key.isValid() && key.isAcceptable()) {
                    doAccept(key);
                }
            }
        }
    }

    public void stop() throws InterruptedException, IOException {
        running = false;
        service.shutdown();
        selector.close();
    }

    private void doAccept(SelectionKey selectionKey) throws IOException {
        ServerSocketChannel serverSocketChannel = (ServerSocketChannel) selectionKey.channel();

        SocketChannel channel;
        while ((channel = serverSocketChannel.accept()) != null) {
            channel.configureBlocking(false);

            index = (index + 1) % SIZE;
            SelectionKey readKey = readers[index].registerChannel(channel);
        }
    }

    private void doRead(SelectionKey selectionKey) throws IOException {
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        channel.read(buffer);
        System.out.println(Arrays.toString(buffer.array()));
    }

    private class Reader implements Runnable {

        private Selector readSelector;

        public Reader() throws IOException {
            readSelector = Selector.open();
        }

        @Override
        public void run() {
            try {
                readSelector.select();

                Iterator<SelectionKey> keyIterator = readSelector.selectedKeys().iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();

                    if (key.isValid() && key.isReadable()) {
                        doRead(key);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            } finally {
                try {
                    readSelector.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public synchronized SelectionKey registerChannel(SocketChannel channel)
                throws IOException {
            return channel.register(readSelector, SelectionKey.OP_READ);
        }
    }
}
