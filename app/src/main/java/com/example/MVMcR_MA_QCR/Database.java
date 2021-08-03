package com.example.MVMcR_MA_QCR;

/**
 * Created by Smita on 20/10/2019.
 */

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import android.widget.Toast;

/**
 * Created by xxx on 06/03/2017.
 */

public class Database {

    static final String TAG = "DBAdapter";
    static final String DB_NAME = "MM_TCF";
    static final int DB_VERSION = 18;

    static final String Table_ImageReg = "ImageReg";
    static final String Table_ShiftReg = "ShiftReg";
    static final String Table_QCheck = "QCheck";
    static final String Table_Rework = "Rework";
    static final String Table_Threshold = "Threshold";
    static final String Table_MasterTable = "S_Hoseclip_Pokayoke_Master";
    static final String Table_DataTable = "S_Hoseclip_Pokayoke_Data";


    /*------------------------------------------------*/
    static final String ID = "id";
    static final String MODEL_NM = "model_name";;
    static final String IMG = "image";

    /*------------------------------------------------*/

    static final String START_TIME = "start_time";;
    static final String END_TIME = "end_time";

    /*------------------------------------------------*/

    static final String QR_CODE = "qr_code";;
    //static final String MODEL_NAME = "model_name";
    static final String HOSE_CLIP_NO = "hose_clip_no";;
    static final String MASTER_IMG_ID = "master_img_id";
    static final String RESULT = "result";;
    static final String WORK_TIME = "work_time";
    static final String SHIFT = "shift";
    static final String IMAGE_PATH = "image_path";
    static final String SYNC_STATUS = "sync_status";

    /*------------------------------------------------*/

    static final String THRESHOLD_VALUE = "threshold_value";;

    /*------------------------------------------------*/

    static final String DATABASE_CREATE_TABLE_IMAGE_REG = "create table ImageReg (id integer primary key, model_name text, image blob );";

    static final String DATABASE_CREATE_TABLE_SHIFT_REG = "create table ShiftReg (id integer primary key, start_time text, end_time text );";

    static final String DATABASE_CREATE_TABLE_QCHECK = "create table QCheck (id integer primary key, qr_code text, model_name text, hose_clip_no integer, master_img_id integer, result integer, work_time text, shift integer, image_path text, sync_status integer );";

    static final String DATABASE_CREATE_TABLE_REWORK = "create table Rework (id integer primary key autoincrement, qr_code text, model_name text, hose_clip_no integer, master_img_id integer, result integer, work_time text, shift integer );";

    static final String DATABASE_CREATE_TABLE_THRESHOLD = "create table Threshold (id integer primary key, threshold_value text);";

    static final String DATABASE_CREATE_TABLE_MasterTable = "create table S_Hoseclip_Pokayoke_Master (id integer primary key, model_name text, image blob);";

    static final String DATABASE_CREATE_TABLE_DataTable = "create table S_Hoseclip_Pokayoke_Data (id integer primary key, model_name text, qr_code text, hose_clip_no text, master_img_id integer, result text, work_time text, image_path text);";


    final Context context;
    DatabaseHelper DBHelper;
    SQLiteDatabase db;

    public Database(Context ctx)
    {
        this.context = ctx;
        DBHelper = new DatabaseHelper(context);
    }



    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {

            super(context, DB_NAME, null, DB_VERSION);

        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
            try {
                db.execSQL(DATABASE_CREATE_TABLE_IMAGE_REG);
                db.execSQL(DATABASE_CREATE_TABLE_SHIFT_REG);
                db.execSQL(DATABASE_CREATE_TABLE_QCHECK);
                db.execSQL(DATABASE_CREATE_TABLE_REWORK);
                db.execSQL(DATABASE_CREATE_TABLE_THRESHOLD);
                db.execSQL(DATABASE_CREATE_TABLE_MasterTable);
                db.execSQL(DATABASE_CREATE_TABLE_DataTable);
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
        {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS "+Table_ImageReg);
            db.execSQL("DROP TABLE IF EXISTS "+Table_ShiftReg);
            db.execSQL("DROP TABLE IF EXISTS "+Table_QCheck);
            db.execSQL("DROP TABLE IF EXISTS "+Table_Rework);
            db.execSQL("DROP TABLE IF EXISTS "+Table_Threshold);
            db.execSQL("DROP TABLE IF EXISTS "+Table_MasterTable);
            db.execSQL("DROP TABLE IF EXISTS "+Table_DataTable);
            onCreate(db);
        }

    }

    public Database open() throws SQLException {

        db = DBHelper.getWritableDatabase();
        return this;

    }

    public void close(){

        DBHelper.close();

    }

    /*--------------------------------------- Table_ImageReg ---f------------------------------------------ */

    public long addImage(int id, String model_nm , byte[] img)
    {

        ContentValues contentValues = new ContentValues();
        contentValues.put(ID, id);
        contentValues.put(MODEL_NM, model_nm);
        contentValues.put(IMG, img);
        //Toast.makeText(context,"Image Inserted",Toast.LENGTH_SHORT).show();
        return db.insert(Table_ImageReg, null, contentValues);
    }

    public long updateImage(String model_nm , byte[] img, int id)
    {

        ContentValues contentValues = new ContentValues();
        contentValues.put(MODEL_NM, model_nm);
        contentValues.put(IMG, img);
        //Toast.makeText(context,"Image Updated",Toast.LENGTH_SHORT).show();
        return db.update(Table_ImageReg, contentValues,ID+"="+id,null);
    }

    public Cursor getSingleImageOfModel(int id)
    {
        // return db.query(Table_TopicQuestions, new String[] {"phone"}, QTOPICNO+" = '"+topicNo+"'", null, null, null, null);
        return db.query(Table_ImageReg, new String[] {MODEL_NM, IMG}, ID+" = '"+id+"' ", null, null, null, null);
    }

    public Cursor getAllImagesOfSpecificModel(String model_nm)
    {
        // return db.query(Table_Name, new String[] {"phone"}, "id"+" = '"+x+"'", null, null, null, null);
        return db.query(Table_ImageReg, new String[] {ID, IMG}, MODEL_NM+" = '"+model_nm+"' ", null, null, null, null);
    }

    public Cursor getAllModels()
    {
        // return db.query(Table_Name, new String[] {"phone"}, "id"+" = '"+x+"'", null, null, null, null);
        return db.query(Table_ImageReg, new String[] {ID, MODEL_NM, IMG}, null, null, null, null, null);
    }

    public Cursor getMaxImageRegResult(){
        return db.query(Table_ImageReg, new String[] {ID}, null, null, null, null, null);
    }


    public boolean deleteSingleImage(int id)
    {
        // return db.delete(Table_Name,  null, null) > 0;
        //Toast.makeText(context,"Image Deleted",Toast.LENGTH_SHORT).show();
        return db.delete(Table_ImageReg, ID+"="+id, null)>0;
        //  db.delete(Table_Name, "cname = '"+selectUser._cname+"'", null);
        //  db.delete(Table_Name, "phone = '"+selectUser._phone+"'", null);
    }

    public boolean deleteAllImages()
    {
        return db.delete(Table_ImageReg,  null, null) > 0;
        //return db.delete(Table_ImageReg, ID+"="+id, null)>0;
        //  db.delete(Table_Name, "cname = '"+selectUser._cname+"'", null);
        //  db.delete(Table_Name, "phone = '"+selectUser._phone+"'", null);
    }

    /*--------------------------------------- Table_ShiftReg --------------------------------------------- */

    public long addShift(int id, String start , String end)
    {

        ContentValues contentValues = new ContentValues();
        contentValues.put(ID, id);
        contentValues.put(START_TIME, start);
        contentValues.put(END_TIME, end);
        //Toast.makeText(context,"Shift Added",Toast.LENGTH_SHORT).show();
        return db.insert(Table_ShiftReg, null, contentValues);
    }


    public Cursor getAllShifts()
    {
        // return db.query(Table_Name, new String[] {"phone"}, "id"+" = '"+x+"'", null, null, null, null);
        return db.query(Table_ShiftReg, new String[] {ID, START_TIME, END_TIME}, null, null, null, null, null);
    }

    public Cursor getMaxShiftRegResult(){
        return db.query(Table_ShiftReg, new String[] {ID}, null, null, null, null, null);
    }

    public boolean deleteAllShifts()
    {
         return db.delete(Table_ShiftReg,  null, null) > 0;
        //return db.delete(Table_ImageReg, ID+"="+id, null)>0;
        //  db.delete(Table_Name, "cname = '"+selectUser._cname+"'", null);
        //  db.delete(Table_Name, "phone = '"+selectUser._phone+"'", null);
    }

    /*--------------------------------------- Table_QCheck --------------------------------------------- */

    public long addQCheckResult(int id, String qr_code , String model_nm, int hose_clip_no, int master_img_id, int result, String work_time, int shift, String image_path, int sync_status )
    {

        ContentValues contentValues = new ContentValues();
        contentValues.put(ID, id);
        contentValues.put(QR_CODE, qr_code);
        contentValues.put(MODEL_NM, model_nm);
        contentValues.put(HOSE_CLIP_NO, hose_clip_no);
        contentValues.put(MASTER_IMG_ID, master_img_id);
        contentValues.put(RESULT, result);
        contentValues.put(WORK_TIME, work_time);
        contentValues.put(SHIFT, shift);
        contentValues.put(IMAGE_PATH, image_path);
        contentValues.put(SYNC_STATUS, sync_status);

        //Toast.makeText(context,"Qcheck",Toast.LENGTH_SHORT).show();
        return db.insert(Table_QCheck, null, contentValues);
    }

    public long updateSyncQCheckResult(int sync_status, int id)
    {

        ContentValues contentValues = new ContentValues();
        contentValues.put(SYNC_STATUS, sync_status);
        //Toast.makeText(context,"Threshold Updated",Toast.LENGTH_SHORT).show();
        return db.update(Table_QCheck, contentValues,ID+"="+id,null);
    }

    public Cursor getUnsyncQCheckResult(int sync_status)
    {
        // return db.query(Table_Name, new String[] {"phone"}, "id"+" = '"+x+"'", null, null, null, null);
        return db.query(Table_QCheck, new String[] {ID, QR_CODE, MODEL_NM, HOSE_CLIP_NO, MASTER_IMG_ID, RESULT, WORK_TIME, IMAGE_PATH, SHIFT}, SYNC_STATUS+" = '"+sync_status+"' ", null, null, null, null);
    }

    public Cursor getSpecificQCheckResult(String qr_code)
    {
        // return db.query(Table_Name, new String[] {"phone"}, "id"+" = '"+x+"'", null, null, null, null);
        return db.query(Table_QCheck, new String[] {ID, MODEL_NM, HOSE_CLIP_NO, MASTER_IMG_ID, RESULT, WORK_TIME, SHIFT}, QR_CODE+" = '"+qr_code+"' ",  null, null, null, null);
    }

    public Cursor getAllQCheckResult()
    {
        // return db.query(Table_Name, new String[] {"phone"}, "id"+" = '"+x+"'", null, null, null, null);
        return db.query(Table_QCheck, new String[] {ID, QR_CODE, MODEL_NM, HOSE_CLIP_NO, MASTER_IMG_ID, RESULT, WORK_TIME, SHIFT}, null, null, null, null, null);
    }

    public Cursor getMaxIdQCheckResult(){
        return db.query(Table_QCheck, new String[] {ID}, null, null, null, null, null);
    }

    public boolean deleteAllQCheckResult()
    {
        return db.delete(Table_QCheck,  null, null) > 0;
        //return db.delete(Table_ImageReg, ID+"="+id, null)>0;
        //  db.delete(Table_Name, "cname = '"+selectUser._cname+"'", null);
        //  db.delete(Table_Name, "phone = '"+selectUser._phone+"'", null);
    }


    /*--------------------------------------- Table_Rework --------------------------------------------- */

    public long addReworkResult(String qr_code , String model_nm, int hose_clip_no, int master_img_id, int result, String work_time, int shift )
    {

        ContentValues contentValues = new ContentValues();
        contentValues.put(QR_CODE, qr_code);
        contentValues.put(MODEL_NM, model_nm);
        contentValues.put(HOSE_CLIP_NO, hose_clip_no);
        contentValues.put(MASTER_IMG_ID, master_img_id);
        contentValues.put(RESULT, result);
        contentValues.put(WORK_TIME, work_time);
        contentValues.put(SHIFT, shift);

        Toast.makeText(context,"Hii",Toast.LENGTH_SHORT).show();
        return db.insert(Table_Rework, null, contentValues);
    }

    public Cursor getAllReworkResult()
    {
        // return db.query(Table_Name, new String[] {"phone"}, "id"+" = '"+x+"'", null, null, null, null);
        return db.query(Table_Rework, new String[] {ID, QR_CODE, MODEL_NM, HOSE_CLIP_NO, MASTER_IMG_ID, RESULT, WORK_TIME, SHIFT}, null, null, null, null, null);
    }

    public Cursor getSpecificReworkResult(String qr_code)
    {
        // return db.query(Table_Name, new String[] {"phone"}, "id"+" = '"+x+"'", null, null, null, null);
        return db.query(Table_Rework, new String[] {ID, MODEL_NM, HOSE_CLIP_NO, MASTER_IMG_ID, RESULT, WORK_TIME, SHIFT}, QR_CODE+" = '"+qr_code+"' ", null, null, null, null);
    }

    /*--------------------------------------- Table_Threshold --------------------------------------------- */

    public long addThreshod(int id, String threshold)
    {

        ContentValues contentValues = new ContentValues();
        contentValues.put(ID, id);
        contentValues.put(THRESHOLD_VALUE, threshold);
        //Toast.makeText(context,"Threshold Inserted",Toast.LENGTH_SHORT).show();
        return db.insert(Table_Threshold, null, contentValues);
    }

    public long updateThreshod(String threshold, int id)
    {

        ContentValues contentValues = new ContentValues();
        contentValues.put(THRESHOLD_VALUE, threshold);
        //Toast.makeText(context,"Threshold Updated",Toast.LENGTH_SHORT).show();
        return db.update(Table_Threshold, contentValues,ID+"="+id,null);
    }

    public Cursor getThreshod()
    {
        // return db.query(Table_Name, new String[] {"phone"}, "id"+" = '"+x+"'", null, null, null, null);
        return db.query(Table_Threshold, new String[] {ID, THRESHOLD_VALUE}, null, null, null, null, null);
    }




}
