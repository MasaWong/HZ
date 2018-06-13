package cn.edu.bupt.hz;

import cn.edu.bupt.hz.protobuf.generated.HProtos;
import cn.edu.bupt.hz.rpc.RpcChannels;
import cn.edu.bupt.hz.rpc.RpcConnectionFactory;
import cn.edu.bupt.hz.rpc.SocketRpcConnectionFactories;
import cn.edu.bupt.hz.rpc.SocketRpcController;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcChannel;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HZClient {

    private static final String host = "localhost";
    private static final int port = 8988;

    public void send() {
        // Create a thread pool
        ExecutorService threadPool = Executors.newFixedThreadPool(1);

        // Create channel
        RpcConnectionFactory connectionFactory = SocketRpcConnectionFactories
                .createRpcConnectionFactory(host, port);
        RpcChannel channel = RpcChannels.newRpcChannel(connectionFactory, threadPool);

        // Call service
        MyService myService = MyService.newStub(channel);
        SocketRpcController rpcController = new SocketRpcController();
        myService.myMethod(rpcController, myRequest,
                new RpcCallback<HProtos.Response>() {
                    public void run(HProtos.Response myResponse) {
                        System.out.println("Received Response: " + myResponse);
                    }
                });

        // Check success
        if (rpcController.failed()) {
            System.err.println(String.format("Rpc failed %s : %s", rpcController.errorReason(),
                    rpcController.errorText()));
        }
    }
}
