package shaoyuan.spiro.db.entity;

import android.arch.persistence.room.ColumnInfo;
import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.support.annotation.NonNull;

import shaoyuan.spiro.model.Datum;

@Entity (tableName = "data")
public class DatumEntity implements Datum {
    @PrimaryKey
    @NonNull
    private Long id;

    @ColumnInfo(name = "value")
    private Long value;

    @ColumnInfo(name = "dateTime")
    private String dateTime;

    @ColumnInfo(name = "usePeriodUuid")
    private String usePeriodUuid;

    public DatumEntity() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getValue() { return value; }
    public void setValue(Long value) { this.value = value; }
    public String getDateTime() { return dateTime; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }
    public String getUsePeriodUuid() { return usePeriodUuid; }
    public void setUsePeriodUuid(String usePeriodUuid) { this.usePeriodUuid = usePeriodUuid; }

}
