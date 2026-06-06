package lk.gov.health.phsp.enums;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

@Converter
public class PrivilegeConverter implements AttributeConverter<Privilege, String> {

    @Override
    public String convertToDatabaseColumn(Privilege privilege) {
        return privilege == null ? null : privilege.name();
    }

    @Override
    public Privilege convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }

        String privilegeName = value.trim();
        if (privilegeName.isEmpty()) {
            return null;
        }

        for (Privilege privilege : Privilege.values()) {
            if (privilege.name().equalsIgnoreCase(privilegeName)
                    || privilege.getLabel().equalsIgnoreCase(privilegeName)) {
                return privilege;
            }
        }

        return null;
    }
}
