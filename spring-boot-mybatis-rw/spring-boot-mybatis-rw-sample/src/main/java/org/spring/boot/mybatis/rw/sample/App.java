package org.spring.boot.mybatis.rw.sample;

import javax.validation.constraints.NotNull;

import org.mybatis.spring.annotation.MapperScan;
import org.spring.boot.mybatis.rw.sample.service.StrudentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hello world!
 *
 */
@RestController
@SpringBootApplication(exclude={DataSourceAutoConfiguration.class})
@Import(value={DataConfiguration.class})
@MapperScan("org.spring.boot.mybatis.rw.sample.mapper")
public class App 
{
	@Autowired
	@NotNull
	private StrudentsService strudentsService;
	
	
	@RequestMapping("/hello")
	String home() {
		strudentsService.rw(); 
		return "Hello World!";
	}
	
    public static void main( String[] args )
    {
    	SpringApplication.run(App.class, args);
    }
}
