package com.walaatnar.inventory

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper

data class Item(val id: Long, val name: String, val category: String, val unit: String, val stock: Double, val reorder: Double, val price: Double)

class InventoryDb(context: Context) : SQLiteOpenHelper(context, "walaat_nar.db", null, 1) {
    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL("CREATE TABLE app_users(id INTEGER PRIMARY KEY AUTOINCREMENT, username TEXT UNIQUE NOT NULL, full_name TEXT NOT NULL, pin_code TEXT NOT NULL, role TEXT NOT NULL, is_active INTEGER NOT NULL DEFAULT 1)")
        db.execSQL("CREATE TABLE items(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE NOT NULL, category TEXT NOT NULL, unit TEXT NOT NULL, default_price REAL NOT NULL DEFAULT 0, opening_stock REAL NOT NULL DEFAULT 0, reorder_level REAL NOT NULL DEFAULT 0, is_active INTEGER NOT NULL DEFAULT 1)")
        db.execSQL("CREATE TABLE suppliers(id INTEGER PRIMARY KEY AUTOINCREMENT, name TEXT UNIQUE NOT NULL, phone TEXT, notes TEXT)")
        db.execSQL("CREATE TABLE purchases(id TEXT PRIMARY KEY, purchase_date TEXT NOT NULL, item_id INTEGER NOT NULL, supplier_id INTEGER, quantity REAL NOT NULL CHECK(quantity>0), unit_price REAL NOT NULL CHECK(unit_price>=0), payment_method TEXT NOT NULL, invoice_number TEXT, notes TEXT, created_by TEXT, synced INTEGER NOT NULL DEFAULT 0)")
        db.execSQL("CREATE TABLE stock_out(id TEXT PRIMARY KEY, out_date TEXT NOT NULL, item_id INTEGER NOT NULL, movement_type TEXT NOT NULL, quantity REAL NOT NULL CHECK(quantity>0), unit_cost_snapshot REAL NOT NULL DEFAULT 0, recipient_or_reason TEXT, created_by TEXT, synced INTEGER NOT NULL DEFAULT 0)")
        db.execSQL("CREATE INDEX idx_purchases_item ON purchases(item_id)")
        db.execSQL("CREATE INDEX idx_stock_out_item ON stock_out(item_id)")
        seedUsers(db); seedItems(db); seedSuppliers(db)
    }
    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {}

    private fun seedUsers(db: SQLiteDatabase) {
        val rows = listOf(arrayOf("admin", "مدير المطعم", "1234", "مدير النظام"), arrayOf("accountant", "المحاسب", "2222", "محاسب"), arrayOf("storekeeper", "أمين المستودع", "3333", "أمين مستودع"))
        rows.forEach { r -> db.execSQL("INSERT INTO app_users(username,full_name,pin_code,role) VALUES(?,?,?,?)", r) }
    }
    private fun seedItems(db: SQLiteDatabase) {
        val rows = listOf(
            arrayOf("غاز","وقود وطاقة","أسطوانة",0.0,0.0,2.0), arrayOf("تاكسي","نقل وتوصيل","مشوار",0.0,0.0,0.0), arrayOf("فحم","وقود وطاقة","كيس / شوال",0.0,0.0,5.0),
            arrayOf("خبز صاج","مخبوزات","ربطة",0.0,0.0,5.0), arrayOf("خبز بر","مخبوزات","ربطة",0.0,0.0,5.0), arrayOf("خبز برجر","مخبوزات","ربطة",0.0,0.0,5.0),
            arrayOf("شاي","مشروبات وساخن","علبة / كرتون",0.0,0.0,2.0), arrayOf("مشروبات غازية","مشروبات","كرتون",0.0,0.0,5.0), arrayOf("ماء","مشروبات","كرتون",0.0,0.0,5.0),
            arrayOf("خبز باشكا","مخبوزات","ربطة",0.0,0.0,5.0), arrayOf("خس","خضار","كجم",0.0,0.0,5.0), arrayOf("بقدونس","خضار","ربطة",0.0,0.0,5.0),
            arrayOf("بصل أحمر","خضار","كجم",0.0,0.0,10.0), arrayOf("بصل أبيض","خضار","كجم",0.0,0.0,10.0), arrayOf("دجاج","لحوم ودواجن","كرتون / كجم",0.0,0.0,5.0),
            arrayOf("لحوم","لحوم ودواجن","كجم",0.0,0.0,5.0), arrayOf("علب بروستد","تغليف ومستهلكات","كرتون / ربطة",0.0,0.0,3.0), arrayOf("طرشي خيار 4 كغ","مواد غذائية وصوصات","حبة / جالون",0.0,0.0,2.0),
            arrayOf("مسحب سانيا 4*2.5 كغ","مجمدات ودواجن","كرتون",189.0,0.0,2.0), arrayOf("بيبسي دايت علب","مشروبات","كرتون",45.22,0.0,2.0),
            arrayOf("مايونيز نارنا جالون شد 4*3.780 كج","مواد غذائية وصوصات","حبة",23.48,0.0,2.0), arrayOf("كاتشب السعودية جالون 5 كجم شد 4","مواد غذائية وصوصات","حبة",17.18,0.0,2.0),
            arrayOf("بيبسي علب 250*30","مشروبات","كرتون",45.22,0.0,3.0), arrayOf("حمضيات علب 250 مل * 30","مشروبات","كرتون",45.22,0.0,2.0),
            arrayOf("صوص شيدر سائل فرشلي شد 6","مواد غذائية وصوصات","حبة",54.0,0.0,2.0), arrayOf("صوص باربكيو ساسامونتي جالون شد 4","مواد غذائية وصوصات","حبة",43.0,0.0,2.0),
            arrayOf("جبن سمين 900 جرام شد 6","ألبان وأجبان","كرتون",65.22,0.0,2.0), arrayOf("جالون تتبيلة فرنسية 4*3.78 ليتر شيف ديليت","مواد غذائية وصوصات","حبة",43.0,0.0,2.0),
            arrayOf("موزريلا مبشور ليميليزيا بولندي كجم 2.3*4","ألبان وأجبان","حبة",44.0,0.0,2.0), arrayOf("زيت دوار الشمس يارا/ثريا/الدار/ميراسول 17 لتر لا غير","زيوت","تنك",126.0,0.0,2.0),
            arrayOf("دبس فليفلة جولدن تركية شد 4","مواد غذائية وصوصات","حبة",62.0,0.0,1.0), arrayOf("بطاطس جولدن سفن 10*10","مجمدات","كرتون",50.44,0.0,3.0),
            arrayOf("كفوف أسود شد 10*70 لارج","تغليف ومستهلكات","حبة",7.08,0.0,3.0), arrayOf("كفوف أسود شد 10*70 اكس لارج","تغليف ومستهلكات","حبة",7.08,0.0,3.0), arrayOf("قصدير 300 سنكون","تغليف ومستهلكات","كرتون",107.0,0.0,1.0)
        )
        rows.forEach { r -> db.execSQL("INSERT INTO items(name,category,unit,default_price,opening_stock,reorder_level) VALUES(?,?,?,?,?,?)", r) }
    }
    private fun seedSuppliers(db: SQLiteDatabase) {
        listOf("شركة نجمة التوريدات للمواد الغذائية","شركة الغاز والتصنيع","مؤسسة الفحم الفاخر","المخبز الآلي","سوق الخضار المركزي","مؤسسة الدواجن واللحوم","موزع المشروبات الغازية","مؤسسة التغليف والبلاستيك","مورد محلي / نقدي").forEach { db.execSQL("INSERT INTO suppliers(name) VALUES(?)", arrayOf(it)) }
    }
    fun login(user: String, pin: String): Pair<String,String>? {
        readableDatabase.rawQuery("SELECT full_name, role FROM app_users WHERE username=? AND pin_code=? AND is_active=1", arrayOf(user,pin)).use { if(it.moveToFirst()) return it.getString(0) to it.getString(1) }
        return null
    }
    fun items(): List<Item> {
        val result = mutableListOf<Item>()
        readableDatabase.rawQuery("SELECT i.id,i.name,i.category,i.unit,i.opening_stock+COALESCE(p.q,0)-COALESCE(o.q,0),i.reorder_level,i.default_price FROM items i LEFT JOIN (SELECT item_id,SUM(quantity) q FROM purchases GROUP BY item_id) p ON p.item_id=i.id LEFT JOIN (SELECT item_id,SUM(quantity) q FROM stock_out GROUP BY item_id) o ON o.item_id=i.id WHERE i.is_active=1 ORDER BY i.name", null).use { c -> while(c.moveToNext()) result.add(Item(c.getLong(0),c.getString(1),c.getString(2),c.getString(3),c.getDouble(4),c.getDouble(5),c.getDouble(6))) }
        return result
    }
    fun itemCount() = readableDatabase.rawQuery("SELECT COUNT(*) FROM items WHERE is_active=1",null).use { it.moveToFirst(); it.getInt(0) }
    fun lowCount() = items().count { it.stock <= it.reorder }
    fun addPurchase(itemId: Long, qty: Double, price: Double, supplierId: Long?, method: String, invoice: String, user: String) {
        val v=ContentValues().apply { put("id", java.util.UUID.randomUUID().toString()); put("purchase_date", java.time.LocalDate.now().toString()); put("item_id",itemId); if(supplierId!=null) put("supplier_id",supplierId); put("quantity",qty); put("unit_price",price); put("payment_method",method); put("invoice_number",invoice); put("created_by",user) }
        writableDatabase.insertOrThrow("purchases",null,v)
    }
    fun addStockOut(itemId: Long, qty: Double, type: String, reason: String, user: String) {
        val v=ContentValues().apply { put("id",java.util.UUID.randomUUID().toString()); put("out_date",java.time.LocalDate.now().toString()); put("item_id",itemId); put("movement_type",type); put("quantity",qty); put("recipient_or_reason",reason); put("created_by",user) }
        writableDatabase.insertOrThrow("stock_out",null,v)
    }
}
