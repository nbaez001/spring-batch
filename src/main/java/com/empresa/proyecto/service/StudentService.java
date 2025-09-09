package com.empresa.proyecto.service;

import com.empresa.proyecto.model.StudentRequest;
import com.empresa.proyecto.model.StudentResponse;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class StudentService {

    public StudentResponse restCallToCreateStudents(StudentRequest studentRequest) {
        RestTemplate restTemplate = new RestTemplate();
        return restTemplate.postForObject("http://localhost:8081/api/v1/students",
                studentRequest, StudentResponse.class);
    }
}