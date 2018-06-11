package com.alejandro_castilla.heartratetest.data;


import com.alejandro_castilla.heartratetest.data.local.LocalDataSource;
import com.alejandro_castilla.heartratetest.data.pref.Pref;
import com.alejandro_castilla.heartratetest.data.remote.RemoteDataSource;

/**
 * Created by yasar on 6/3/18.
 */

public interface DataSource extends RemoteDataSource, LocalDataSource, Pref {


}
