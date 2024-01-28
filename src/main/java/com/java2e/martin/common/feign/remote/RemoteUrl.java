package com.java2e.martin.common.feign.remote;

import cn.hutool.core.convert.Convert;
import cn.hutool.core.util.ClassUtil;
import com.java2e.martin.common.feign.MartinFeignAutoConfiguration;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.web.bind.annotation.RequestMapping;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author 狮少
 * @version 1.0
 * @date 2019/9/3
 * @describtion RemoteUrl 收集所有接口中的 url
 * @since 1.0
 */
@Slf4j
public class RemoteUrl {
    public static Set<String> REMOTE_URLS;

    static {
        REMOTE_URLS = collectFeignUrls();
    }

    /**
     * 统计所有feignClient中的url
     */
    public static Set<String> collectFeignUrls() {
        EnableFeignClients enableFeignClients = AnnotationUtils.getAnnotation(MartinFeignAutoConfiguration.class, EnableFeignClients.class);
        String[] basePackages = enableFeignClients.basePackages();
        int length = basePackages.length;
        Set<String> allUrls = new HashSet<>();
        for (int i = 0; i < length; i++) {
            String basePackage = basePackages[i];
            Set<Class<?>> classSet = ClassUtil.scanPackage(basePackage);
            for (Class cl : classSet) {
                Method[] methods = cl.getMethods();
                for (Method method : methods) {
                    RequestMapping mergedAnnotation = AnnotatedElementUtils.findMergedAnnotation(method, RequestMapping.class);
                    if (null != mergedAnnotation) {
                        String[] value = mergedAnnotation.value();
                        pathVariableMapMethodArgumentResolver(value, allUrls);
                    }
                }
            }
        }
        return allUrls;
    }

    /**
     * 将请求中的{}，转化成通配符*
     *
     * @param value   转化之前
     * @param allUrls 转化之后
     * @return
     */
    private static String[] pathVariableMapMethodArgumentResolver(String[] value, Set<String> allUrls) {
        ArrayList<String> result = new ArrayList<>();
        Function<String, String> function1 = s -> {
            return s.contains("{") ? "*" : s;
        };
        Arrays.asList(value).forEach(x -> {
            log.debug("FeignClient Url,{}", Convert.toStr(x));
            String collect = Arrays.stream(x.split("/")).map(function1).collect(Collectors.joining("/"));
            log.debug("Oauth2PermitAll Url，{}", collect);
            allUrls.add(collect);
        });
        return result.stream().toArray(String[]::new);
    }

}
