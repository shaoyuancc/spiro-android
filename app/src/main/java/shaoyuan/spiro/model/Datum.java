package shaoyuan.spiro.model;

public interface Datum {
    Integer getId();
    Integer getValue();
    String getDateTime();
    String getUsePeriodUuid();
    String toString();
    void setId(Integer id);
    void setValue(Integer value);
    void setDateTime(String dateTime);
    void setUsePeriodUuid(String usePeriodUuid);
}