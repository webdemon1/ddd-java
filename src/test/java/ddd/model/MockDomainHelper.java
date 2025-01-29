package ddd.model;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import ddd.infrastructure.DomainHelper;
import ddd.infrastructure.Timestamper;
import ddd.infrastructure.uid.IdGenerator;

public class MockDomainHelper implements DomainHelper {

    private Map<String, String> settingMap = new HashMap<>();

    public Timestamper time() {
        return new Timestamper(LocalDate.of(2014, 11, 18));
    }

    public IdGenerator uid() {
        return new IdGenerator();
    }

    public MockDomainHelper setting(String id, String value) {
        settingMap.put(id, value);
        return this;
    }

}
