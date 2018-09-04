package shaoyuan.spiro;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import shaoyuan.spiro.db.AppDatabase;
import shaoyuan.spiro.db.dao.DatumDao;
import shaoyuan.spiro.db.entity.DatumEntity;

import static org.hamcrest.Matchers.equalTo;

@RunWith(AndroidJUnit4.class)
public class dbTest {
    private DatumDao mDatumDao;
    private AppDatabase mDb;

    @Before
    public void createDb() {
        Context context = InstrumentationRegistry.getTargetContext();
        mDb = Room.inMemoryDatabaseBuilder(context, AppDatabase.class).build();
        mDatumDao = mDb.datumDao();
    }

    @After
    public void closeDb() throws IOException {
        mDb.close();
    }

    @Test
    public void writeDatumAndReadInList() throws Exception {
        DatumEntity datum = new DatumEntity();
        datum.setId(1L);
        datum.setDateTime("monday");
        datum.setUsePeriodUuid("AAA");
        datum.setValue(5L);
        mDatumDao.insertDatum(datum);
        DatumEntity byId= mDatumDao.loadDatum(1L);
        Assert.assertThat(byId.getDateTime(), equalTo(datum.getDateTime()));
    }
}