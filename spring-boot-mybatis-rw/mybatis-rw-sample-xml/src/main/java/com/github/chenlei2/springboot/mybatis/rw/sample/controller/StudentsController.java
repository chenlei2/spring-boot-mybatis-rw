package com.github.chenlei2.springboot.mybatis.rw.sample.controller;

import com.github.chenlei2.springboot.mybatis.rw.sample.service.StrudentsService;
import pres.chenlei2.springboot.mybatis.rw.sample.service.StrudentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/students")
public class StudentsController {

	@Autowired
	private StrudentsService strudentsService;
	@RequestMapping("/index")
	public @ResponseBody String get(){
		strudentsService.rw();
		return "hello world";
	}
}
