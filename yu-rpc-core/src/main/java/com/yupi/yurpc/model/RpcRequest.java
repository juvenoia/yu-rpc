package com.yupi.yurpc.model;

import com.yupi.yurpc.Config.RpcConfig;
import com.yupi.yurpc.constant.RpcConstant;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class RpcRequest implements Serializable {
    private String serviceName;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] args;

    private String serviceVersion = RpcConstant.DEFAULT_SERVICE_VERSION;
}
