package com.imooc.mall.vo;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.imooc.mall.enums.ResponseEnum;
import lombok.Data;
import org.springframework.validation.BindingResult;

/**
 * @author 小浣浣
 * @verson 1.0
 * @time 2025/3/22 21:12
 */
@Data
@JsonInclude(value = JsonInclude.Include.NON_NULL)//把返回给前端为空的字段去掉
public class ResponseVo<T> {
    private Integer status;
    private String msg;
    private T Data;


    public ResponseVo(Integer status, String msg) {
        this.status = status;
        this.msg = msg;
    }

    private ResponseVo(Integer status, T data) {
        this.status = status;
        Data = data;
    }

    public static <T>ResponseVo<T> success(String msg){
        return new ResponseVo<>(ResponseEnum.SUCCESS.getCode(),msg);
    }
    public static <T>ResponseVo<T> success(){
        return new ResponseVo<>(ResponseEnum.SUCCESS.getCode(),ResponseEnum.SUCCESS.getDesc());
    }
    public static <T> ResponseVo<T> success(String msg,T data) {
        return new ResponseVo<>(ResponseEnum.SUCCESS.getCode(), data);
    }
    public static <T> ResponseVo<T> success(T data) {
        return new ResponseVo<>(ResponseEnum.SUCCESS.getCode(), data);
    }
    public static <T>ResponseVo<T> error(ResponseEnum responseEnum){
        return new ResponseVo<>(responseEnum.getCode(), responseEnum.getDesc());
    }
    public static <T>ResponseVo<T> error(ResponseEnum responseEnum,String msg){
        return new ResponseVo<>(responseEnum.getCode(), responseEnum.getDesc());
    }

    public static <T>ResponseVo<T> error(ResponseEnum responseEnum, BindingResult bindingResult){
        return new ResponseVo<>(responseEnum.PARAM_ERROR.getCode(),
                bindingResult.getFieldError().getField()+" "+
                bindingResult.getFieldError().getDefaultMessage() );
    }
}
