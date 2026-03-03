package com.coldchain.guardian.app.exception;

import com.coldchain.guardian.common.exception.BusinessException;
import com.coldchain.guardian.common.exception.ErrorCode;

/**
 * 告警相关业务异常类
 */
public class AlertException extends BusinessException {

    public AlertException(ErrorCode errorCode) {
        super(errorCode);
    }

    public AlertException(ErrorCode errorCode, String message) {
        super(errorCode, message);
    }

    public AlertException(ErrorCode errorCode, String message, Throwable cause) {
        super(errorCode, message, cause);
    }
}