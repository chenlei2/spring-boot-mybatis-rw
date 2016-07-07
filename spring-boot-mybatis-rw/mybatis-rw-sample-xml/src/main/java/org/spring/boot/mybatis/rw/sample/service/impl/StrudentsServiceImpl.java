package org.spring.boot.mybatis.rw.sample.service.impl;

import org.spring.boot.mybatis.rw.sample.mapper.Students;
import org.spring.boot.mybatis.rw.sample.mapper.StudentsMapper;
import org.spring.boot.mybatis.rw.sample.service.StrudentsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
@Service
public class StrudentsServiceImpl implements StrudentsService {
	@Autowired
	private StudentsMapper studentsMapper;
	@Transactional
	public void rw() {
		Students students = studentsMapper.selectByPrimaryKey(1L);
		System.out.println(students.getName());
		students = studentsMapper.selectByPrimaryKey(1L);
		System.out.println(students.getName());
		students.setName("rw");
		studentsMapper.updateByPrimaryKeySelective(students);
		students.setId(2L);
		studentsMapper.updateByPrimaryKeySelective(students);

//		throw new RuntimeException();
	}

}
