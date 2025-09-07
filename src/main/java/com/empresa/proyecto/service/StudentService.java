package com.empresa.proyecto.service;

import com.empresa.proyecto.model.StudentResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Service
public class StudentService {

    List<StudentResponse> list;

    public List<StudentResponse> restCallGetStudents() {
        RestTemplate restTemplate = new RestTemplate();
        StudentResponse[] studentResponse =
                restTemplate.getForObject("http://localhost:8081/api/v1/students",
                        StudentResponse[].class);
        list = new ArrayList<>(Arrays.asList(studentResponse));
        return list;
    }

    public StudentResponse getStudent(long id, String name) {
        System.out.println("id = " + id + " and name = " + name);
        if (list == null) {
            restCallGetStudents();
        }

        if (list != null && !list.isEmpty()) {
            return list.remove(0);
        }
        return null;
    }
}
