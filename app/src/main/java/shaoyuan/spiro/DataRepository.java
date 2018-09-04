package shaoyuan.spiro;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;

import shaoyuan.spiro.db.AppDatabase;
import shaoyuan.spiro.db.entity.AttributeEntity;
import shaoyuan.spiro.db.entity.DatumEntity;

import java.util.List;

public class DataRepository {
    private static DataRepository sInstance;

    private final AppDatabase mDatabase;
    private MediatorLiveData<List<DatumEntity>> mObservableData;

    private DataRepository(final AppDatabase database) {
        mDatabase = database;
        mObservableData = new MediatorLiveData<>();

        mObservableData.addSource(mDatabase.datumDao().loadAllData(),
                datumEntities -> {
                    if (mDatabase.getDatabaseCreated().getValue() != null) {
                        mObservableData.postValue(datumEntities);
                    }
                });
    }

    public static DataRepository getInstance(final AppDatabase database) {
        if (sInstance == null) {
            synchronized (DataRepository.class) {
                if (sInstance == null) {
                    sInstance = new DataRepository(database);
                }
            }
        }
        return sInstance;
    }

    /**
     * Get the list of attributes from the database and get notified when the data changes.
     */
    public LiveData<List<DatumEntity>> getData() {
        return mObservableData;
    }

    public LiveData<DatumEntity> loadDatum(final Long id) {
        return mDatabase.datumDao().loadDatum(id);
    }

    public LiveData<AttributeEntity> loadAttribute(final String attributeUuid) {
        return mDatabase.attributeDao().loadAttribute(attributeUuid);
    }

}
