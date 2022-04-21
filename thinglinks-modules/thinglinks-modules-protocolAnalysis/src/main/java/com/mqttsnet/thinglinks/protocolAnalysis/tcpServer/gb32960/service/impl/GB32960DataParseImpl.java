package com.mqttsnet.thinglinks.protocolAnalysis.tcpServer.gb32960.service.impl;

import com.alibaba.fastjson.JSON;
import com.mqttsnet.thinglinks.common.core.utils.HexUtils;
import com.mqttsnet.thinglinks.common.core.utils.SubStringUtil;
import com.mqttsnet.thinglinks.common.redis.service.RedisService;
import com.mqttsnet.thinglinks.protocolAnalysis.tcpServer.gb32960.entity.GB32960MessageData;
import com.mqttsnet.thinglinks.protocolAnalysis.tcpServer.gb32960.entity.dao.*;
import com.mqttsnet.thinglinks.protocolAnalysis.tcpServer.gb32960.service.GB32960DataParseService;
import io.netty.channel.ChannelHandlerContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @Description: gb32960实时数据解析实现
 * @Author: ShiHuan Sun
 * @E-mail: 13733918655@163.com
 * @Website: http://mqtts.net
 * @CreateDate: 2021/11/15$ 18:19$
 * @UpdateUser: ShiHuan Sun
 * @UpdateDate: 2021/11/15$ 18:19$
 * @UpdateRemark: 修改内容
 * @Version: 1.0
 */
@Slf4j
@Service
@RefreshScope
public class GB32960DataParseImpl implements GB32960DataParseService {

    @Autowired
    private RedisService redisService;

    /**
     * 实时数据解析并返回数据
     * @param readDatas
     */
    @Override
    public String realTimeDataParseAndPushData(ChannelHandlerContext ctx, String readDatas) throws Exception{
        //心跳
        if ("07".equals(SubStringUtil.subStr(readDatas,4,6))){
            GB32960MessageData gb32960MessageData = new GB32960MessageData();
            gb32960MessageData.setMsgHead(SubStringUtil.subStrStart(readDatas,4));
            gb32960MessageData.setMsgCommand(SubStringUtil.subStr(readDatas,4,6));
            gb32960MessageData.setMsgResponse(SubStringUtil.subStr(readDatas,6,8));
            gb32960MessageData.setUniqueIdentifier(SubStringUtil.subStr(readDatas,8,42));
            gb32960MessageData.setEncryption(SubStringUtil.subStr(readDatas,42,44));
            gb32960MessageData.setDataCellLength(SubStringUtil.subStr(readDatas,44,48));
//            gb32960MessageData.setData(SubStringUtil.subStr(readDatas,48,-2));
            gb32960MessageData.setCheckCode(SubStringUtil.subStrEnd(readDatas,2));
            log.info("GB32960心跳数据处理---->");
            //响应标识
            gb32960MessageData.setMsgResponse("01");
            //数据单元长度
            gb32960MessageData.setDataCellLength("0000");
            //校验码计算
            StringBuffer checkCodeData=new StringBuffer().append(gb32960MessageData.getMsgCommand()).append(gb32960MessageData.getMsgResponse())
                    .append(gb32960MessageData.getUniqueIdentifier()).append(gb32960MessageData.getEncryption()).append(gb32960MessageData.getDataCellLength());
            gb32960MessageData.setCheckCode(HexUtils.getBCC(String.valueOf(checkCodeData)));
            StringBuffer pushData=new StringBuffer().append(gb32960MessageData.getMsgHead()).append(gb32960MessageData.getMsgCommand()).append(gb32960MessageData.getMsgResponse())
                    .append(gb32960MessageData.getUniqueIdentifier()).append(gb32960MessageData.getEncryption()).append(gb32960MessageData.getDataCellLength()).append(gb32960MessageData.getCheckCode());
            return String.valueOf(pushData);
        }
        //终端校时
        if ("08".equals(SubStringUtil.subStr(readDatas,4,6))){
            GB32960MessageData gb32960MessageData = new GB32960MessageData();
            gb32960MessageData.setMsgHead(SubStringUtil.subStrStart(readDatas,4));
            gb32960MessageData.setMsgCommand(SubStringUtil.subStr(readDatas,4,6));
            gb32960MessageData.setMsgResponse(SubStringUtil.subStr(readDatas,6,8));
            gb32960MessageData.setUniqueIdentifier(SubStringUtil.subStr(readDatas,8,42));
            gb32960MessageData.setEncryption(SubStringUtil.subStr(readDatas,42,44));
            gb32960MessageData.setDataCellLength(SubStringUtil.subStr(readDatas,44,48));
//            gb32960MessageData.setData(SubStringUtil.subStr(readDatas,48,-2));
            gb32960MessageData.setCheckCode(SubStringUtil.subStrEnd(readDatas,2));
            log.info("GB32960终端校时数据处理---->");
            //响应标识
            gb32960MessageData.setMsgResponse("01");
            //数据单元长度
            gb32960MessageData.setDataCellLength("0000");
            //校验码计算
            StringBuffer checkCodeData=new StringBuffer().append(gb32960MessageData.getMsgCommand()).append(gb32960MessageData.getMsgResponse())
                    .append(gb32960MessageData.getUniqueIdentifier()).append(gb32960MessageData.getEncryption()).append(gb32960MessageData.getDataCellLength());
            gb32960MessageData.setCheckCode(HexUtils.getBCC(String.valueOf(checkCodeData)));
            StringBuffer pushData=new StringBuffer().append(gb32960MessageData.getMsgHead()).append(gb32960MessageData.getMsgCommand()).append(gb32960MessageData.getMsgResponse())
                    .append(gb32960MessageData.getUniqueIdentifier()).append(gb32960MessageData.getEncryption()).append(gb32960MessageData.getDataCellLength()).append(gb32960MessageData.getCheckCode());
            return String.valueOf(pushData);
        }
        //TODO 其他报文与数据单元B3.5.2一致 数据单元不为空，特殊处理
        GB32960MessageData gb32960MessageData = new GB32960MessageData();
        gb32960MessageData.setMsgHead(SubStringUtil.subStrStart(readDatas,4));
        gb32960MessageData.setMsgCommand(SubStringUtil.subStr(readDatas,4,6));
        gb32960MessageData.setMsgResponse(SubStringUtil.subStr(readDatas,6,8));
        gb32960MessageData.setUniqueIdentifier(SubStringUtil.subStr(readDatas,8,42));
        gb32960MessageData.setEncryption(SubStringUtil.subStr(readDatas,42,44));
        gb32960MessageData.setDataCellLength(SubStringUtil.subStr(readDatas,44,48));
        gb32960MessageData.setData(SubStringUtil.subStr(readDatas,48,-2));
        gb32960MessageData.setCheckCode(SubStringUtil.subStrEnd(readDatas,2));
        //原生数据单元报文
        String primaryData=SubStringUtil.subStr(readDatas,48,-2);
        //TODO 平台登录数据处理(目前没有走ThingLinks平台Link模块校验)
        if("05".equals(gb32960MessageData.getMsgCommand()) && "01".equals(gb32960MessageData.getEncryption())){
            log.info("GB32960平台登录数据处理---->");
            if (redisService.hasKey(ctx.channel().remoteAddress().toString())){
                String count = redisService.get(ctx.channel().remoteAddress().toString());
                if ("3".equals(count)){
                    redisService.deleteObject(ctx.channel().remoteAddress().toString());
                    log.info("GB32960平台登录次数上限，断开连接"+ctx.channel().remoteAddress().toString());
                    ctx.channel().close();
                }else {
                    count+=1;
                    redisService.set(ctx.channel().remoteAddress().toString(),count);
                }
            }else {
                redisService.set(ctx.channel().remoteAddress().toString(),"1");
            }
            //响应标识
            gb32960MessageData.setMsgResponse("01");
            //数据单元长度
            gb32960MessageData.setDataCellLength("0006");
            //数据单元
            String data = gb32960MessageData.getData();
            StringBuffer timestamp=new StringBuffer().append(SubStringUtil.subStrStart(data,12));
            //校验码计算
            StringBuffer checkCodeData=new StringBuffer().append(gb32960MessageData.getMsgCommand()).append(gb32960MessageData.getMsgResponse())
                    .append(gb32960MessageData.getUniqueIdentifier()).append(gb32960MessageData.getEncryption()).append(gb32960MessageData.getDataCellLength()).append(timestamp);
            gb32960MessageData.setCheckCode(HexUtils.getBCC(String.valueOf(checkCodeData)));
            StringBuffer pushData=new StringBuffer().append(gb32960MessageData.getMsgHead()).append(gb32960MessageData.getMsgCommand()).append(gb32960MessageData.getMsgResponse())
                    .append(gb32960MessageData.getUniqueIdentifier()).append(gb32960MessageData.getEncryption()).append(gb32960MessageData.getDataCellLength()).append(timestamp).append(gb32960MessageData.getCheckCode());
            return String.valueOf(pushData);
        }
        //车辆登录数据处理
        if("01".equals(gb32960MessageData.getMsgCommand()) && "01".equals(gb32960MessageData.getEncryption())){
            log.info("GB32960车辆登录数据处理----->");
            //响应标识
            gb32960MessageData.setMsgResponse("01");
            //数据单元长度
            gb32960MessageData.setDataCellLength("0006");
            //数据单元
            String data = gb32960MessageData.getData();
            StringBuffer timestamp=new StringBuffer().append(SubStringUtil.subStrStart(data,12));
            //校验码计算
            StringBuffer checkCodeData=new StringBuffer().append(gb32960MessageData.getMsgCommand()).append(gb32960MessageData.getMsgResponse())
                    .append(gb32960MessageData.getUniqueIdentifier()).append(gb32960MessageData.getEncryption()).append(gb32960MessageData.getDataCellLength()).append(timestamp);
            gb32960MessageData.setCheckCode(HexUtils.getBCC(String.valueOf(checkCodeData)));
            StringBuffer pushData=new StringBuffer().append(gb32960MessageData.getMsgHead()).append(gb32960MessageData.getMsgCommand()).append(gb32960MessageData.getMsgResponse())
                    .append(gb32960MessageData.getUniqueIdentifier()).append(gb32960MessageData.getEncryption()).append(gb32960MessageData.getDataCellLength()).append(timestamp).append(gb32960MessageData.getCheckCode());
            return String.valueOf(pushData);
        }
        //实时数据单元处理(不加密)
        if("02".equals(gb32960MessageData.getMsgCommand()) || "03".equals(gb32960MessageData.getMsgCommand()) && "01".equals(gb32960MessageData.getEncryption())){
            log.info("32960实时数据处理----->");
            GB32960BaseDTO dflzmBaseDTO = new GB32960BaseDTO();
            dflzmBaseDTO.setCommand("TERMINAL_VEHICLE_UPLOAD_REALTIME");
            dflzmBaseDTO.setVin(HexUtils.convertHexToString(gb32960MessageData.getUniqueIdentifier()));
            String data = gb32960MessageData.getData();
            //采集时间处理
            log.info("采集时间-年："+ HexUtils.hexStringToDecimal(SubStringUtil.subStrStart(data,2)));
            log.info("采集时间-月："+ HexUtils.hexStringToDecimal(SubStringUtil.subStr(data,2,4)));
            log.info("采集时间-日："+ HexUtils.hexStringToDecimal(SubStringUtil.subStr(data,4,6)));
            log.info("采集时间-小时："+ HexUtils.hexStringToDecimal(SubStringUtil.subStr(data,6,8)));
            log.info("采集时间-分："+ HexUtils.hexStringToDecimal(SubStringUtil.subStr(data,8,10)));
            log.info("采集时间-秒："+ HexUtils.hexStringToDecimal(SubStringUtil.subStr(data,10,12)));
            StringBuffer timestamp=new StringBuffer().append(HexUtils.hexStringToDecimal(SubStringUtil.subStrStart(data, 2))).append(HexUtils.hexStringToDecimal(SubStringUtil.subStr(data, 2, 4))).append(HexUtils.hexStringToDecimal(SubStringUtil.subStr(data,4,6)))
                            .append(HexUtils.hexStringToDecimal(SubStringUtil.subStr(data,6,8))).append(HexUtils.hexStringToDecimal(SubStringUtil.subStr(data,8,10))).append(HexUtils.hexStringToDecimal(SubStringUtil.subStr(data,10,12)));
            final String year = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));//转换为年月日格式
            String acquisitionTime = year + " "+HexUtils.hexStringToDecimal(SubStringUtil.subStr(data,6,8))+":"+HexUtils.hexStringToDecimal(SubStringUtil.subStr(data,8,10))+":"+HexUtils.hexStringToDecimal(SubStringUtil.subStr(data,10,12));
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            Date date = simpleDateFormat.parse(acquisitionTime);
            long ts = date.getTime();
            dflzmBaseDTO.setDataTime(ts);
            if ("01".equals(SubStringUtil.subStr(data,12,14))){
                //整车数据处理
                GB32960VehicleStatus vehicleStatus = new GB32960VehicleStatus();
                //车辆状态
                String vehicleStatusEngineStatus = SubStringUtil.subStr(data, 14, 16);
                switch (vehicleStatusEngineStatus){
                    case "01":
                        vehicleStatus.setEngineStatus("STARTED");
                        break;
                    case "02":
                        vehicleStatus.setEngineStatus("STOPPED");
                        break;
                    case "03":
                        vehicleStatus.setEngineStatus("OTHER");
                        break;
                    case "FE":
                        vehicleStatus.setEngineStatus("ERROR");
                        break;
                    case "FF":
                        vehicleStatus.setEngineStatus("INVALID");
                        break;
                    default:
                }
                //充电状态
                String vehicleStatusChargingStatus = SubStringUtil.subStr(data, 16, 18);
                switch (vehicleStatusChargingStatus){
                    case "01":
                        vehicleStatus.setChargingStatus("CHARGING_STOPPED");
                        break;
                    case "02":
                        vehicleStatus.setChargingStatus("CHARGING_DRIVING");
                        break;
                    case "03":
                        vehicleStatus.setChargingStatus("NO_CHARGING");
                        break;
                    case "04":
                        vehicleStatus.setChargingStatus("NO_CHARGING");
                        break;
                    case "FE":
                        vehicleStatus.setChargingStatus("ERROR");
                        break;
                    case "FF":
                        vehicleStatus.setChargingStatus("INVALID");
                        break;
                    default:
                }
                //运行模式
                String vehicleStatusRunningModel = SubStringUtil.subStr(data, 18, 20);
                switch (vehicleStatusRunningModel){
                    case "01":
                        vehicleStatus.setRunningModel("EV");
                        break;
                    case "02":
                        vehicleStatus.setRunningModel("PHEV");
                        break;
                    case "03":
                        vehicleStatus.setRunningModel("FV");
                        break;
                    case "FE":
                        vehicleStatus.setRunningModel("ERROR");
                        break;
                    case "FF":
                        vehicleStatus.setRunningModel("INVALID");
                        break;
                    default:
                }
                //车速
                Integer vehicleStatusSpeed = HexUtils.hexStringToDecimal(SubStringUtil.subStr(data, 20, 24));
                Double speed = new BigDecimal(vehicleStatusSpeed).divide(new BigDecimal("10")).setScale(1, BigDecimal.ROUND_UP).doubleValue();
                vehicleStatus.setSpeed(speed);
                //累计里程
                Integer vehicleStatusMileage =  HexUtils.hexStringToDecimal(SubStringUtil.subStr(data, 24, 32));
                Double mileage = new BigDecimal(vehicleStatusMileage).divide(new BigDecimal("10")).setScale(1, BigDecimal.ROUND_UP).doubleValue();
                vehicleStatus.setMileage(mileage);
                //总电压
                Integer vehicleStatusVoltage =  HexUtils.hexStringToDecimal(SubStringUtil.subStr(data, 32, 36));
                Double voltage = new BigDecimal(vehicleStatusVoltage).divide(new BigDecimal("10")).setScale(1, BigDecimal.ROUND_UP).doubleValue();
                vehicleStatus.setVoltage(voltage);
                //总电流
                Integer vehicleStatusCurrent =  HexUtils.hexStringToDecimal(SubStringUtil.subStr(data, 36, 40));
                Double current = new BigDecimal(vehicleStatusCurrent).subtract(new BigDecimal(1000)).divide(new BigDecimal("10")).setScale(1, BigDecimal.ROUND_UP).doubleValue();
                vehicleStatus.setCurrent(current);
                //SOC
                Integer vehicleStatusSoc =  HexUtils.hexStringToDecimal(SubStringUtil.subStr(data, 40, 42));
                switch (vehicleStatusSoc){
                    default:
                        vehicleStatus.setSoc(vehicleStatusSoc);
                        break;
                }
                //DC-DC状态
                String vehicleStatusDcStatus = SubStringUtil.subStr(data, 42, 44);
                switch (vehicleStatusDcStatus){
                    case "01":
                        vehicleStatus.setDcStatus("NORMAL");
                        break;
                    case "02":
                        vehicleStatus.setDcStatus("OFF");
                        break;
                    case "FE":
                        vehicleStatus.setDcStatus("ERROR");
                        break;
                    case "FF":
                        vehicleStatus.setDcStatus("INVALID");
                        break;
                    default:
                }
                //TODO 档位处理
                GB32960TransmissionStatus transmissionStatus = new GB32960TransmissionStatus();
                Integer vehicleStatusTransmissionStatus = HexUtils.hexStringToDecimal(SubStringUtil.subStr(data, 44, 46));
                switch (vehicleStatusTransmissionStatus){
                    default:
                        transmissionStatus.setGear(vehicleStatusTransmissionStatus);
                        break;
                }
                vehicleStatus.setTransmissionStatus(transmissionStatus);
                //绝缘电阻
                Integer vehicleStatusInsulationResistance = HexUtils.hexStringToDecimal(SubStringUtil.subStr(data, 46, 50));
                switch (vehicleStatusInsulationResistance){
                    default:
                        vehicleStatus.setInsulationResistance(vehicleStatusInsulationResistance);
                        break;
                }
                //预留位
                Integer yuliu = HexUtils.hexStringToDecimal(SubStringUtil.subStr(data, 50, 54));

                dflzmBaseDTO.setVehicleStatus(vehicleStatus);
                gb32960MessageData.setData(SubStringUtil.subStr(data,54,gb32960MessageData.getData().length()));
            }
            if("02".equals(SubStringUtil.subStrStart(gb32960MessageData.getData(), 2))){
                //TODO 驱动电机数据(目前只处理了单个驱动电机)
                GB32960DriveMotorStatus driveMotorStatus = new GB32960DriveMotorStatus();
                //驱动机个数
                Integer driveMotorStatusDriveMotorCount = HexUtils.hexStringToDecimal(SubStringUtil.subStr(gb32960MessageData.getData(), 2, 4));
                switch (driveMotorStatusDriveMotorCount){
                    default:
                        driveMotorStatus.setDriveMotorCount(driveMotorStatusDriveMotorCount);
                        break;
                }
                //每个驱动电机总成信息长度
                String driveMotorsLength = SubStringUtil.subStr(gb32960MessageData.getData(), 4, 28 * driveMotorStatusDriveMotorCount);
                //驱动机详情
                GB32960DriveMotors driveMotors = new GB32960DriveMotors();
                //驱动电机序号
                Integer driveMotorsSn = HexUtils.hexStringToDecimal(SubStringUtil.subStrStart(driveMotorsLength, 2));
                switch (driveMotorsSn){
                    default:
                        driveMotors.setSn(driveMotorsSn);
                        break;
                }
                //驱动电机序号
                String driveMotorsDriveMotorPower = SubStringUtil.subStr(driveMotorsLength, 2, 4);
                switch (driveMotorsDriveMotorPower){
                    case "01":
                        driveMotors.setDriveMotorPower("CONSUMING_POWER");
                        break;
                    case "02":
                        driveMotors.setDriveMotorPower("GENERATING_POWER");
                        break;
                    case "03":
                        driveMotors.setDriveMotorPower("CLOSED");
                        break;
                    case "04":
                        driveMotors.setDriveMotorPower("PREPARING");
                        break;
                    case "FE":
                        driveMotors.setDriveMotorPower("CLOSED");
                        break;
                    case "FF":
                        driveMotors.setDriveMotorPower("INVALID");
                        break;
                    default:
                }
                //驱动电机控制器温度
                Integer driveMotorsControllerTemperature = HexUtils.hexStringToDecimal(SubStringUtil.subStr(driveMotorsLength, 4,6));
                switch (driveMotorsControllerTemperature){
                    default:
                        driveMotors.setControllerTemperature(driveMotorsControllerTemperature-40);
                        break;
                }
                //驱动电机转速
                Integer driveMotorsDriveMotorSpeed = HexUtils.hexStringToDecimal(SubStringUtil.subStr(driveMotorsLength, 6,10));
                switch (driveMotorsDriveMotorSpeed){
                    default:
                        driveMotors.setDriveMotorSpeed(driveMotorsDriveMotorSpeed-20000);
                        break;
                }
                //驱动电机转矩
                Integer driveMotorsDriveMotorTorque = HexUtils.hexStringToDecimal(SubStringUtil.subStr(driveMotorsLength, 10,14));
                Double driveMotorTorque = new BigDecimal(driveMotorsDriveMotorTorque).subtract(new BigDecimal(20000)).divide(new BigDecimal("10")).setScale(1, BigDecimal.ROUND_UP).doubleValue();
                driveMotors.setDriveMotorTorque(driveMotorTorque);
                //驱动电机温度
                Integer driveMotorsDriveMotorTemperature = HexUtils.hexStringToDecimal(SubStringUtil.subStr(driveMotorsLength, 14,16));
                switch (driveMotorsDriveMotorTemperature){
                    default:
                        driveMotors.setDriveMotorTemperature(driveMotorsDriveMotorTemperature-40);
                        break;
                }
                //电机控制器输入电压
                Integer driveMotorsControllerInputVoltage = HexUtils.hexStringToDecimal(SubStringUtil.subStr(driveMotorsLength, 16,20));
                Double controllerInputVoltage = new BigDecimal(driveMotorsControllerInputVoltage).divide(new BigDecimal("10")).setScale(1, BigDecimal.ROUND_UP).doubleValue();
                driveMotors.setControllerInputVoltage(controllerInputVoltage);
                //电机控制器直流母线电流
                Integer driveMotorsDcBusCurrentOfController = HexUtils.hexStringToDecimal(SubStringUtil.subStr(driveMotorsLength, 20,24));
                Double dcBusCurrentOfController = new BigDecimal(driveMotorsDcBusCurrentOfController).subtract(new BigDecimal(1000)).divide(new BigDecimal("10")).setScale(1, BigDecimal.ROUND_UP).doubleValue();
                driveMotors.setDcBusCurrentOfController(dcBusCurrentOfController);
                //TODO 数据赋值未处理
                List<GB32960DriveMotors> list = new ArrayList<>();
                list.add(driveMotors);
                driveMotorStatus.setDriveMotors(list);
                dflzmBaseDTO.setDriveMotorStatus(driveMotorStatus);
                gb32960MessageData.setData(SubStringUtil.subStr(gb32960MessageData.getData(),driveMotorsLength.length()+4,gb32960MessageData.getData().length()));
            }
            if("05".equals(SubStringUtil.subStrStart(gb32960MessageData.getData(),2))){
                //TODO 车辆位置数据
                GB32960LocationStatus locationStatus = new GB32960LocationStatus();
                //定位状态
                GB32960LocateStatus locateStatus = new GB32960LocateStatus();
                locateStatus.setValidation("VALID");
                //0:北纬、1:南纬
                Integer locateStatusLatitudeType = HexUtils.hexStringToDecimal(SubStringUtil.subStr(gb32960MessageData.getData(), 2, 3));
                switch (locateStatusLatitudeType){
                    case 0:
                        locateStatus.setLatitudeType("NORTH");
                        break;
                    case 1:
                        locateStatus.setLatitudeType("SOUTH");
                        break;
                }
                //0:东经、1:西经
                Integer locateStatusLongitudeType = HexUtils.hexStringToDecimal(SubStringUtil.subStr(gb32960MessageData.getData(), 3, 4));
                switch (locateStatusLongitudeType){
                    case 0:
                        locateStatus.setLongitudeType("EAST");
                        break;
                    case 1:
                        locateStatus.setLongitudeType("WEST");
                        break;
                }
                locationStatus.setLocateStatus(locateStatus);
                //精度
                Integer locationStatusLongitude = HexUtils.hexStringToDecimal(SubStringUtil.subStr(gb32960MessageData.getData(), 4, 12));
                switch (locationStatusLongitude){
                    default:
                        BigDecimal b = new BigDecimal(locationStatusLongitude);
                        Double result = b.movePointLeft(6).doubleValue();
                        locationStatus.setLongitude(result);
                        break;
                }
                //精度
                Integer locationStatusLatitude = HexUtils.hexStringToDecimal(SubStringUtil.subStr(gb32960MessageData.getData(), 12, 20));
                switch (locationStatusLatitude){
                    default:
                        BigDecimal b = new BigDecimal(locationStatusLatitude);
                        Double result = b.movePointLeft(6).doubleValue();
                        locationStatus.setLatitude(result);
                        break;
                }
                dflzmBaseDTO.setLocationStatus(locationStatus);
                gb32960MessageData.setData(SubStringUtil.subStr(gb32960MessageData.getData(),20,gb32960MessageData.getData().length()));
            }
            if("06".equals(SubStringUtil.subStrStart(gb32960MessageData.getData(),2))){
                //TODO 极致数据处理
                GB32960ExtremeStatus extremeStatus = new GB32960ExtremeStatus();
                //最高电压电池子系统号
                Integer extremeStatusSubSystemIndexOfMaxVoltage = HexUtils.hexStringToDecimal(SubStringUtil.subStr(gb32960MessageData.getData(), 2, 4));
                switch (extremeStatusSubSystemIndexOfMaxVoltage){
                    default:
                        extremeStatus.setSubSystemIndexOfMaxVoltage(extremeStatusSubSystemIndexOfMaxVoltage);
                        break;
                }
                //最高电压电池单体代号
                Integer extremeStatusCellIndexOfMaxVoltage = HexUtils.hexStringToDecimal(SubStringUtil.subStr(gb32960MessageData.getData(), 4, 6));
                switch (extremeStatusCellIndexOfMaxVoltage){
                    default:
                        extremeStatus.setCellIndexOfMaxVoltage(extremeStatusCellIndexOfMaxVoltage);
                        break;
                }
                //电池单体电压最高值
                Integer extremeStatusCellMaxVoltage = HexUtils.hexStringToDecimal(SubStringUtil.subStr(gb32960MessageData.getData(), 6, 10));
                Double cellMaxVoltage = new BigDecimal(extremeStatusCellMaxVoltage).divide(new BigDecimal("1000")).setScale(3, BigDecimal.ROUND_UP).doubleValue();
                extremeStatus.setCellMaxVoltage(cellMaxVoltage);
                //最低电压电池子系统号
                Integer extremeStatusSubSystemIndexOfMinVoltage = HexUtils.hexStringToDecimal(SubStringUtil.subStr(gb32960MessageData.getData(), 10, 12));
                switch (extremeStatusSubSystemIndexOfMinVoltage){
                    default:
                        extremeStatus.setSubSystemIndexOfMinVoltage(extremeStatusSubSystemIndexOfMinVoltage);
                        break;
                }
                //最低电压电池单体代号
                Integer extremeStatusCellIndexOfMinVoltage = HexUtils.hexStringToDecimal(SubStringUtil.subStr(gb32960MessageData.getData(), 12, 14));
                switch (extremeStatusCellIndexOfMinVoltage){
                    default:
                        extremeStatus.setCellIndexOfMinVoltage(extremeStatusCellIndexOfMinVoltage);
                        break;
                }
                //电池单体电压最低值
                Integer extremeStatusCellMinVoltage = HexUtils.hexStringToDecimal(SubStringUtil.subStr(gb32960MessageData.getData(), 14, 18));
                Double cellMinVoltage = new BigDecimal(extremeStatusCellMinVoltage).divide(new BigDecimal("1000")).setScale(3, BigDecimal.ROUND_UP).doubleValue();
                extremeStatus.setCellMinVoltage(cellMinVoltage);
                //最高温度子系统号
                Integer extremeStatusSubSystemIndexOfMaxTemperature = HexUtils.hexStringToDecimal(SubStringUtil.subStr(gb32960MessageData.getData(), 18, 20));
                switch (extremeStatusSubSystemIndexOfMaxTemperature){
                    default:
                        extremeStatus.setSubSystemIndexOfMaxTemperature(extremeStatusSubSystemIndexOfMaxTemperature);
                        break;
                }
                //最高温度探针序号
                Integer extremeStatusProbeIndexOfMaxTemperature = HexUtils.hexStringToDecimal(SubStringUtil.subStr(gb32960MessageData.getData(), 20, 22));
                switch (extremeStatusProbeIndexOfMaxTemperature){
                    default:
                        extremeStatus.setProbeIndexOfMaxTemperature(extremeStatusProbeIndexOfMaxTemperature);
                        break;
                }
                //最高温度值
                Integer extremeStatusMaxTemperature = HexUtils.hexStringToDecimal(SubStringUtil.subStr(gb32960MessageData.getData(), 22, 24));
                switch (extremeStatusMaxTemperature){
                    default:
                        extremeStatus.setMaxTemperature(extremeStatusMaxTemperature-40);
                        break;
                }
                //最低温度子系统号
                Integer extremeStatusSubSystemIndexOfMinTemperature = HexUtils.hexStringToDecimal(SubStringUtil.subStr(gb32960MessageData.getData(), 24, 26));
                switch (extremeStatusSubSystemIndexOfMinTemperature){
                    default:
                        extremeStatus.setSubSystemIndexOfMinTemperature(extremeStatusSubSystemIndexOfMinTemperature);
                        break;
                }
                //最低温度探针序号
                Integer extremeStatusProbeIndexOfMinTemperature = HexUtils.hexStringToDecimal(SubStringUtil.subStr(gb32960MessageData.getData(), 26, 28));
                switch (extremeStatusProbeIndexOfMinTemperature){
                    default:
                        extremeStatus.setProbeIndexOfMinTemperature(extremeStatusProbeIndexOfMinTemperature);
                        break;
                }
                //最低温度值
                Integer extremeStatusMinTemperature = HexUtils.hexStringToDecimal(SubStringUtil.subStr(gb32960MessageData.getData(), 28, 30));
                switch (extremeStatusMinTemperature){
                    default:
                        extremeStatus.setMinTemperature(extremeStatusMinTemperature-40);
                        break;
                }
                dflzmBaseDTO.setExtremeStatus(extremeStatus);
                gb32960MessageData.setData(SubStringUtil.subStr(gb32960MessageData.getData(),30,gb32960MessageData.getData().length()));
            }
            if("07".equals(SubStringUtil.subStrStart(gb32960MessageData.getData(),2))){
                //计数器
                Integer i=2;
                //TODO 报警数据处理
                GB32960AlertStatus alertStatus = new GB32960AlertStatus();
                //最高报警等级,为当前发生的故障中的最高等级值，有效值范围：0～3
                String alertStatusHighestAlertLevel = SubStringUtil.subStr(gb32960MessageData.getData(), i, i+=2);
                switch (alertStatusHighestAlertLevel){
                    case "00":
                        alertStatus.setHighestAlertLevel("LEVEL_0");
                        break;
                    case "01":
                        alertStatus.setHighestAlertLevel("LEVEL_1");
                        break;
                    case "02":
                        alertStatus.setHighestAlertLevel("LEVEL_2");
                        break;
                    case "03":
                        alertStatus.setHighestAlertLevel("LEVEL_3");
                        break;
                    case "FE":
                        alertStatus.setHighestAlertLevel("CLOSED");
                        break;
                    case "FF":
                        alertStatus.setHighestAlertLevel("INVALID");
                        break;
                    default:
                        alertStatus.setHighestAlertLevel(alertStatusHighestAlertLevel);
                        break;
                }
                //通用报警标识
                String universalAlarmIdentification = SubStringUtil.subStr(gb32960MessageData.getData(), i, i+=8);
                log.info("Universal alarm identification--"+universalAlarmIdentification);

                //可充电储能装置故障总数N1,N1个可充电储能装置故障，有效值范围：0～252，“0xFE”表示异常，“0xFF”表示无效
                Integer alertStatusEnergyStorageAlertCount = HexUtils.hexStringToDecimal(SubStringUtil.subStr(gb32960MessageData.getData(), i, i+=2));
                switch (alertStatusEnergyStorageAlertCount){
                    case 0:
                        alertStatus.setEnergyStorageAlertCount(alertStatusEnergyStorageAlertCount);
                        break;
                    default:
                        alertStatus.setEnergyStorageAlertCount(alertStatusEnergyStorageAlertCount);
                        //可充电储能装置故障代码列表
                        Integer alertStatusEnergyStorageAlertList = HexUtils.hexStringToDecimal(SubStringUtil.subStr(gb32960MessageData.getData(), i, i += 4 * alertStatusEnergyStorageAlertCount));
                        log.info("可充电储能装置故障代码列表--"+alertStatusEnergyStorageAlertList);
                        switch (alertStatusEnergyStorageAlertList){
                            default:
                                alertStatus.setEnergyStorageAlertList(new ArrayList<>());
                                break;
                        }
                        break;
                }
                //驱动电机,故障总数N2,N2个驱动电机故障，有效值范围：0～252，“0xFE”表示异常，“0xFF”表示无效
                Integer alertStatusDriveMotorAlertCount = HexUtils.hexStringToDecimal(SubStringUtil.subStr(gb32960MessageData.getData(), i, i+=2));
                switch (alertStatusDriveMotorAlertCount){
                    case 0:
                        alertStatus.setDriveMotorAlertCount(alertStatusDriveMotorAlertCount);
                        break;
                    default:
                        alertStatus.setDriveMotorAlertCount(alertStatusDriveMotorAlertCount);
                        //驱动电机故障代码列表
                        Integer alertStatusDriveMotorAlertList = HexUtils.hexStringToDecimal(SubStringUtil.subStr(gb32960MessageData.getData(), i, i += 4 * alertStatusDriveMotorAlertCount));
                        log.info("驱动电机故障代码列表--"+alertStatusDriveMotorAlertList);
                        switch (alertStatusDriveMotorAlertList){
                            default:
                                alertStatus.setDriveMotorAlertList(new ArrayList<>());
                                break;
                        }
                        break;
                }

                //发动机故障总数N3,N3个驱动电机故障，有效值范围：0～252，“0xFE”表示异常，“0xFF”表示无效
                Integer alertStatusEngineAlertCount = HexUtils.hexStringToDecimal(SubStringUtil.subStr(gb32960MessageData.getData(), i, i+=2));
                switch (alertStatusEngineAlertCount){
                    case 0:
                        alertStatus.setEngineAlertCount(alertStatusEngineAlertCount);
                        break;
                    default:
                        alertStatus.setEngineAlertCount(alertStatusEngineAlertCount);
                        //发动机故障列表
                        Integer alertStatusEngineAlertList = HexUtils.hexStringToDecimal(SubStringUtil.subStr(gb32960MessageData.getData(), i, i += 4 * alertStatusEngineAlertCount));
                        log.info("发动机故障列表--"+alertStatusEngineAlertList);
                        switch (alertStatusEngineAlertList){
                            default:
                                alertStatus.setEngineAlertList(new ArrayList<>());
                                break;
                        }
                        break;
                }
                //其他故障总数N4,N4个驱动电机故障，有效值范围：0～252，“0xFE”表示异常，“0xFF”表示无效
                Integer otherAlertCount = HexUtils.hexStringToDecimal(SubStringUtil.subStr(gb32960MessageData.getData(), i, i+=2));
                switch (otherAlertCount){
                    case 0:
                        alertStatus.setOtherAlertCount(otherAlertCount);
                        break;
                    default:
                        alertStatus.setOtherAlertCount(otherAlertCount);
                        //其他故障列表
                        Integer otherFaultList = HexUtils.hexStringToDecimal(SubStringUtil.subStr(gb32960MessageData.getData(), i, i += 4 * otherAlertCount));
                        log.info("其他故障列表--"+otherFaultList);
                        switch (otherFaultList){
                            default:
                                alertStatus.setOtherAlertList(new ArrayList<>());
                                break;
                        }
                        break;
                }
                dflzmBaseDTO.setAlertStatus(alertStatus);
                gb32960MessageData.setData(SubStringUtil.subStr(gb32960MessageData.getData(),i += 4 * alertStatusEngineAlertCount,gb32960MessageData.getData().length()));
            }
            if("08".equals(SubStringUtil.subStrStart(gb32960MessageData.getData(),2))){
                //TODO 可充电储能装置电压数据
                //计数器
                Integer i=2;
                GB32960EnergyStorageVoltageStatus energyStorageVoltageStatus = new GB32960EnergyStorageVoltageStatus();
                //可充电储能子系统个数,N个可充电储能子系统，有效值范围：1～250，“0xFE”表示异常，“0xFF”表示无效
                Integer subSystemOfEnergyStorageCount = HexUtils.hexStringToDecimal(SubStringUtil.subStr(gb32960MessageData.getData(), i, i+=2));
                energyStorageVoltageStatus.setSubSystemOfEnergyStorageCount(subSystemOfEnergyStorageCount);
                //可充电储能子系统电压信息列表
                List<GB32960EnergyStorageVoltages> energyStorageVoltagesList = new ArrayList<>();
                for (int j = 1; j <= subSystemOfEnergyStorageCount; j++) {
                    GB32960EnergyStorageVoltages energyStorageVoltages = new GB32960EnergyStorageVoltages();
                    //可充电储能子系统号.有效值范围：1～250。有效值范围：1～250
                    Integer energyStorageSubSystemIndex = HexUtils.hexStringToDecimal(SubStringUtil.subStr(gb32960MessageData.getData(), i, i+=2));
                    energyStorageVoltages.setEnergyStorageSubSystemIndex(energyStorageSubSystemIndex);
                    //可充电储能装置电压,有效值范围：0～10000（表示0V～1000V），最小计量单元：0.1V，“0xFF,0xFE”表示异常，“0xFF,0xFF”表示无效
                    Integer energyStorageVoltage = HexUtils.hexStringToDecimal(SubStringUtil.subStr(gb32960MessageData.getData(), i, i+=4));
                    Double energyStorageVoltageDouble = new BigDecimal(energyStorageVoltage).divide(new BigDecimal("10")).setScale(1, BigDecimal.ROUND_UP).doubleValue();
                    energyStorageVoltages.setEnergyStorageVoltage(energyStorageVoltageDouble);
                    //可充电储能装置电流,有效值范围： 0～20000（数值偏移量1000A，表示-1000A～+1000A），最小计量单元：0.1A，“0xFF,0xFE”表示异常，“0xFF,0xFF”表示无效
                    Integer energyStorageCurrent = HexUtils.hexStringToDecimal(SubStringUtil.subStr(gb32960MessageData.getData(), i, i+=4));
                    Double energyStorageCurrentDouble = new BigDecimal(energyStorageCurrent).subtract(new BigDecimal(1000)).divide(new BigDecimal("10")).setScale(1, BigDecimal.ROUND_UP).doubleValue();
                    energyStorageVoltages.setEnergyStorageCurrent(energyStorageCurrentDouble);
                    //单体电池总数,N个电池单体，有效值范围：1～65531，“0xFF,0xFE”表示异常，“0xFF,0xFF”表示无效
                    Integer cellCount = HexUtils.hexStringToDecimal(SubStringUtil.subStr(gb32960MessageData.getData(), i, i+=4));
                    energyStorageVoltages.setCellCount(cellCount);
                    //本帧起始电池序号,当本帧单体个数超过200时，应拆分成多帧数据进行传输，有效值范围：1～65531
                    Integer frameCellStartIndex = HexUtils.hexStringToDecimal(SubStringUtil.subStr(gb32960MessageData.getData(), i, i+=4));
                    energyStorageVoltages.setFrameCellStartIndex(frameCellStartIndex);
                    //本帧单体电池总数,本帧单体总数 m;有效值范围：1～200
                    Integer frameCellCount = HexUtils.hexStringToDecimal(SubStringUtil.subStr(gb32960MessageData.getData(), i, i+=2));
                    energyStorageVoltages.setFrameCellCount(frameCellCount);
                    List<Double> cellVoltages = new ArrayList<>();
                    for (int k = 1; k <= frameCellCount; k++) {
                        //单体电池电电压
                        Integer cellVoltage = HexUtils.hexStringToDecimal(SubStringUtil.subStr(gb32960MessageData.getData(), i, i +=4));
                        Double cellVoltageDouble = new BigDecimal(cellVoltage).divide(new BigDecimal("1000")).setScale(3, BigDecimal.ROUND_UP).doubleValue();
                        cellVoltages.add(cellVoltageDouble);
                    }
                    energyStorageVoltages.setCellVoltages(cellVoltages);
                    energyStorageVoltagesList.add(energyStorageVoltages);
                }
                //可充电储能子系统电压信息列表
                energyStorageVoltageStatus.setEnergyStorageVoltages(energyStorageVoltagesList);
                dflzmBaseDTO.setEnergyStorageVoltageStatus(energyStorageVoltageStatus);
                gb32960MessageData.setData(SubStringUtil.subStr(gb32960MessageData.getData(),i,gb32960MessageData.getData().length()));
            }
            if("09".equals(SubStringUtil.subStrStart(gb32960MessageData.getData(),2))){
                //TODO 可充电储能装置温度数据
                //计数器
                Integer i=2;
                GB32960EnergyStorageTemperatureStatus energyStorageTemperatureStatus = new GB32960EnergyStorageTemperatureStatus();
                //可充电储能子系统个数,N个可充电储能装置，有效值范围：1～250，“0xFE”表示异常，“0xFF”表示无效
                Integer subEnergyStorageSystemCount = HexUtils.hexStringToDecimal(SubStringUtil.subStr(gb32960MessageData.getData(), i, i+=2));
                energyStorageTemperatureStatus.setSubEnergyStorageSystemCount(subEnergyStorageSystemCount);
                //可充电储能子系统温度信息列表,按可充电储能子系统代号依次排列，每个可充电储能子系统温度分布数据格
                List<GB32960EnergyStorageTemperatures> energyStorageTemperaturesList = new ArrayList<>();
                for (int j = 1; j <= subEnergyStorageSystemCount; j++) {
                    GB32960EnergyStorageTemperatures energyStorageTemperatures = new GB32960EnergyStorageTemperatures();
                    //可充电储能子系统号,有效值范围：1～250
                    Integer energyStorageSubSystemIndex1 = HexUtils.hexStringToDecimal(SubStringUtil.subStr(gb32960MessageData.getData(), i, i+=2));
                    energyStorageTemperatures.setEnergyStorageSubSystemIndex1(energyStorageSubSystemIndex1);
                    //可充电储能,温度探针个数,N个温度探针，有效值范围：1～65531，“0xFF,0xFE”表示异常，“0xFF,0xFF”表示无效
                    Integer probeCount = HexUtils.hexStringToDecimal(SubStringUtil.subStr(gb32960MessageData.getData(), i, i+=4));
                    energyStorageTemperatures.setProbeCount(probeCount);
                    List<Integer> cellTemperatures = new ArrayList<>();
                    for (int k = 1; k <= probeCount; k++) {
                        //可充电储能子系统,各温度探针,检测到的温度值,有效值范围：0～250 （数值偏移量40℃，表示-40℃～+210℃），最小计量单元：1℃，“0xFE”表示异常，“0xFF”表示无效。
                        cellTemperatures.add(HexUtils.hexStringToDecimal(SubStringUtil.subStr(gb32960MessageData.getData(), i, i+=2))-40);
                    }
                    energyStorageTemperatures.setCellTemperatures(cellTemperatures);
                    energyStorageTemperaturesList.add(energyStorageTemperatures);
                }
                //可充电储能子系统温度信息列表,按可充电储能子系统代号依次排列，每个可充电储能子系统温度分布数据格
                energyStorageTemperatureStatus.setEnergyStorageTemperatures(energyStorageTemperaturesList);
                dflzmBaseDTO.setEnergyStorageTemperatureStatus(energyStorageTemperatureStatus);
                gb32960MessageData.setData(SubStringUtil.subStr(gb32960MessageData.getData(),i,gb32960MessageData.getData().length()));
            }
            //TODO 数据解析完成推送至mq
            log.info("数据解析完成推送至mq{}",JSON.toJSONString(dflzmBaseDTO));
        }
        //响应标识
        gb32960MessageData.setMsgResponse("01");
        //数据单元长度
        gb32960MessageData.setDataCellLength("0006");
        //数据单元-时间获取
        StringBuffer timestamp=new StringBuffer().append(SubStringUtil.subStrStart(primaryData,12));
        //校验码计算
        StringBuffer checkCodeData=new StringBuffer().append(gb32960MessageData.getMsgCommand()).append(gb32960MessageData.getMsgResponse())
                .append(gb32960MessageData.getUniqueIdentifier()).append(gb32960MessageData.getEncryption()).append(gb32960MessageData.getDataCellLength()).append(timestamp);
        gb32960MessageData.setCheckCode(HexUtils.getBCC(String.valueOf(checkCodeData)));
        StringBuffer pushData=new StringBuffer().append(gb32960MessageData.getMsgHead()).append(gb32960MessageData.getMsgCommand()).append(gb32960MessageData.getMsgResponse())
                .append(gb32960MessageData.getUniqueIdentifier()).append(gb32960MessageData.getEncryption()).append(gb32960MessageData.getDataCellLength()).append(timestamp).append(gb32960MessageData.getCheckCode());
        return String.valueOf(pushData);
    }

    public static void main(String[] args) throws UnsupportedEncodingException {
        String a = "4c464341483935570344d33303130303936";
        String newstr = new String(a.getBytes(Charset.defaultCharset()),"US-ASCII");
        String bcc = HexUtils.getBCC("4C46434148393657374D3330363032303101001E1508010A1228000A38393836303434393138313938303037363136370100");
        System.out.println(bcc);
    }



}
