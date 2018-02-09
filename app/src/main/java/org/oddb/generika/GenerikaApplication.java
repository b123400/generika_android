/*
 *  Generika Android
 *  Copyright (C) 2018 ywesee GmbH
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package org.oddb.generika;

import android.app.Application;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.RealmConfiguration;

import org.oddb.generika.model.Product;


public class GenerikaApplication extends Application {

  @Override
  public void onCreate() {
    super.onCreate();

    Realm.init(this);
    RealmConfiguration realmConfig = new RealmConfiguration.Builder()
      .initialData(new Realm.Transaction() {
        @Override
        public void execute(Realm realm) {
          RealmResults<Product> products = realm.where(Product.class)
            .findAll();
          if (products.size() != 2) {
            // by self / via scanner
            Product p0 = realm.createObject(Product.class);
            p0.setSourceType("scanner");

            // by doctor, pharmacy (operator) / via receipt
            Product p1 = realm.createObject(Product.class);
            p1.setSourceType("receipt");
          }
        }
      })
      .build();

    // TODO: remove (delete all at restart)
    Realm.deleteRealm(realmConfig);

    Realm.setDefaultConfiguration(realmConfig);
  }
}
