package com.mqttsnet.thinglinks.common.core.utils.bean;

import cn.hutool.core.bean.BeanUtil;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Bean增强类工具类
 *
 * <p>
 * 把一个拥有对属性进行set和get方法的类，我们就可以称之为JavaBean。
 * </p>
 *
 * @author mqttsnet
 * @since 3.1.2
 */
public class BeanPlusUtil extends BeanUtil {

    /**
     * 转换 list
     *
     * @param sourceList       源集合
     * @param destinationClass 目标类型
     * @return 目标集合
     */
    public static <T, E> List<T> toBeanList(Collection<E> sourceList, Class<T> destinationClass) {
        if (sourceList == null || sourceList.isEmpty() || destinationClass == null) {
            return Collections.emptyList();
        }
        return sourceList.parallelStream()
                .filter(Objects::nonNull)
                .map(source -> toBean(source, destinationClass))
                .collect(Collectors.toList());
    }

}
