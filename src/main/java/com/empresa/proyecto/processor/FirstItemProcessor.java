package com.empresa.proyecto.processor;

import com.empresa.proyecto.entity.mysql.StudentT;
import com.empresa.proyecto.entity.postgresql.Student;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class FirstItemProcessor implements ItemProcessor<Student, StudentT> {

    @Override
    public StudentT process(Student item) {
        System.out.println(item.getId());
        StudentT student = new StudentT();
        student.setId(item.getId());
        student.setFirstName(item.getFirstName());
        student.setLastName(item.getLastName());
        student.setEmail(item.getEmail());
        student.setDeptId(item.getDeptId());
        student.setActive(item.getIsActive() != null &&
                Boolean.parseBoolean(item.getIsActive()));
        return student;
    }
}