package com.mqttsnet.thinglinks.video.utils.gb28181;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.dom4j.Attribute;
import org.dom4j.Document;
import org.dom4j.DocumentException;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.springframework.util.ObjectUtils;
import org.springframework.util.ReflectionUtils;

import javax.sip.RequestEvent;
import javax.sip.message.Request;
import java.io.ByteArrayInputStream;
import java.io.StringReader;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

/**
 * 基于dom4j的工具包
 */
@Slf4j
public class XmlUtil {

    /**
     * 解析XML为Document对象
     */
    public static Element parseXml(String xml) {
        Document document = null;
        //
        StringReader sr = new StringReader(xml);
        SAXReader saxReader = new SAXReader();
        try {
            document = saxReader.read(sr);
        } catch (DocumentException e) {
            log.error("解析失败", e);
        }
        return null == document ? null : document.getRootElement();
    }

    /**
     * 获取element对象的text的值
     *
     * @param em  节点的对象
     * @param tag 节点的tag
     * @return 节点
     */
    public static String getText(Element em, String tag) {
        if (null == em) {
            return null;
        }
        Element e = em.element(tag);
        //
        return null == e ? null : e.getText().trim();
    }

    /**
     * 获取element对象的text的值
     *
     * @param em  节点的对象
     * @param tag 节点的tag
     * @return 节点
     */
    public static Double getDouble(Element em, String tag) {
        if (null == em) {
            return null;
        }
        Element e = em.element(tag);
        if (null == e) {
            return null;
        }
        String text = e.getText().trim();
        if (ObjectUtils.isEmpty(text) || !NumberUtils.isParsable(text)) {
            return null;
        }
        return Double.parseDouble(text);
    }

    /**
     * 获取element对象的text的值
     *
     * @param em  节点的对象
     * @param tag 节点的tag
     * @return 节点
     */
    public static Integer getInteger(Element em, String tag) {
        if (null == em) {
            return null;
        }
        Element e = em.element(tag);
        if (null == e) {
            return null;
        }
        String text = e.getText().trim();
        if (ObjectUtils.isEmpty(text) || !NumberUtils.isParsable(text)) {
            return null;
        }
        return Integer.parseInt(text);
    }

    /**
     * 递归解析xml节点，适用于 多节点数据
     *
     * @param node     node
     * @param nodeName nodeName
     * @return List<Map < String, Object>>
     */
    public static List<Map<String, Object>> listNodes(Element node, String nodeName) {
        if (null == node) {
            return null;
        }
        // 初始化返回
        List<Map<String, Object>> listMap = new ArrayList<Map<String, Object>>();
        // 首先获取当前节点的所有属性节点
        List<Attribute> list = node.attributes();

        Map<String, Object> map = null;
        // 遍历属性节点
        for (Attribute attribute : list) {
            if (nodeName.equals(node.getName())) {
                if (null == map) {
                    map = new HashMap<String, Object>();
                    listMap.add(map);
                }
                // 取到的节点属性放到map中
                map.put(attribute.getName(), attribute.getValue());
            }

        }
        // 遍历当前节点下的所有节点 ，nodeName 要解析的节点名称
        // 使用递归
        Iterator<Element> iterator = node.elementIterator();
        while (iterator.hasNext()) {
            Element e = iterator.next();
            listMap.addAll(listNodes(e, nodeName));
        }
        return listMap;
    }

    /**
     * xml转json
     *
     * @param element
     * @param json
     */
    public static void node2Json(Element element, JSONObject json) {
        // 如果是属性
        for (Object o : element.attributes()) {
            Attribute attr = (Attribute) o;
            if (!ObjectUtils.isEmpty(attr.getValue())) {
                json.put("@" + attr.getName(), attr.getValue());
            }
        }
        List<Element> chdEl = element.elements();
        if (chdEl.isEmpty() && !ObjectUtils.isEmpty(element.getText())) {// 如果没有子元素,只有一个值
            json.put(element.getName(), element.getText());
        }

        for (Element e : chdEl) {   // 有子元素
            if (!e.elements().isEmpty()) {  // 子元素也有子元素
                JSONObject chdjson = new JSONObject();
                node2Json(e, chdjson);
                Object o = json.get(e.getName());
                if (o != null) {
                    JSONArray jsona = null;
                    if (o instanceof JSONObject) {  // 如果此元素已存在,则转为jsonArray
                        JSONObject jsono = (JSONObject) o;
                        json.remove(e.getName());
                        jsona = new JSONArray();
                        jsona.add(jsono);
                        jsona.add(chdjson);
                    }
                    if (o instanceof JSONArray) {
                        jsona = (JSONArray) o;
                        jsona.add(chdjson);
                    }
                    json.put(e.getName(), jsona);
                } else {
                    if (!chdjson.isEmpty()) {
                        json.put(e.getName(), chdjson);
                    }
                }
            } else { // 子元素没有子元素
                for (Object o : element.attributes()) {
                    Attribute attr = (Attribute) o;
                    if (!ObjectUtils.isEmpty(attr.getValue())) {
                        json.put("@" + attr.getName(), attr.getValue());
                    }
                }
                if (!e.getText().isEmpty()) {
                    json.put(e.getName(), e.getText());
                }
            }
        }
    }

    public static Element getRootElement(RequestEvent evt) throws DocumentException {

        return getRootElement(evt, "gb2312");
    }

    public static Element getRootElement(RequestEvent evt, String charset) throws DocumentException {
        Request request = evt.getRequest();
        return getRootElement(request.getRawContent(), charset);
    }

    public static Element getRootElement(byte[] content, String charset) throws DocumentException {
        if (charset == null) {
            charset = "gb2312";
        }
        SAXReader reader = new SAXReader();
        reader.setEncoding(charset);
        Document xml = reader.read(new ByteArrayInputStream(content));
        return xml.getRootElement();
    }

    /**
     * 新增方法支持内部嵌套
     *
     * @param element xmlElement
     * @param clazz 结果类
     * @param <T> 泛型
     * @return 结果对象
     * @throws NoSuchMethodException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws IllegalAccessException
     */
    public static <T> T loadElement(Element element, Class<T> clazz) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Field[] fields = clazz.getDeclaredFields();
        T t = clazz.getDeclaredConstructor().newInstance();
        for (Field field : fields) {
            ReflectionUtils.makeAccessible(field);
            MessageElement annotation = field.getAnnotation(MessageElement.class);
            if (annotation == null) {
                continue;
            }
            String value = annotation.value();
            String subVal = annotation.subVal();
            Element element1 = element.element(value);
            if (element1 == null) {
                continue;
            }
            if ("".equals(subVal)) {
                // 无下级数据
                Object fieldVal = element1.isTextOnly() ? element1.getText() : loadElement(element1, field.getType());
                Object o = simpleTypeDeal(field.getType(), fieldVal);
                ReflectionUtils.setField(field, t, o);
            } else {
                // 存在下级数据
                ArrayList<Object> list = new ArrayList<>();
                Type genericType = field.getGenericType();
                if (!(genericType instanceof ParameterizedType)) {
                    continue;
                }
                Class<?> aClass = (Class<?>) ((ParameterizedType) genericType).getActualTypeArguments()[0];
                for (Element element2 : element1.elements(subVal)) {
                    list.add(loadElement(element2, aClass));
                }
                ReflectionUtils.setField(field, t, list);
            }
        }
        return t;
    }

    public static <T> T elementDecode(Element element, Class<T> clazz) throws NoSuchMethodException, InvocationTargetException, InstantiationException, IllegalAccessException {
        Field[] fields = clazz.getDeclaredFields();
        T t = clazz.getDeclaredConstructor().newInstance();
        for (Field field : fields) {
            ReflectionUtils.makeAccessible(field);
            MessageElementForCatalog annotation = field.getAnnotation(MessageElementForCatalog.class);
            if (annotation == null) {
                continue;
            }
            String[] values = annotation.value();
            for (String value : values) {
                boolean subVal = value.contains(".");
                if (!subVal) {
                    Element element1 = element.element(value);
                    if (element1 == null) {
                        continue;
                    }
                    // 无下级数据
                    Object fieldVal = element1.isTextOnly() ? element1.getText() : loadElement(element1, field.getType());
                    Object o = simpleTypeDeal(field.getType(), fieldVal);
                    ReflectionUtils.setField(field, t, o);
                    break;
                } else {
                    String[] pathArray = value.split("\\.");
                    Element subElement = element;
                    for (String path : pathArray) {
                        subElement = subElement.element(path);
                        if (subElement == null) {
                            break;
                        }
                    }
                    if (subElement == null) {
                        continue;
                    }
                    Object fieldVal = subElement.isTextOnly() ? subElement.getText() : loadElement(subElement, field.getType());
                    Object o = simpleTypeDeal(field.getType(), fieldVal);
                    ReflectionUtils.setField(field, t, o);
                }
            }

        }
        return t;
    }

    /**
     * 简单类型处理
     *
     * @param tClass
     * @param val
     * @return
     */
    private static Object simpleTypeDeal(Class<?> tClass, Object val) {
        try {
            if (val == null || val.toString().equalsIgnoreCase("null")) {
                return null;
            }
            if (tClass.equals(String.class)) {
                return val.toString();
            }
            if (tClass.equals(Integer.class)) {
                return Integer.valueOf(val.toString());
            }
            if (tClass.equals(Double.class)) {
                return Double.valueOf(val.toString());

            }
            if (tClass.equals(Long.class)) {
                return Long.valueOf(val.toString());
            }
            return val;
        } catch (Exception e) {
            return null;
        }
    }

    private enum ChannelType {
        CivilCode, BusinessGroup, VirtualOrganization, Other
    }
}
