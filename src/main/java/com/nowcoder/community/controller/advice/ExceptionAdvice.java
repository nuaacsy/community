package com.nowcoder.community.controller.advice;

import com.nowcoder.community.util.CommunityUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;


//修饰类 全局配置类 可对controller进行全局配置
@ControllerAdvice(annotations = Controller.class)
public class ExceptionAdvice {

    //日志
    private static final Logger logger= LoggerFactory.getLogger(ExceptionAdvice.class);

    //处理所有异常
    @ExceptionHandler({Exception.class})
    public void handleException(Exception e, HttpServletResponse response, HttpServletRequest request) throws IOException {
        logger.error("服务器发生异常："+e.getMessage());
        for(StackTraceElement element:e.getStackTrace()){
            logger.error(element.toString());
        }

        String xRequestedWith=request.getHeader("x-requested-with");
        if("XMLHttpRequest".equals(xRequestedWith)){
            //返回一个普通字符串 json
            response.setContentType("application/plain;char=utf-8");
            PrintWriter writer=response.getWriter();
            writer.write(CommunityUtil.getJSONString(1,"服务器异常！"));

        }
        else {
            response.sendRedirect(request.getContextPath()+"/error");
        }
    }

}
