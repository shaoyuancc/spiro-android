/*
 * Copyright 2017, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package shaoyuan.spiro.viewmodel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MediatorLiveData;

import shaoyuan.spiro.BasicApp;
import shaoyuan.spiro.db.entity.DatumEntity;

import java.util.List;

public class DataListViewModel extends AndroidViewModel {

    // MediatorLiveData can observe other LiveData objects and react on their emissions.
    private final MediatorLiveData<List<DatumEntity>> mObservableData;

    public DataListViewModel(Application application) {
        super(application);

        mObservableData = new MediatorLiveData<>();
        // set by default null, until we get data from the database.
        mObservableData.setValue(null);

        LiveData<List<DatumEntity>> data = ((BasicApp) application).getRepository()
                .getData();

        // observe the changes of the products from the database and forward them
        mObservableData.addSource(data, mObservableData::setValue);
    }

    /**
     * Expose the LiveData Products query so the UI can observe it.
     */
    public LiveData<List<DatumEntity>> getData() {
        return mObservableData;
    }
}
