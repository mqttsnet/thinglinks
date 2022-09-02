package com.mqttsnet.thinglinks.common.core.enums;

/**
 * @EnumDescription: 结果码(状态码,异常码)统一注册类  (定型类,提供habit内部工具报错),异常码范围：[10000-20000)
 * @EnumName: ResultEnum
 * @Author: thinglinks
 * @Date: 2021-12-27 16:32:53
 */
public enum ResultEnum {

		/**
		 * #eclipse 快捷键:小写转大写
		 *  ctrl+shift+x
		 * #避免重复
		 *按从小到大顺序,定义好及时提交，
		 * #抛异常代码范例
		 *  throw new BusinessException(ResultEnum.PARAM_CONVERT);
		 */
		//
		SUCCESS(200,"SUCCESS"),
		FAIL(500, "业务异常"),
		PARAM_ERROR( 10000, "参数不正确"),
		SERVICE_ERROR(10001, "服务异常"),
		DATA_ERROR(10002, "数据异常"),
		DATA_UPDATE_ERROR(100003, "数据版本异常"),
		URL_ENCODE_ERROR( 100004, "URL编码失败"),
		ILLEGAL_CALLBACK_REQUEST_ERROR( 10005, "非法回调请求"),
		PARAM_MISS(10006, "缺少参数"),
	    UNIQUE_KEY(10007, "主键约束错误"),
	    PARAM_CONVERT(10008, "参数转换异常"),
	    PARSE_YMD_TO_DATE(10009, "yyyymmdd字符串转日期异常"),
	    PARAM_CHECK_VERIFY(10010,"参数校验不合法"),
	    PARAM_VERIFY_FAIL(100011, "参数校验失败"),
		PARAMETER_IS_NOT_NULL(10012,"传入参数不能为空")
	    ;
	      
	    public Integer code;
	    public String message;  
	      
	    private ResultEnum(Integer code, String message){
	        this.code = code;  
	        this.message = message;  
	    }  

	    public int getCode() {  
	        return code;  
	    }  
	  
	    public String getMessage() {  
	        return message;  
	    }  
	    
	}
