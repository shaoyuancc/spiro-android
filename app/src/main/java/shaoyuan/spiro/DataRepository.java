package shaoyuan.spiro;

import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;

import shaoyuan.spiro.db.AppDatabase;
import shaoyuan.spiro.db.entity.AttributeEntity;

import java.util.List;

public class DataRepository {
    private static DataRepository sInstance;

    private final AppDatabase mDatabase;
    private MediatorLiveData<List<AttributeEntity>> mObservableAttributes;

    private DataRepository(final AppDatabase database) {
        mDatabase = database;
        mObservableAttributes = new MediatorLiveData<>();

        mObservableAttributes.addSource(mDatabase.attributeDao().loadAllAttributes(),
                attributeEntities -> {
                    if (mDatabase.getDatabaseCreated().getValue() != null) {
                        mObservableAttributes.postValue(attributeEntities);
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
    public LiveData<List<AttributeEntity>> getAttributes() {
        return mObservableAttributes;
    }

    public LiveData<AttributeEntity> loadAttribute(final String attributeUuid) {
        return mDatabase.attributeDao().loadAttribute(attributeUuid);
    }

}
