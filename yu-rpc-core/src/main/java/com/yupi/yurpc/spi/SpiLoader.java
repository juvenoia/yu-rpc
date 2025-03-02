package com.yupi.yurpc.spi;

import cn.hutool.core.io.resource.ResourceUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
public class SpiLoader {
    private static Map<String, Map<String, Class<?>>> loaderMap = new ConcurrentHashMap<>();
    private static Map<String, Object> instanceCache = new ConcurrentHashMap<>();

    private static final String RPC_SYSTEM_SPI_DIR = "META-INF/rpc/system/";
    private static final String RPC_CUSTOM_SPI_DIR = "META-INF/rpc/custom/";

    private static final String[] SCAN_DIRS = new String[] {RPC_SYSTEM_SPI_DIR, RPC_CUSTOM_SPI_DIR};

    public static <T> T getInstance(Class<?> tClass, String key) {
        String tClassName = tClass.getName();
        Map<String, Class<?>> keyClassMap = loaderMap.get(tClassName);

        if (keyClassMap == null) {
            throw new RuntimeException(String.format("com.yupi.yurpc.spi.SpiLoader 未加载 %s 类型", tClassName));
        }
        if (!keyClassMap.containsKey(key)) {
            throw new RuntimeException(String.format("com.yupi.yurpc.spi.SpiLoader 的 %s 不存在 %s 的key", tClassName, key));
        }

        Class<?> implClass = keyClassMap.get(key);
        String implClassName = implClass.getName();

        if (!instanceCache.containsKey(implClassName)) {
            try {
                instanceCache.put(implClassName, implClass.newInstance());
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        return (T) instanceCache.get(implClassName);
    }


    public static Map<String, Class<?>> load(Class<?> loadClass) {
        log.info("加载类型为 {} 的SPI", loadClass.getName());
        Map<String, Class<?>> keyClassMap = new HashMap<>();

        for (String scanDir : SCAN_DIRS) {
            List<URL> resources = ResourceUtil.getResources(scanDir + loadClass.getName());
            for (URL resource : resources) {
                try {
                    InputStreamReader inputStreamReader = new InputStreamReader(resource.openStream());
                    BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                    String line;
                    while ((line = bufferedReader.readLine()) != null) {
                        String[] split = line.split("=");
                        if (split.length > 1) {
                            String key = split[0];
                            String className = split[1];
                            System.out.println(key);
                            System.out.println(className);
                            keyClassMap.put(key, Class.forName(className));
                        }
                    }
                } catch (Exception e) {
                    log.error("spi resource load error");
                }
            }
            loaderMap.put(loadClass.getName(), keyClassMap);
        }
        return keyClassMap;
    }
}
