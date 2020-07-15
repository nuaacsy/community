package com.nowcoder.community.service;

import org.springframework.stereotype.Repository;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@Service
@Repository("beandeguanli")
public class AlphaService {

    public  AlphaService(){
        System.out.println("实例化");
    }
    //PostConstruct在构造函数之后执行，init（）方法之前执行。
    //Constructor(构造方法) -> @Autowired(依赖注入) -> @PostConstruct(注释的方法)
    @PostConstruct
    public  void  init(){
        System.out.println("初始化");
    }
    @PreDestroy
    public  void  destory(){
        System.out.println("销毁");
    }
}
