package com.empresa.proyecto.processor;

import com.empresa.proyecto.dto.DeceasedCsv;
import com.empresa.proyecto.entity.Deceased;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DeceasedItemProcessor implements ItemProcessor<DeceasedCsv, Deceased> {

    @Override
    public Deceased process(DeceasedCsv item) {
        return new Deceased(null,
                item.getNationalId(),
                item.getFirstName(),
                item.getPaternalLastName(),
                item.getMaternalLastName(),
                LocalDate.parse(item.getDeathDate()),
                Integer.parseInt(item.getAge()),
                item.getGenre(),
                item.getDeathPlace(),
                item.getSource(),
                item.getCause(),
                item.getGeoCode(),
                item.getMarital());
    }
}