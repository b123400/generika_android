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
package org.oddb.generika.model;

import android.util.Log;

import io.realm.annotations.PrimaryKey;
import io.realm.annotations.LinkingObjects;
import io.realm.Realm;
import io.realm.RealmObject;
import io.realm.RealmList;
import io.realm.RealmResults;

import java.lang.String;
import java.lang.IllegalAccessException;
import java.lang.IllegalArgumentException;
import java.lang.NoSuchMethodException;
import java.lang.SecurityException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.HashMap;


public class ProductItem extends RealmObject {
  public static final String FIELD_ID = "id";

  private static AtomicInteger INTEGER_COUNTER = new AtomicInteger(0);

  @PrimaryKey
  private long id;

  @LinkingObjects("items")
  private final RealmResults<Product> source = null;

  private String ean;
  // parsed partial numbers from EAN13 (ean)
  private String reg;
  private String seq;
  private String pack;

  // scanned_at/imported_at
  private String datetime;

  // -- scanner product item (scanner medications)
  private String barcode;
  private String expiresAt;  // TODO: valdatum
  // (values from oddb)
  private String name;
  private String size;
  private String deduction;
  private String price;
  private String category;

  // TODO: receipt (amk)
  // -- receipt product item (receipt medications)
  // (values from receipt)
  // * prescription_hash
  // * title
  // * comment
  // * atc
  // * owner

  private static int increment() {
    return INTEGER_COUNTER.getAndIncrement();
  }

  public long getId() { return id; }
  public void setId(long id) { this.id = id; }

  public RealmResults<Product> getSource() { return source; }

  public String getEan() { return ean; }
  public void setEan(String ean) { this.ean = ean; }

  public String getReg() { return reg; }
  public String getSeq() { return seq; }
  public String getPack() { return pack; }

  public String getDatetime() { return datetime; }
  public void setDatetime(String datetime) { this.datetime = datetime; }

  public String getBarcode() { return barcode; }
  public void setBarcode(String barcode) { this.barcode = barcode; }

  // (values from oddb)
  public String getName() { return name; }
  public void setName(String name) { this.name = name; }

  public String getSize() { return size; }
  public void setSize(String size) { this.size = size; }

  public String getDeduction() { return deduction; }
  public void setDeduction(String deduction) { this.deduction = deduction; }

  public String getPrice() { return price; }
  public void setPrice(String price) { this.price = price; }

  public String getCategory() {
    // It seems that api response contains '&nbsp;' as white space.
    if (category != null) {
      return category.replace("&nbsp;", " ");
    }
    return category;
  }
  public void setCategory(String category) { this.category = category; }

  public String getCountString() {
    return Long.toString(id);
  }

  public void updateProperties(HashMap<String, String> properties) throws
    SecurityException, NoSuchMethodException, IllegalArgumentException,
    IllegalAccessException, InvocationTargetException {
    // TODO: validate format etc.
    String[] keys = {"name", "size", "deduction", "price", "category"};
    // this.getClass() fails :'(
    Class c = ProductItem.class;
    Class[] parameterTypes = new Class[]{String.class};
    for (String key: keys) {
      String value = properties.get(key);
      if (value != null && value != "" && value != "null")  {
        String methodName = "set" +
          key.substring(0, 1).toUpperCase() + key.substring(1);
        Method method = c.getDeclaredMethod(methodName, parameterTypes);
        method.invoke(this, new Object[]{value});
      }
    }
  }

  // converts as message to application user
  public String toMessage() {
    String unit = "CHF";
    String priceString = "";  // default empty string if price value is unknown
    if (price != null && !price.contains("null")) {
      priceString = String.format("%s: %s", unit, price);
    }
    String name = getName();
    if (name == null) { name = ""; }
    String size = getSize();
    if (size == null) { size = ""; }
    return String.format("%s,\n%s\n%s", name, size, priceString);
  }

  // must be called in transaction
  public static ProductItem createWithEanIntoSource(
    Realm realm, String ean, Product product) {

    RealmList<ProductItem> items = product.getItems();
    ProductItem item = realm.createObject(ProductItem.class, increment());
    item.setEan(ean);
    items.add(item);
    return item;
  }
}