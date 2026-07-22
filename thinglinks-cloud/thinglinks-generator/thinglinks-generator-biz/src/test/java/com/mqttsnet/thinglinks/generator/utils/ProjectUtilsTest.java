package com.mqttsnet.thinglinks.generator.utils;

import com.mqttsnet.basic.database.properties.DatabaseProperties;
import com.mqttsnet.thinglinks.generator.enumeration.ProjectTypeEnum;
import com.mqttsnet.thinglinks.generator.vo.save.ProjectGeneratorVO;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * @author mqttsnet
 * @version v1.0
 * @date 2022/4/5 5:54 PM
 * @create [2022/4/5 5:54 PM ] [mqttsnet] [初始创建]
 */
public class ProjectUtilsTest {
    public static void main(String[] args) throws IOException {
        Path outputDir = Files.createTempDirectory("thinglinks-cloud-generator-");
        Files.writeString(
                outputDir.resolve("pom.xml"),
                "<project>\n    <!-- @thinglinks.generator auto insert root.pom.xml -->\n</project>\n",
                StandardCharsets.UTF_8
        );

        ProjectGeneratorVO vo = new ProjectGeneratorVO();
        vo.setProjectPrefix("thinglinks");
        vo.setOutputDir(outputDir.toString());
        vo.setType(ProjectTypeEnum.CLOUD);
        vo.setAuthor("MqttsNet");
        vo.setServiceName("test");
        vo.setModuleName("test");
        vo.setParent("com.mqttsnet.thinglinks");
        vo.setGroupId("com.mqttsnet.thinglinks");
        vo.setUtilParent("com.mqttsnet.basic");
        vo.setUtilGroupId("com.mqttsnet.basic");
        vo.setVersion("1.0.3");
        vo.setDescription("测试服务");
        vo.setServerPort(8080);
        vo.setSeata(false);
        ProjectUtils.generator(vo, new DatabaseProperties());
    }
}
