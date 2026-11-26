package com.walaatnar.inventory

import android.app.Activity
import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*
import java.text.DecimalFormat

class MainActivity : Activity() {
    private lateinit var db: InventoryDb
    private var currentUser = ""
    private var currentName = ""
    private var currentRole = ""
    private val fmt = DecimalFormat("#,##0.##")
    private val navy = 0xFF172B4D.toInt(); private val orange = 0xFFE87524.toInt(); private val bg = 0xFFF4F6FA.toInt()

    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); db=InventoryDb(this); showLogin() }
    private fun base(): LinearLayout = LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(22,22,22,22); setBackgroundColor(bg); layoutDirection=View.LAYOUT_DIRECTION_RTL }
    private fun txt(s:String,size:Float=16f,bold:Boolean=false,color:Int=navy)=TextView(this).apply { text=s; textSize=size; setTextColor(color); if(bold) setTypeface(null,1); gravity=Gravity.RIGHT; setPadding(4,8,4,8) }
    private fun btn(s:String, action:()->Unit)=Button(this).apply { text=s; isAllCaps=false; setTextColor(-1); setBackgroundColor(orange); setOnClickListener{action()} }
    private fun field(hint:String, type:Int=InputType.TYPE_CLASS_TEXT)=EditText(this).apply { this.hint=hint; inputType=type; textSize=16f; setSingleLine(true); gravity=Gravity.RIGHT }
    private fun page(title:String):LinearLayout { val root=base(); root.addView(txt("ولعت نار",14f,true,orange)); root.addView(txt(title,25f,true)); return root }
    private fun showLogin() {
        val root=page("تسجيل الدخول"); root.gravity=Gravity.CENTER
        val user=field("اسم المستخدم"); val pin=field("الرقم السري",InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD)
        root.addView(user); root.addView(pin); root.addView(btn("دخول") { val r=db.login(user.text.toString().trim(),pin.text.toString()); if(r==null) toast("بيانات الدخول غير صحيحة") else { currentUser=user.text.toString().trim(); currentName=r.first; currentRole=r.second; showHome() } })
        root.addView(txt("دخول تجريبي: admin / 1234",13f,false,0xFF667085.toInt())); setContentView(root)
    }
    private fun showHome() {
        val root=page("لوحة التحكم"); root.addView(txt("مرحباً، $currentName",16f)); root.addView(txt("الصلاحية: $currentRole",13f,false,0xFF667085.toInt()))
        val items=db.items(); val low=items.count{it.stock<=it.reorder}
        root.addView(txt("ملخص المستودع",18f,true)); root.addView(txt("عدد الأصناف: ${db.itemCount()}   |   أصناف تحتاج طلب: $low",17f,true))
        root.addView(txt("إجمالي الكميات المتاحة: ${fmt.format(items.sumOf{it.stock})}",16f))
        root.addView(btn("الأصناف والمخزون") { showInventory() })
        if(currentRole=="مدير النظام" || currentRole=="محاسب") root.addView(btn("تسجيل مشتريات") { showPurchase() })
        if(currentRole=="مدير النظام" || currentRole=="أمين مستودع") root.addView(btn("صرف مواد من المستودع") { showStockOut() })
        root.addView(btn("تحديث البيانات المعروضة") { showHome() }); root.addView(btn("تسجيل الخروج") { showLogin() })
        root.addView(txt("وضع العمل: محلي دون إنترنت • المزامنة السحابية تحتاج إعداد اتصال Supabase",12f,false,0xFF667085.toInt()))
        setContentView(root)
    }
    private fun showInventory() {
        val root=page("المخزون الحالي"); val search=field("ابحث عن صنف")
        val list=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
        fun fill(q:String="") { list.removeAllViews(); db.items().filter{it.name.contains(q,true)||it.category.contains(q,true)}.forEach { item ->
            val color=if(item.stock<=item.reorder) 0xFFB42318.toInt() else navy
            list.addView(txt("${item.name}\n${item.category} • ${item.unit}\nالرصيد: ${fmt.format(item.stock)}  |  حد الطلب: ${fmt.format(item.reorder)}",15f,true,color))
            val line=View(this).apply{setBackgroundColor(0xFFDDE2EA.toInt())}; list.addView(line,LinearLayout.LayoutParams(-1,1))
        } }
        search.addTextChangedListener(SimpleWatcher{fill(it)}); root.addView(search); fill()
        root.addView(btn("رجوع") { showHome() }); val scroll=ScrollView(this); scroll.addView(list); root.addView(scroll,LinearLayout.LayoutParams(-1,0,1f)); setContentView(root)
    }
    private fun itemPicker(callback:(Item)->Unit) {
        val data=db.items(); AlertDialog.Builder(this).setTitle("اختر الصنف").setItems(data.map{it.name}.toTypedArray()){_,which->callback(data[which])}.show()
    }
    private fun showPurchase() {
        var selected:Item?=null
        val root=page("تسجيل فاتورة شراء"); val chooseText=txt("لم يتم اختيار صنف"); val choose=btn("اختيار الصنف") { itemPicker { selected=it; chooseText.text="الصنف: ${it.name}" } }
         val qty=field("الكمية",InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL); val price=field("سعر الوحدة قبل الضريبة",InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL); val invoice=field("رقم الفاتورة"); val method=field("طريقة الدفع (نقداً / شبكة / آجل)")
        root.addView(choose); root.addView(chooseText); root.addView(qty); root.addView(price); root.addView(invoice); root.addView(method)
        root.addView(btn("حفظ المشتريات") { val q=qty.text.toString().toDoubleOrNull(); val p=price.text.toString().toDoubleOrNull(); if(selected==null||q==null||q<=0||p==null||p<0) toast("اختر الصنف وأدخل كمية وسعراً صحيحين") else { db.addPurchase(selected!!.id,q,p,null,method.text.toString().ifBlank{"غير محدد"},invoice.text.toString(),currentUser); toast("تم حفظ الشراء • الإجمالي شامل الضريبة: ${fmt.format(q*p*1.15)} ر.س"); showHome() } })
        root.addView(btn("إلغاء") { showHome() }); setContentView(root)
    }
    private fun showStockOut() {
        var selected:Item?=null
        val root=page("صرف مواد من المستودع"); val chooseText=txt("لم يتم اختيار صنف"); root.addView(btn("اختيار الصنف") { itemPicker { selected=it; chooseText.text="الصنف: ${it.name} • الرصيد: ${fmt.format(it.stock)}" } }); root.addView(chooseText)
        val qty=field("الكمية المصروفة",InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL); val type=field("نوع الحركة (استهلاك / تالف / تحويل)"); val reason=field("المستلم أو سبب الصرف"); root.addView(qty); root.addView(type); root.addView(reason)
        root.addView(btn("حفظ الصرف") { val q=qty.text.toString().toDoubleOrNull(); if(selected==null||q==null||q<=0) toast("اختر الصنف وأدخل كمية صحيحة") else if(q>selected!!.stock) toast("الكمية أكبر من الرصيد المتاح") else { db.addStockOut(selected!!.id,q,type.text.toString().ifBlank{"استهلاك"},reason.text.toString(),currentUser); toast("تم تسجيل الصرف"); showHome() } })
        root.addView(btn("إلغاء") { showHome() }); setContentView(root)
    }
    private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_LONG).show()
}

private class SimpleWatcher(val changed:(String)->Unit): android.text.TextWatcher {
    override fun beforeTextChanged(s:CharSequence?,start:Int,count:Int,after:Int) {}
    override fun onTextChanged(s:CharSequence?,start:Int,before:Int,count:Int) { changed(s?.toString().orEmpty()) }
    override fun afterTextChanged(s:android.text.Editable?) {}
}
