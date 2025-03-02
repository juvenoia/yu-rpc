package com.yupi.yurpc.fault;

import com.yupi.yurpc.model.RpcResponse;

import java.util.Map;

public interface TolerantStrategy {
    RpcResponse doTolerant(Map<String, Object> context, Exception e);
}
