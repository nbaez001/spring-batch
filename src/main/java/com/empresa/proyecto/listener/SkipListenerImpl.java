package com.empresa.proyecto.listener;

import com.empresa.proyecto.dto.DeceasedCsv;
import com.empresa.proyecto.entity.Deceased;
import org.springframework.batch.core.SkipListener;
import org.springframework.batch.item.file.FlatFileParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;
import java.io.FileWriter;
import java.time.LocalDateTime;

@Component
public class SkipListenerImpl implements SkipListener<DeceasedCsv, Deceased> {

    @Value("${props.error-dir}")
    private String errorDir;

    @Override
    public void onSkipInRead(Throwable t) {
        if (t instanceof FlatFileParseException) {
            createFile(errorDir + "/reader/skipInRead.txt",
                    ((FlatFileParseException) t).getInput());
        }
    }

    @Override
    public void onSkipInWrite(Deceased item, Throwable t) {
        if (t instanceof NullPointerException) {
            createFile(errorDir + "/writer/skipInWrite.txt",
                    item.toString());
        }
    }

    @Override
    public void onSkipInProcess(DeceasedCsv item, Throwable t) {
        if (t instanceof NullPointerException) {
            createFile(errorDir + "/processor/skipInProcess.txt",
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
