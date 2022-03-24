package com.my.es.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.my.es.domain.ReadBookPd;
import com.my.es.service.IReadBookPdService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Zijian Liao
 * @since 1.0.0
 */
@RestController
public class ReadBookPdController {

    @Autowired
    private IReadBookPdService readBookPdService;

    @GetMapping("/list")
    public Object list(){
        Page<ReadBookPd> page = new Page<>(1, 2);
        LambdaQueryWrapper<ReadBookPd> wrapper = Wrappers.lambdaQuery();
        return readBookPdService.page(page, wrapper);
    }

    @GetMapping("/index")
    public Object index(){
        return readBookPdService.importAll();
    }
}
