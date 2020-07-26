package com.example.demo.controller;

import com.example.demo.util.RedisUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class TestController {

    @Autowired
    private RedisUtil redisUtil;

    void redis_write1()
    {
        Map<String, Object> dic = new HashMap<>();
        dic.put("age", 18);
        dic.put("name", "Chris");

        redisUtil.set("user", dic);
    }

    String redis_read1()
    {
        Map<String, Object> dic = (Map<String, Object>) redisUtil.get("user");

        ObjectMapper objectMapper = new ObjectMapper();

        String json = "none";

        try {
            json = objectMapper.writeValueAsString(dic);
            System.out.println(json);
        }catch (JsonProcessingException e) {
            e.printStackTrace();
        }

        return json;
    }

    @GetMapping("redis_write")
    String redis_write()
    {
        redis_write1();
        return "success";
    }

    @GetMapping("redis_read")
    String redis_read(){
        return redis_read1();
    }

}
