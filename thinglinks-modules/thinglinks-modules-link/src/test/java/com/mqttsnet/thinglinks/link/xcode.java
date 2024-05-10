package com.mqttsnet.thinglinks.link;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import javax.xml.bind.annotation.adapters.HexBinaryAdapter;
import java.math.BigInteger;
import java.util.Date;

public class xcode {
    public static void main(String[] args) throws Exception {
        String injson = "{\n" +
                "    \"rxInfo\": [\n" +
                "        {\n" +
                "            \"altitude\": 0,\n" +
                "            \"rssi\": -21,\n" +
                "            \"latitude\": 0,\n" +
                "            \"name\": \"Local Gateway\",\n" +
                "            \"time\": \"2022-07-13T06:08:43.895294Z\",\n" +
                "            \"loRaSNR\": 13.8,\n" +
                "            \"mac\": \"24e124fffef542e2\",\n" +
                "            \"longitude\": 0\n" +
                "        }\n" +
                "    ],\n" +
                "    \"txInfo\": {\n" +
                "        \"dataRate\": {\n" +
                "            \"modulation\": \"LORA\",\n" +
                "            \"bandwidth\": 125,\n" +
                "            \"spreadFactor\": 7\n" +
                "        },\n" +
                "        \"adr\": true,\n" +
                "        \"codeRate\": \"4/5\",\n" +
                "        \"frequency\": 471700000\n" +
                "    },\n" +
                "    \"data\": \"A2f5AARofgV9lAEGc9Qm\",\n" +
                "    \"fPort\": 85,\n" +
                "    \"time\": \"2022-07-13T06:08:43.895294Z\",\n" +
                "    \"applicationID\": \"1\",\n" +
                "    \"fCnt\": 63,\n" +
                "    \"deviceName\": \"CO2\",\n" +
                "    \"applicationName\": \"MQTT\",\n" +
                "    \"devEUI\": \"24e124126b122724\"\n" +
                "}";

        if (args != null && args.length != 0) {
            injson = args[0];
        }
        JSONObject parsejson = JSONObject.parseObject(injson);
        System.out.println(Convert2SystemJSON(parsejson));
    }

    public static String Convert2SystemJSON(JSONObject injsonobj) throws Exception {
        String hexdata = ((JSONObject) injsonobj.get("msg")).get("data").toString();
        byte[] bt = java.util.Base64.getDecoder().decode(hexdata);
        JSONObject data = hex2wsdjsonobj(bt);

        JSONArray services = new JSONArray();
        JSONObject server = new JSONObject();
        server.put("serviceId", "serdsd123");
        server.put("data", data);
        server.put("eventTime", new Date());
        services.add(server);

        JSONObject dev = new JSONObject();
        dev.put("deviceId", ((JSONObject) injsonobj.get("msg")).get("devEui"));
        dev.put("services", services);
        JSONArray devices = new JSONArray();
        devices.add(dev);

        JSONObject root = new JSONObject();
        root.put("devices", devices);
        return root.toJSONString();
    }

    /**
     * 十六进制转换成10进制 负数也能转换
     */
    public static int hex16convert2(String hex) throws Exception {
        if (hex.length() != 4) {
            throw new Exception("必须是4个长度");
        }
        int bit1 = Integer.parseInt(hex.substring(0, 1), 16);
        if ( bit1 < 8){
            return Integer.parseInt(hex, 16);
        } else{
            return new BigInteger("FFFF" + hex, 16).intValue();
        }
    }

    /**
     * 温湿度解码
     */
    private static JSONObject hex2wsdjsonobj(byte[] bt) throws Exception {
        javax.xml.bind.annotation.adapters.HexBinaryAdapter hexBinaryAdapter = new HexBinaryAdapter();
        JSONObject data = new JSONObject();
        String temp = hexBinaryAdapter.marshal(new byte[]{bt[2]});
        String temp2 = hexBinaryAdapter.marshal(new byte[]{bt[3]});
        String changtemp = change(temp, temp2);
        data.put("temperature", hex16convert2(changtemp) * 0.01);
        temp = hexBinaryAdapter.marshal(new byte[]{bt[4]});
        temp2 = hexBinaryAdapter.marshal(new byte[]{bt[5]});
        changtemp = change(temp, temp2);
        data.put("humidity", hex16convert2(changtemp) * 0.01);
        temp = hexBinaryAdapter.marshal(new byte[]{bt[8]});
        temp2 = hexBinaryAdapter.marshal(new byte[]{bt[9]});
        changtemp = change(temp, temp2);
        data.put("battery", hex16convert2(changtemp));
        return data;
    }

    public static String change(String start, String end) {
        return end + start;
    }


}
