package com.nowcoder.community.controller;

import com.nowcoder.community.dao.alphaDao;
import com.nowcoder.community.service.AlphaService;
import com.nowcoder.community.util.CommunityUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.servlet.ModelAndView;

import com.alibaba.fastjson.JSONObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/alpha")
public class AlphaController
{
    @RequestMapping("/hello")
    @ResponseBody
    public String sayHello(){
        return "hello world";
    }

    @Autowired
    private alphaDao alpDap;

    @RequestMapping("/getdata")
    @ResponseBody
    public String getdata(){
        return alpDap.select();
    }

    @RequestMapping(path = "/student",method = RequestMethod.POST)
    @ResponseBody
    public String getStudent(String name,int age){
        System.out.println(name+age);
        return "success";
    }

    @RequestMapping(path = "/school",method = RequestMethod.GET)
    @ResponseBody
    public ModelAndView getSchool(){
        ModelAndView modelAndView=new ModelAndView();
        modelAndView.addObject("school","nuaa");
        modelAndView.setViewName("/demo/view");
        return modelAndView;
    }

    //JSON
    @RequestMapping(path="/employee",method = RequestMethod.GET)
    @ResponseBody
    public List getEmployee(){

        List<Map<String ,Object>>listmap=new ArrayList<>();
        Map<String ,Object>map=new HashMap<>();
        map.put("name","zhangsan");
        map.put("age",23);
        listmap.add(map);
        map=new HashMap<>();
        map.put("name","lise");
        map.put("age",23);
        listmap.add(map);
        return listmap;
    }


    //ajax示例
    @RequestMapping(path = "/ajax",method = RequestMethod.POST)
    @ResponseBody
    public String testAjax(String name,int age){
        System.out.println(name);
        System.out.println(age);
        String s = CommunityUtil.getJSONString(0, "操作成功");
        return s;
    }

    @RequestMapping(path = "/ajax",method = RequestMethod.GET)
    @ResponseBody
    public String testAjax2(){
        Map<String ,Object> map=new HashMap<>();
        map.put("name","zhangsan");
        map.put("age",12);
        return CommunityUtil.getJSONString(0,"ok",map);
    }

}

