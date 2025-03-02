package com.yupi.yurpc.registry;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.collection.ConcurrentHashSet;
import cn.hutool.cron.CronUtil;
import cn.hutool.cron.task.Task;
import cn.hutool.json.JSON;
import cn.hutool.json.JSONUtil;
import com.yupi.yurpc.Config.RegistryConfig;
import com.yupi.yurpc.model.ServiceMetaInfo;
import io.etcd.jetcd.*;
import io.etcd.jetcd.options.GetOption;
import io.etcd.jetcd.options.PutOption;
import io.etcd.jetcd.watch.WatchEvent;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class EtcdRegistry implements Registry {

    private Client client;
    private KV kvClient;

    private static final String ETCD_ROOT_PATH = "/rpc/";

    private final Set<String> localRegisterNodeKeySet = new HashSet<>();

    private final RegistryServiceCache registryServiceCache = new RegistryServiceCache();
    @Override
    public void init(RegistryConfig registryConfig) {
        client = Client.builder()
                .endpoints(registryConfig.getAddress())
                .connectTimeout(Duration.ofMillis(registryConfig.getTimeout()))
                .build();
        kvClient = client.getKVClient();
        heartBeat();
    }

    @Override
    public void register(ServiceMetaInfo serviceMetaInfo) throws Exception {
        Lease leaseClient = client.getLeaseClient();
        long leaseId = leaseClient.grant(30).get().getID();

        String registryKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        ByteSequence key = ByteSequence.from(registryKey, StandardCharsets.UTF_8);
        ByteSequence value = ByteSequence.from(JSONUtil.toJsonStr(serviceMetaInfo), StandardCharsets.UTF_8);

        PutOption putOption = PutOption.builder()
                .withLeaseId(leaseId)
                .build();

        kvClient.put(key, value, putOption);

        localRegisterNodeKeySet.add(registryKey);
    }

    @Override
    public void unRegister(ServiceMetaInfo serviceMetaInfo) {
        String registryKey = ETCD_ROOT_PATH + serviceMetaInfo.getServiceNodeKey();
        kvClient.delete(ByteSequence.from(registryKey, StandardCharsets.UTF_8));
        localRegisterNodeKeySet.remove(registryKey);
    }

    @Override
    public List<ServiceMetaInfo> serviceDiscovery(String serviceKey) {

        List<ServiceMetaInfo> cachedServiceMetaInfoList = registryServiceCache.readCache();
        if (cachedServiceMetaInfoList != null)
            return cachedServiceMetaInfoList;
        String searchPrefix = ETCD_ROOT_PATH + serviceKey + '/';
        try {
            GetOption getOption = GetOption.builder()
                    .isPrefix(true)
                    .build();
            List<KeyValue> kvs = kvClient.get(
                            ByteSequence.from(searchPrefix, StandardCharsets.UTF_8),
                            getOption
                    ).get()
                    .getKvs();
            List<ServiceMetaInfo> serviceMetaInfoList = kvs.stream()
                    .map(kv -> {
                        String key = kv.getKey().toString(StandardCharsets.UTF_8);
                        watch(key);
                        String value = kv.getValue().toString(StandardCharsets.UTF_8);
                        return JSONUtil.toBean(value, ServiceMetaInfo.class);
                    }).collect(Collectors.toList());

            registryServiceCache.writeCache(serviceMetaInfoList);
            return serviceMetaInfoList;
        } catch (Exception e) {
            throw new RuntimeException("获取服务列表失败", e);
        }
    }

    @Override
    public void destroy() {
        for (String key: localRegisterNodeKeySet) {
            try {
                kvClient.delete(ByteSequence.from(key, StandardCharsets.UTF_8));
            } catch (Exception e) {
                throw new RuntimeException(key + "节点下线失败");
            }
        }
        if (kvClient != null)
            kvClient.close();
        if (client != null)
            client.close();
    }

    @Override
    public void heartBeat() {
        CronUtil.schedule("*/10 * * * * *", new Task() {
            @Override
            public void execute() {
                for (String key : localRegisterNodeKeySet) {
                    try {
                        List<KeyValue> kvs = kvClient.get(ByteSequence.from(key, StandardCharsets.UTF_8))
                                .get()
                                .getKvs();
                        if (CollUtil.isEmpty(kvs)) {
                            continue;
                        }
                        KeyValue keyValue = kvs.get(0);
                        String value = keyValue.getValue().toString(StandardCharsets.UTF_8);
                        ServiceMetaInfo bean = JSONUtil.toBean(value, ServiceMetaInfo.class);
                        register(bean);
                    } catch (Exception e) {
                        throw new RuntimeException("续签失败", e);
                    }
                }
            }
        });
        CronUtil.setMatchSecond(true);
        CronUtil.start();
    }
    private final Set<String> watchingKeySet = new ConcurrentHashSet<>();
    @Override
    public void watch(String serviceNodeKey) {
        Watch watchClient = client.getWatchClient();
        boolean newWatch = watchingKeySet.add(serviceNodeKey);
        if (newWatch) {
            watchClient.watch(ByteSequence.from(serviceNodeKey, StandardCharsets.UTF_8), response -> {
                for (WatchEvent event: response.getEvents()) {
                    switch (event.getEventType()) {
                        case DELETE:
                            registryServiceCache.clearCache();
                            break;
                        case PUT:
                        default:
                            break;
                    }
                }
            });
        }
    }
}
