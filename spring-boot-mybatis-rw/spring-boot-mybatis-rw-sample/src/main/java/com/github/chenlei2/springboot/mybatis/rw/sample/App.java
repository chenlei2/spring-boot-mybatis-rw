package com.github.chenlei2.springboot.mybatis.rw.sample;

import javax.validation.constraints.NotNull;

import com.github.chenlei2.springboot.mybatis.rw.sample.service.StrudentsService;
import org.mybatis.spring.annotation.MapperScan;
import com.github.chenlei2.springboot.mybatis.rw.starter.datasource.DataSourceHold;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hello world!
 *
 */
@RestController
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
//@Import(value={DataConfiguration.class})
@MapperScan("org.spring.boot.mybatis.rw.sample.mapper")
public class App 
{
	@Autowired
	@NotNull
	private StrudentsService strudentsService;
	
	
	@RequestMapping("/hello")
	String home() {
		/**
		 * 实际工作中，这一步通过用户登录拦截器设置用户当前所在数据组
		 */
		DataSourceHold.CURRENT_DATASOURCE.set("rout1");
		strudentsService.rw(); 

		return "Hello World!";
	}
	
    public static void main( String[] args )
    {
    	SpringApplication.run(App.class, args);
    }
}
