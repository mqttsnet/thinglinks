package com.mqttsnet.thinglinks.common.core.exception.file;

import com.mqttsnet.thinglinks.common.core.exception.base.BaseException;

/**
 * 文件信息异常类
 * 
 * @author thinglinks
 */
public class FileException extends BaseException
{
    private static final long serialVersionUID = 1L;

    public FileException(String code, Object[] args)
    {
        super("file", code, args, null);
    }

}
