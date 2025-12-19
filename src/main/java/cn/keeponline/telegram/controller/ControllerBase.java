package cn.keeponline.telegram.controller;

import cn.keeponline.telegram.exception.BizzRuntimeException;
import cn.keeponline.telegram.response.Response;
import cn.keeponline.telegram.response.ResponseEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

@Slf4j
public class ControllerBase {

    /**
     * 参数校验统一异常处理
     */
    @ExceptionHandler(BindException.class)
    @ResponseBody
    public Response BindException(BindException ex) {
        FieldError fieldError = ex.getBindingResult().getFieldError();
        log.warn("参数校验异常:{}({})", fieldError.getDefaultMessage(), fieldError.getField());
        return Response.fail(ResponseEnum.RESULT_ERROR.getCode(), fieldError.getDefaultMessage());
    }

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Response handleException(Exception e) {
        log.error(e.getMessage(), e);
        if (e instanceof BizzRuntimeException) {
            BizzRuntimeException bizz = (BizzRuntimeException) e;
            return Response.fail(bizz.getCode(), bizz.getMessage());
        }
        return Response.fail(ResponseEnum.RESULT_ERROR.getCode(), ResponseEnum.RESULT_ERROR.getMessage());
    }

}
