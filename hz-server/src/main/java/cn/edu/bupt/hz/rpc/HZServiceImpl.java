package cn.edu.bupt.hz.rpc;

import cn.edu.bupt.hz.protobuf.generated.HProtos;
import com.google.protobuf.RpcCallback;
import com.google.protobuf.RpcController;

public class HZServiceImpl extends HProtos.HService {
    public void search(RpcController controller, HProtos.HRequest request, RpcCallback<HProtos.HResponse> done) {

    }
}
