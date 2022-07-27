package com.mqttsnet.thinglinks.common.redis.service;

import lombok.Data;

/**
 * @author thinglinks
 * redis分布式锁返回结果
 */
@Data
public class RedisLockRunResult<R> {
    /**
     * 是否获取到锁
     */
    private boolean lockSuccess = false;
    /**
     * 返回值
     */
    private R res;

    /**
     * 执行成功的返回值
     * @param r 执行结果
     * @param <R> 执行结果泛型
     * @return
     */
    public static <R> RedisLockRunResult<R> buildSuccess(R r) {
        RedisLockRunResult<R> res = new RedisLockRunResult<>();
        res.setLockSuccess(true);
        res.setRes(r);
        return res;
    }

    /**
     * 未获取到锁的返回值
     * @param <R> 执行结果泛型
     * @return 执行结果为null,未获取到锁
     */
    public static <R> RedisLockRunResult<R> buildGetLockErr() {
        RedisLockRunResult<R> res = new RedisLockRunResult<>();
        res.setLockSuccess(false);
        return res;
    }
}
