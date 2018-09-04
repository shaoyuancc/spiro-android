package shaoyuan.spiro.db.entity;

import android.arch.persistence.room.Entity;
import android.arch.persistence.room.PrimaryKey;
import android.arch.persistence.room.ColumnInfo;
import android.support.annotation.NonNull;

import shaoyuan.spiro.model.Attribute;

@Entity (tableName = "attributes")
public class AttributeEntity implements Attribute{
    @PrimaryKey
    @NonNull
    private String uuid;

    @ColumnInfo(name = "name")
    private String name;

    @ColumnInfo(name = "display")
    private String display;

    @ColumnInfo(name = "description")
    private String description;

    public AttributeEntity() {
    }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDisplay() { return display; }
    public void setDisplay(String display) { this.display = display; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

}
