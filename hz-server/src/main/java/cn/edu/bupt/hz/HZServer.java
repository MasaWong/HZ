package cn.edu.bupt.hz;

import cn.edu.bupt.hz.rpc.RpcServer;
import cn.edu.bupt.hz.rpc.ServerRpcConnectionFactory;
import cn.edu.bupt.hz.rpc.SocketRpcConnectionFactories;

import java.util.concurrent.Executors;

public class HZServer {

    private static final int port = 8988;
    private static final int threadPoolSize = 4;

    public void start() {
        // Start server
        ServerRpcConnectionFactory rpcConnectionFactory = SocketRpcConnectionFactories
                .createServerRpcConnectionFactory(port);
        RpcServer server = new RpcServer(rpcConnectionFactory,
                Executors.newFixedThreadPool(threadPoolSize), true);
        server.registerService(); // For non-blocking impl
        server.registerBlockingService(); // For blocking impl
        server.run();
    }
}
