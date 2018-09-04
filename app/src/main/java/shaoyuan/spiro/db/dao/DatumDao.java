package shaoyuan.spiro.db.dao;

import android.arch.lifecycle.LiveData;
import android.arch.persistence.room.Dao;
import android.arch.persistence.room.Delete;
import android.arch.persistence.room.Insert;
import android.arch.persistence.room.OnConflictStrategy;
import android.arch.persistence.room.Query;
import android.arch.persistence.room.Update;

import java.util.List;

import shaoyuan.spiro.db.entity.DatumEntity;

@Dao
public interface DatumDao {
    @Insert
    void insertDatum(DatumEntity datum);

    @Insert (onConflict = OnConflictStrategy.REPLACE)
    void insertData(List<DatumEntity> data);

    @Query("SELECT * FROM data")
    LiveData<List<DatumEntity>> loadAllData();

    @Query ("SELECT * FROM data WHERE id = :id")
    LiveData<DatumEntity> loadDatum(Long id);

    @Update
    void updateDatum(DatumEntity datum);

    @Delete
    void deleteDatum(DatumEntity datum);
}
