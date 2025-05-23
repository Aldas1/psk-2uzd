package com.university.converter;

import com.university.mybatis.entity.FacultyMB;
import com.university.service.FacultyService;
import jakarta.enterprise.context.ApplicationScoped;
import jakarta.faces.component.UIComponent;
import jakarta.faces.context.FacesContext;
import jakarta.faces.convert.Converter;
import jakarta.faces.convert.FacesConverter;
import jakarta.inject.Inject;
import jakarta.inject.Named;

@Named
@ApplicationScoped
@FacesConverter(value = "facultyMyBatisConverter", managed = true)
public class FacultyMyBatisConverter implements Converter<FacultyMB> {

    @Inject
    private FacultyService facultyService;

    @Override
    public FacultyMB getAsObject(FacesContext context, UIComponent component, String value) {
        if (value == null || value.isEmpty()) {
            return null;
        }

        try {
            Long id = Long.valueOf(value);
            return facultyService.getFacultyByIdMyBatis(id);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext context, UIComponent component, FacultyMB faculty) {
        if (faculty == null || faculty.getId() == null) {
            return "";
        }
        return faculty.getId().toString();
    }
}