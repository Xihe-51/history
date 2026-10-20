package com.ailab.back.config;

import com.ailab.back.vo.ResultVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.servlet.NoHandlerFoundException;

/**
 * 全局异常处理：把异常转换为接口文档约定的 {code,msg,data} 结构，
 * 避免前端收到 Spring 默认的 400/500 错误页而无法解析。
 *
 * <p>HTTP 状态码统一返回 200（业务码放在 code 字段），
 * 只有鉴权失败(401/403)由 JwtAuthFilter 直接返回。
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** 缺少必填参数，例如 /historical-event/page 未传 dynastyId */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResultVO<Void> handleMissingParam(MissingServletRequestParameterException e) {
        return ResultVO.error("缺少必填参数：" + e.getParameterName());
    }

    /** 参数类型不匹配，例如 dynastyId=abc */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResultVO<Void> handleTypeMismatch(MethodArgumentTypeMismatchException e) {
        return ResultVO.error("参数格式错误：" + e.getName());
    }

    /** 请求体不是合法 JSON */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResultVO<Void> handleUnreadable(HttpMessageNotReadableException e) {
        return ResultVO.error("请求体格式错误，请检查 JSON 内容");
    }

    /** 上传文件超过大小限制 */
    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public ResultVO<Void> handleMaxUpload(MaxUploadSizeExceededException e) {
        return ResultVO.error("上传文件过大，请压缩后重试（单个文件上限 100MB）");
    }

    /** 请求方法不支持 */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    public ResultVO<Void> handleMethodNotSupported(HttpRequestMethodNotSupportedException e) {
        return ResultVO.error("不支持的请求方法：" + e.getMethod());
    }

    /** 接口不存在 */
    @ExceptionHandler(NoHandlerFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ResultVO<Void> handleNotFound(NoHandlerFoundException e) {
        return ResultVO.error("接口不存在：" + e.getRequestURL());
    }

    /**
     * 数据库约束冲突：常见于外键约束、唯一索引冲突。
     * 返回可读提示，而不是把 SQL 细节暴露给前端。
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResultVO<Void> handleDataIntegrity(DataIntegrityViolationException e) {
        log.warn("数据库约束冲突: {}", e.getMostSpecificCause().getMessage());
        String cause = String.valueOf(e.getMostSpecificCause().getMessage());
        if (cause.contains("Duplicate entry")) {
            return ResultVO.error("数据重复，已存在相同的记录");
        }
        if (cause.contains("foreign key constraint") || cause.contains("Cannot add or update a child row")) {
            return ResultVO.error("关联数据不存在，请检查所属朝代/阶段等外键是否正确");
        }
        if (cause.contains("Cannot delete or update a parent row")) {
            return ResultVO.error("该记录仍被其他数据引用，无法删除");
        }
        return ResultVO.error("数据保存失败，请检查数据是否合法");
    }

    /** 业务层主动抛出的参数/状态错误 */
    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResultVO<Void> handleIllegalArgument(RuntimeException e) {
        return ResultVO.error(e.getMessage() == null ? "请求参数不合法" : e.getMessage());
    }

    /** 兜底：记录完整堆栈，但只向前端返回简洁信息 */
    @ExceptionHandler(Exception.class)
    public ResultVO<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return ResultVO.error("系统繁忙，请稍后重试");
    }
}
