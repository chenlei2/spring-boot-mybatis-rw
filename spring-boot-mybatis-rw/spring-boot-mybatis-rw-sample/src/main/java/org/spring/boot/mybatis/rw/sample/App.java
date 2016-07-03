package org.spring.boot.mybatis.rw.sample;

import org.mybatis.spring.annotation.MapperScan;
import org.spring.boot.mybatis.rw.sample.service.StrudentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Hello world!
 *
 */
@RestController
@SpringBootApplication
@Import(value={DataBaseConfiguration.class})
@MapperScan("org.spring.boot.mybatis.rw.sample.mapper")
public class App 
{
	@Autowired
	private StrudentsService strudentsService;
	
	
	@RequestMapping("/")
	String home() {
		strudentsService.rw(); 
		return "Hello World!";
	}
	
    public static void main( String[] args )
    {
    	SpringApplication.run(App.class, args);
    }
}
