package com.mqttsnet.thinglinks.link.controller.protocol;

import com.alibaba.fastjson.JSON;
import com.mqttsnet.thinglinks.common.core.compile.jdk.JDKDynamicCompiler;
import com.mqttsnet.thinglinks.common.core.execute.ClassExecuteHandler;
import com.mqttsnet.thinglinks.common.core.web.domain.AjaxResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.PrintWriter;

/**
 * @program: thinglinks
 * @description: 动态编译代码Controller
 * @packagename: com.mqttsnet.thinglinks.link.controller.protocol
 * @author: ShiHuan Sun
 * @e-mainl: 13733918655@163.com
 * @date: 2022-07-01 18:31
 **/
@RestController
@RequestMapping("/protocolCompileXcode")
@Slf4j
public class CompileXcodeController {
    /**
     * 动态编译代码
     * @param request
     * @param response
     * @throws Exception
     */
    @PostMapping("/dynamicallyXcode")
    public AjaxResult importProductJson(HttpServletRequest request, HttpServletResponse response) throws Exception {
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        // 代码，要求类必须实现BiFunction接口
        String code = request.getParameter("code");
        PrintWriter out = null;
        try {
            out = response.getWriter();

            Class<?> newClass = JDKDynamicCompiler.compile(code);
            Object result = ClassExecuteHandler.execApply(newClass, request, response);
            return new AjaxResult(200,JSON.toJSONString(result));
        } catch (Throwable e) {
            return new AjaxResult(500,JSON.toJSONString(e.getMessage()));
        } finally {
            out.flush();
            out.close();
        }
    }


}
