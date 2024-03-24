package com.mqttsnet.thinglinks.common.core.exception;

import cn.hutool.core.util.StrUtil;
import com.mqttsnet.thinglinks.common.core.utils.StrPool;
import lombok.Getter;

/**
 * 非运行期异常基类，所有自定义非运行时异常继承该类
 *
 * @author mqttsnet
 * @version 1.0
 * @see RuntimeException
 */
public class BaseUncheckedException extends Exception {

    private static final long serialVersionUID = -778887391066124051L;

    /**
     * 异常信息
     */
    private String message;

    /**
     * 具体异常码
     */
    @Getter
    private int code;

    public BaseUncheckedException(Throwable cause) {
        super(cause);
    }

    public BaseUncheckedException(final int code, Throwable cause) {
        super(cause);
        this.code = code;
    }


    public BaseUncheckedException(final int code, final String message) {
        super(message);
        this.code = code;
        this.message = message;
    }

    public BaseUncheckedException(final int code, final String message, Throwable cause) {
        super(cause);
        this.code = code;
        this.message = message;
    }

    public BaseUncheckedException(final int code, final String format, Object... args) {
        super(StrUtil.contains(format, StrPool.BRACE) ? StrUtil.format(format, args) : String.format(format, args));
        this.code = code;
        this.message = StrUtil.contains(format, StrPool.BRACE) ? StrUtil.format(format, args) : String.format(format, args);
    }


    @Override
    public String getMessage() {
        return message;
    }

}
