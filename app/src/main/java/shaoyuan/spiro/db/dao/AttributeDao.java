package shaoyuan.spiro.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;
import android.arch.persistence.room.Delete;

import shaoyuan.spiro.db.entity.AttributeEntity;

import java.util.List;

@Dao
public interface AttributeDao {

    @Insert
    void insertAttribute (AttributeEntity attribute);

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    void insertAttributes (List<AttributeEntity> attributeEntities);

    @Query("SELECT * FROM attributes")
    LiveData<List<AttributeEntity>> loadAllAttributes();

    @Query ("SELECT * FROM attributes WHERE uuid = :uuid")
    LiveData<AttributeEntity> loadAttribute (String uuid);

    @Update
    void updateAttribute (AttributeEntity attribute);

    @Delete
    void deleteAttribute (AttributeEntity attribute);
}
