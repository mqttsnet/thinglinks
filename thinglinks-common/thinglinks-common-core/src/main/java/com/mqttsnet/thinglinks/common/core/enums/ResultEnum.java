package com.mqttsnet.thinglinks.common.core.enums;

/**
 * @EnumDescription: 结果码(状态码,异常码)统一注册类  (定型类,提供habit内部工具报错),异常码范围：[00000000-20000000)
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
		PRODUCT_DELETE_OFRULE__EXCEPTION(500, "有规则引擎正在使用该设备模型，无法删除！"),
		PRODUCT_DELETE_EXCEPTION(500, "有设备正在使用该设备模型，无法删除！"),
		RULE_ENGINE_NAME_TOO_LONG(500, "规则引擎名称长度错误，请输入总长4至20字符之间的名称"),
		//业务异常
		BUSINESS_EXCEPTION(500, "业务异常"),
		//华为删除数据失败
		HWDeleteERROR_EXCEPTION(500, "删除数据失败,请联系管理员"),
		//其他异常(数据库连接异常，sql异常等其他框架的异常等)
		OTHER_EXCEPTION(00000003, "其他异常"),
		//错误(非异常 not Exception jvm抛出)
		ERROR(501, "系统异常,请联系后台管理员查看日志"),
	
		//产品名称重复
		PRODCUTNAMECF_EXCEPTION(500, "产品名称重复"),
		ZZNAMECF_EXCEPTION(500, "主站名称重复"),
		//产品topic名称重复
		PRODCUTTOPICNAMECF_EXCEPTION(500, "产品topic名称重复"),
		PARAM_ERROR(00000001, "入参校验错误"),
		EMPTY(99999999, "没有数据"),
		SUCCESS(200, "SUCCESS"),
		PARAM_MISS(10000002, "缺少参数"),
	    UNIQUE_KEY(10000001, "主键约束错误"),
	    PARAM_CONVERT(10000003, "参数转换异常"),
	    PARSE_YMD_TO_DATE(10000004, "yyyymmdd字符串转日期异常"),
	    PARAM_CHECK_VERIFY(10000099,"参数校验不合法"),
	    PARAM_VERIFY_FAIL(10000005, "参数校验失败"),
		PARAMETER_IS_NOT_NULL(500,"传入参数不能为空"),
		PRODUCT_IS_NULL(500,"没有此产品"),
		DONOT_USE_HUEWEI(500,"没有调用华为接口"),
		DEVICE_HAS_ONLINE(500,"删除设备中存在在线端设备或边设备!"),
		DEVICE_HAS_RULE(500,"所选边设备或端设备在规则引擎中使用!"),
		DEVICE_SUBSCRIBE_TOPIC_IS_NULL(500,"订阅的设备topic为空！"),
		USE_HUAWEI_INTERFACE_ERROR(500,"新增北向应用报错，请联系管理人员！"),
		CAN_NOT_ISSUE_DISCONNECT_DEVICE(500,"不能下发未连接设备！"),
		DEVICE_IS_REPEAT(500,"存在重复设备！"),
		DEVICE_HAS_SUBDEVICE(500,"存在子设备！"),
		PRODUCT_IS_NOU_EXIST(500,"不存在此产品！"),
		PRODUCT_IS_NOU_EXIST2(500,"普通产品不存在！"),
		PRODUCT_IS_NOU_EXIST3(500,"网关产品不存在！"),
		CAN_NOT_STOP_ONLINEDEVICE(500,"不能启用在线设备！"),
		CAN_NOT_DELETE_ONLINEDEVICE(500,"不能删除在线设备！"),
		CAN_NOT_STOP_ONLINEDEVICE2(500,"不能停用在线设备！"),
		PLEASE_CONNECT_ADMINISTRATOR(500,"新增设备报错，请联系管理人员！"),
		PLEASE_CONNECT_ADMINISTRATOR2(500,"修改设备报错，请联系管理人员！"),
		PLEASE_CONNECT_ADMINISTRATOR4(500,"删除设备报错，请联系管理人员！"),
		PLEASE_CONNECT_ADMINISTRATOR3(500,"该北向应用有关联的消息主题，不能删除！"),
		PLEASE_CONNECT_ADMINISTRATOR5(500,"重置密码报错，请联系管理人员！"),
		PLEASE_CONNECT_ADMINISTRATOR6(500,"请填写正确的分组信息！"), 
		PLEASE_CONNECT_ADMINISTRATOR8(500,"修改设备运行状态报错，请联系管理人员！"),
		PLEASE_CONNECT_ADMINISTRATOR9(500,"查询告警信息失败，请联系管理人员！"),
		PLEASE_WRITE_FIELD(500,"请填写正确的字段信息！"),
		PLEASE_WRITE_RIGHT_AREA(500,"请填写正确的地区！"),
		PLEASE_WRITE_RIGHT_ALLAREA(500, "请将填写的地区精确到区县级！"),
		PLEASE_WRITE_RIGHT_DEVICE_NAME(500, "请填写正确的设备名称"),
		PLEASE_WRITE_ORGANIZATION(500,"请填写正确的组织！"),
		PLEASE_CONNECT_ADMINISTRATOR7(500,"请填写正确的位置信息"),
		DEVICE_HAS_CHILDREN(500,"所选设备为汇聚节点，请重新选择"),
		DEVICE_ADD_CHILDREN(500,"汇聚节点三级设备添加报错，请联系管理人员！"),
		DEVICE_RM_CHILDREN(500,"汇聚节点三级设备移除报错，请联系管理人员！"),
		DONT_HAVE_DATA(500,"所选区间没有数据"),
		CANT_CHANG_RUN_STATE(500,"未连接设备不能修改运行状态"),
		PASSWORD_CHECK_FAIL(500,"密码校验失败"),
		USERNAME_CHECK_FAIL(500,"用户名校验失败"),
		CLIENTID_CHECK_FAIL(500,"clientId校验失败"),
		COURT_NAME_NO_NULL(500,"位置名称不能为空"),
		COURT_NAME_SHORT_NO_NULL(500,"位置简称不能为空"),
		AFFILIATED_UNITS_NO_NULL(500,"所属区域不能为空"),
		COURT_EXISTENCE(500,"位置编码已存在"),
		ORGANIZATION_NAME_NO_NULL(500,"所属单位不能为空"),
		PROFESSION_NO_NULL(500,"专业不能为空"),
	    PROAPPID_NOT_SAME(500,"应用不一致")
	    ;
	      
	    public int code;  
	    public String message;  
	      
	    private ResultEnum(int code, String message){  
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
