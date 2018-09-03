package shaoyuan.spiro.db;

import shaoyuan.spiro.db.entity.AttributeEntity;
import shaoyuan.spiro.AppUtil;

import java.lang.reflect.Type;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

/**
 * Generates data to pre-populate the database
 */
public class DataGenerator {

    public static List<AttributeEntity> generateAttributes() {
        String data = AppUtil.loadJSONFromAsset("raw/attributes.json");
        Type type = new TypeToken<List<AttributeEntity>>() {
        }.getType();
        List<AttributeEntity> attributes = new Gson().fromJson(data, type);


        return attributes;
    }
}

class AttributeEntityList {
    private List<AttributeEntity> list;

    public List<AttributeEntity> getList() { return list; }
    public void setList(List<AttributeEntity> list) { this.list = list; }
}
