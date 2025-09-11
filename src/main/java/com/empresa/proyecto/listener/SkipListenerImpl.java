package com.empresa.proyecto.listener;

import com.empresa.proyecto.entity.mysql.StudentT;
import com.empresa.proyecto.entity.postgresql.Student;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;

@Component
public class SkipListenerImpl implements SkipListener<Student, StudentT> {

    @Override
    public void onSkipInRead(Throwable t) {
        if (t instanceof FlatFileParseException) {
            createFile("C:/nerio/proyectos/spring-batch/chunkJob/firstChunkStep/reader/skipInRead.txt",
                    ((FlatFileParseException) t).getInput());
        }
    }

    @Override
    public void onSkipInWrite(StudentT item, Throwable t) {
        if (t instanceof NullPointerException) {
            createFile("C:/nerio/proyectos/spring-batch/chunkJob/firstChunkStep/writer/skipInWrite.txt",
                    item.toString());
        }
    }

    @Override
    public void onSkipInProcess(Student item, Throwable t) {
        if (t instanceof NullPointerException) {
            createFile("C:/nerio/proyectos/spring-batch/chunkJob/firstChunkStep/processor/skipInProcess.txt",
                    item.toString());
        }
    }

    public void createFile(String filePath, String data) {
        try (FileWriter fileWriter = new FileWriter(new File(filePath), true)) {
            fileWriter.write(data + "," + LocalDateTime.now() + "\n");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
