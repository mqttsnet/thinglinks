package com.mqttsnet.thinglinks.base.controller.user;

import cn.hutool.core.util.StrUtil;
import cn.idev.excel.FastExcel;
import cn.idev.excel.annotation.ExcelIgnore;
import cn.idev.excel.annotation.ExcelProperty;
import cn.idev.excel.exception.ExcelDataConvertException;
import cn.idev.excel.metadata.property.ExcelContentProperty;
import com.mqttsnet.basic.base.R;
import com.mqttsnet.basic.utils.StrPool;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.servlet.http.HttpServletResponse;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URLEncoder;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
/**
 * @author mqttsnet
 * @version v1.0
 * @date 2022/9/20 11:33 AM
 * @create [2022/9/20 11:33 AM ] [mqttsnet] [初始创建]
 */
@Slf4j
@RequiredArgsConstructor
@Validated
@RestController
@RequestMapping("/excel")
public class ExcelTestController {
    @Operation(summary = "导出数据")
    @PostMapping(value = "/exportItem")
    public void exportItem(HttpServletResponse response) throws IOException {
        try {
            // 这里注意 有同学反应使用swagger 会导致各种问题，请直接用浏览器或者用postman
            response.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            response.setCharacterEncoding("utf-8");
            // 这里URLEncoder.encode可以防止中文乱码 当然和easyexcel没有关系
            String fileName = URLEncoder.encode("【模版】", "UTF-8").replaceAll("\\+", "%20");
            response.setHeader("Content-disposition", "attachment;filename*=utf-8''" + fileName + ".xlsx");

            response.setHeader("Access-Control-Expose-Headers", "Content-Disposition");

            List<DemoData> list = new ArrayList<>();
            list.add(new DemoData().setAge(12D).setName("小w").setDate(LocalDateTime.now()));
            list.add(new DemoData().setAge(13D).setName("小zw").setDate(LocalDateTime.now()));
            list.add(new DemoData().setAge(145D).setName("小sw").setDate(LocalDateTime.now()));
            FastExcel.write(response.getOutputStream(), DemoData.class).sheet("模板").doWrite(list);

        } catch (Exception e) {
            // 重置response
            response.reset();
            response.setContentType(StrPool.CONTENT_TYPE);
            response.setCharacterEncoding(StrPool.UTF_8);
            R error = R.fail("下载文件失败" + e.getMessage());
            response.getWriter().println(com.alibaba.fastjson2.JSON.toJSONString(error));
        }
    }

    /**
     * 补充数据导入
     *
     * @param simpleFile 上传文件
     * @return 是否导入成功
     * @throws Exception 异常
     */
    @Operation(summary = "导入Excel")
    @PostMapping(value = "/import")
    public R<Object> importExcel(@RequestParam(value = "file") MultipartFile simpleFile) throws Exception {
        // 配置excel第一行字段名
        try {
            List<Map<String, Object>> dataList = FastExcel.read(simpleFile.getInputStream())
                    .sheet().doReadSync();

            String failMsg = ValidatorUtils.validateAll(dataList, 1);
            if (StrUtil.isNotEmpty(failMsg)) {
                return R.fail(failMsg);
            }

            log.info("dataList.size={}", dataList.size());

            return R.success(true);
        } catch (ExcelDataConvertException e) {
            log.error("导入数据格式错误", e);
            ExcelContentProperty excelContentProperty = e.getExcelContentProperty();
            Field field = excelContentProperty.getField();
            ExcelProperty excelProperty = field.getAnnotation(ExcelProperty.class);
            String name = "";
            if (excelProperty != null) {
                Schema apiModelProperty = field.getAnnotation(Schema.class);
                name = apiModelProperty != null ? apiModelProperty.description() : "";
            } else {
                name = StrUtil.join(".", excelProperty.value());
            }
            Integer rowIndex = e.getRowIndex();
            Integer columnIndex = e.getColumnIndex() + 1;
            String value = e.getCellData().getStringValue();
            String msg = "第{}行，第{}列，字段【{}】的参数值：【{}】 填写有误，请认真检查。";
            return R.fail(StrUtil.format(msg, rowIndex, columnIndex, name, value));
        }

    }

    @Getter
    @Setter
    @Accessors(chain = true)
    @EqualsAndHashCode
    public static class DemoData {
        @ExcelProperty("字符串标题")
        private String name;
        @ExcelProperty("日期标题")
        private LocalDateTime date;
        @ExcelProperty("年龄")
        private Double age;
        /**
         * 忽略这个字段
         */
        @ExcelIgnore
        private String ignore;
    }
}
