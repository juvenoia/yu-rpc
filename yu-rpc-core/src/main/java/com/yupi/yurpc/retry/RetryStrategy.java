package com.yupi.yurpc.retry;

import com.yupi.yurpc.model.RpcResponse;

import java.util.concurrent.Callable;

public interface RetryStrategy {
    RpcResponse doRetry(Callable<RpcResponse> callable) throws Exception;
}
