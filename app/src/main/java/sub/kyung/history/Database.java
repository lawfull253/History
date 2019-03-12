package sub.kyung.history;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;

public class Database extends SQLiteOpenHelper {
    public Database(Context context) {
        super(context, "history_db", null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE tbl_history(history_idx Integer PRIMARY KEY AUTOINCREMENT," +
                " history_keyword TEXT, history_content TEXT, history_year Integer, history_month Integer, history_day Integer)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    public History selectHistory(Date date){
        History history = new History(-1, null, null, date);
        String[] params = { Integer.toString(date.getYear()),
                Integer.toString(date.getMonth()),
                Integer.toString(date.getDate())
        };

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM tbl_history WHERE history_year=? AND history_month=? AND history_day=?", params);

        if(cursor.moveToNext()){
            history = new History(cursor.getInt(0), cursor.getString(1), cursor.getString(2), new Date(cursor.getInt(3), cursor.getInt(4), cursor.getInt(5)));
        }

        db.close();
        return history;
    }

    public HashSet<Date> selectHistoryDaysFromKey(String key, Date curDate){
        HashSet<Date> days = new HashSet<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT history_year, history_month, history_day FROM tbl_history WHERE history_keyword Like '%" + key + "%'", null);
        //  Log.e("test1", "DB SELECT");
        while(cursor.moveToNext()){
            //  Log.e("test1", "date :: " + new Date(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2)));
            days.add(new Date(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2)));
        }
        Log.e("test1", "search :: " + days.toString());
        db.close();

        return days;
    }

    public List<History> selectHistoryAll(){
        List<History> historyList = new ArrayList<>();

        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM tbl_history", null);
        while (cursor.moveToNext()) {

            historyList.add(new History(cursor.getInt(0), cursor.getString(1), cursor.getString(2), new Date(cursor.getInt(3), cursor.getInt(4), cursor.getInt(5))));
        }

        db.close();

        return historyList;

    }

    public void insertHistory(History history){
        String[] params = {history.getHistoryKeyword(), history.getHistoryContent(), Integer.toString(history.getYear()), Integer.toString(history.getMonth()), Integer.toString(history.getDay())};

        if(selectHistory(history.getHistoryDate()).getHistoryIdx() == -1) {
            SQLiteDatabase db = getWritableDatabase();
            db.execSQL("INSERT INTO tbl_history(history_keyword, history_content, history_year, history_month, history_day)" +
                    " VALUES(?, ?, ?, ?, ?)", params);
            db.close();
        } else {
            updateHistory(history);
        }

    }

    public void updateHistory(History history){
        String[] params = {history.getHistoryKeyword(), history.getHistoryContent(), Integer.toString(history.getYear()), Integer.toString(history.getMonth()), Integer.toString(history.getDay())};

        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE tbl_history SET history_keyword=?, history_content=? WHERE history_year=? AND history_month=? AND history_day=?", params);

        db.close();
    }

    public void deleteHistory(Date date){
        String[] params = {Integer.toString(date.getYear()), Integer.toString(date.getMonth()), Integer.toString(date.getDate())};
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM tbl_history WHERE history_year=? AND history_month=? AND history_day=?", params);

        db.close();
    }

    public void deleteHistoryAll(){
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("DELETE FROM tbl_history");

        db.close();
    }

    public void selectAll(){
        int count = 0;
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM tbl_history", null);
        while(cursor.moveToNext())
            Log.e("test1", String.format("Index : %d, keyword : %s , content : %s, date : %s",cursor.getInt(0), cursor.getString(1), cursor.getString(2), cursor.getInt(3)+","+cursor.getInt(4)+","+ cursor.getInt(5)));
    }
}
