/*
* 腾讯数据
 */
package com.tencent.core.exceptions;

import java.io.Serializable;

public class TencentException extends BaseException implements Serializable {
    public TencentException(BaseException.BaseExceptionEnum exceptionMessage) {
        super(exceptionMessage);
    }

    public TencentException(BaseException.BaseExceptionEnum exceptionMessage, Object... params) {
        super(exceptionMessage, params);
    }

    public TencentException(String message) {
        super(message);
    }

    public TencentException(String message, Throwable cause) {
        super(message, cause);
    }
}
