package shaoyuan.spiro.db;

import shaoyuan.spiro.db.entity.AttributeEntity;
import shaoyuan.spiro.AppUtil;
import shaoyuan.spiro.db.entity.DatumEntity;

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

    public static List<DatumEntity> generateData() {
        String data = AppUtil.loadJSONFromAsset("raw/testdata.json");
        Type type = new TypeToken<List<DatumEntity>>() {
        }.getType();
        List<DatumEntity> datumList = new Gson().fromJson(data, type);

        return datumList;
    }
}