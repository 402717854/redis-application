package com.redis.application.exception;

import com.redis.application.constant.GlobalExceptionEnum;
import lombok.Data;

import java.text.MessageFormat;

/**
 * @Classname GlobalException
 * @Description 全局异常处理
 * @Date 2020/12/9 10:29
 * @Created by 20113370
 */
@Data
public class OperationException extends RuntimeException {

    private String errorCode;

    public OperationException(String message) {
        super(message);
    }

    public OperationException(String errorCode, String message) {
        super(message);
        this.setErrorCode(errorCode);
    }

    public OperationException(GlobalExceptionEnum globalExceptionEnum) {
        super(globalExceptionEnum.getMessage());
        this.setErrorCode(globalExceptionEnum.getCode());
    }

    public OperationException(GlobalExceptionEnum globalExceptionEnum , Object... obj) {
        super(MessageFormat.format(globalExceptionEnum.getMessage(),obj));
        this.setErrorCode(globalExceptionEnum.getCode());
    }
}
