package org.spring.boot.mybatis.rw.sample.service.impl;

import org.mybatis.spring.SqlSessionTemplate;
import org.spring.boot.mybatis.rw.sample.mapper.Students;
import org.spring.boot.mybatis.rw.sample.mapper.StudentsMapper;
import org.spring.boot.mybatis.rw.sample.service.StrudentsService;
import org.spring.boot.mybatis.rw.starter.datasource.AbstractRWRoutingDataSourceProxy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class StrudentsServiceImpl implements StrudentsService {
	@Autowired
	private StudentsMapper studentsMapper;
	@Autowired
	private SqlSessionTemplate sqlSessionTemplate;
	@Transactional
	public void rw() {
		AbstractRWRoutingDataSourceProxy.FORCE_WRITE.set(true);
		Students students = (Students)sqlSessionTemplate.selectOne("selectByPrimaryKey", 1L);
		System.out.println(students.getName());
		students.setName("rw");
		sqlSessionTemplate.update("updateByPrimaryKeySelective", students);
		//studentsMapper.updateByPrimaryKeySelective(students);
		students.setId(2L);
		studentsMapper.updateByPrimaryKeySelective(students);
		students = studentsMapper.selectByPrimaryKey(2L);
		System.out.println(students.getName());
//		throw new RuntimeException();
	}

}
