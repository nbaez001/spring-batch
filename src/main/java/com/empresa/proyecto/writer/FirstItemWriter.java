package com.empresa.proyecto.writer;

import com.empresa.proyecto.model.StudentResponse;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Component
public class FirstItemWriter implements ItemWriter<StudentResponse> {

    @Override
    public void write(Chunk<? extends StudentResponse> chunk) throws Exception {
        System.out.println("Inside Item Writer");
        chunk.forEach(System.out::println);
    }
}