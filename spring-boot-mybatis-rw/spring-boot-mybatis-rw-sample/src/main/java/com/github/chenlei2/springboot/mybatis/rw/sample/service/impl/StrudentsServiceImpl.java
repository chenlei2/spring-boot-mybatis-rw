package com.github.chenlei2.springboot.mybatis.rw.sample.service.impl;

import com.github.chenlei2.springboot.mybatis.rw.sample.mapper.Students;
import com.github.chenlei2.springboot.mybatis.rw.sample.mapper.StudentsMapper;
import com.github.chenlei2.springboot.mybatis.rw.sample.service.StrudentsService;
import org.mybatis.spring.SqlSessionTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class StrudentsServiceImpl implements StrudentsService {
	@Autowired
	private StudentsMapper studentsMapper;
	@Autowired
	private SqlSessionTemplate sqlSessionTemplate;
//	@Transactional
	public void rw() {

		Students students = (Students)sqlSessionTemplate.selectOne("selectByPrimaryKey", 1L);
		System.out.println(students.getName());
		students.setName("rw");
		sqlSessionTemplate.update("updateByPrimaryKeySelective", students);
		studentsMapper.updateByPrimaryKeySelective(students);
		students.setId(2L);
		studentsMapper.updateByPrimaryKeySelective(students);
		students = studentsMapper.selectByPrimaryKey(2L);
		System.out.println(students.getName());
//		throw new RuntimeException();
	}

}
