package shaoyuan.spiro.db;

@Entity
public class AttributeEntity {
    @PrimaryKey
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