package shaoyuan.spiro.model;

public interface Datum {
    Long getId();
    Long getValue();
    String getDateTime();
    String getUsePeriodUuid();
    void setId(Long id);
    void setValue(Long value);
    void setDateTime(String dateTime);
    void setUsePeriodUuid(String usePeriodUuid);
}