package com.empresa.proyecto.processor;

import com.empresa.proyecto.model.StudentCsv;
import com.empresa.proyecto.model.StudentRequest;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
public class FirstItemProcessor implements ItemProcessor<StudentCsv, StudentRequest> {

    @Override
    public StudentRequest process(StudentCsv item) throws Exception {
        return new StudentRequest(
                item.getId(),
                item.getFirstName(),
                item.getLastName(),
                item.getEmail());
    }
}
