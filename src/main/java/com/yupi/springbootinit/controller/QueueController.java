package com.yupi.springbootinit.controller;

import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.LinkedHashMap;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * 线程池接口
 *
 * @author <a href="https://github.com/eat11cookies">hxq</a>
 * 
 */
@RestController
@RequestMapping("/queue")
@CrossOrigin(origins = {"http://140.143.139.251:8080","http://localhost:8000/"})
@Slf4j
public class QueueController {
    @Resource
    private ThreadPoolExecutor threadPoolExecutor;

    @GetMapping("/add")
    public void add(String name){
        CompletableFuture.runAsync(()->{
            log.info("任务执行中"+name+"执行人"+Thread.currentThread().getName());
            try {
                Thread.sleep(600000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        },threadPoolExecutor);
    }

    @GetMapping("get")
    public String get(){
        LinkedHashMap<String,Object> hashMap = new LinkedHashMap<>();
        int size = threadPoolExecutor.getQueue().size();
        hashMap.put("队列长度",size);
        long taskCount = threadPoolExecutor.getTaskCount();
        hashMap.put("任务总数",taskCount);
        long completedTaskCount = threadPoolExecutor.getCompletedTaskCount();
        hashMap.put("已完成任务数",completedTaskCount);
        int activeCount = threadPoolExecutor.getActiveCount();
        hashMap.put("正在工作的线程数",activeCount);
        System.out.println(hashMap);
        return JSONUtil.toJsonStr(hashMap);
    }
}
