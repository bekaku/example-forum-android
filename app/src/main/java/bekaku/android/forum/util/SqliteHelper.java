package bekaku.android.forum.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.util.LinkedList;
import java.util.List;

import bekaku.android.forum.model.ForumSetting;

public class SqliteHelper extends SQLiteOpenHelper {

    private final String TAG = getClass().getSimpleName();
    private static final String databaseName = "android_forum";
    private static final String tableSetting = "forum_setting";
    private static final int version = 1;

    private SQLiteDatabase sqLiteDatabase;
    private Context c;

    public SqliteHelper(Context context) {
        super(context, databaseName, null, version);
        this.c = context;
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        createTableBy(sqLiteDatabase,getCreateTableSettingQery());
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

    }
    private void createTableBy(SQLiteDatabase db, String creatQuery){

        db.execSQL(creatQuery);
        Log.i(TAG, creatQuery);
    }


    //table forum_setting
    private static String getCreateTableSettingQery(){

        String createTableString = "CREATE TABLE IF NOT EXISTS ";
        return String.format(createTableString+" %s " +
                        "(%s INTEGER PRIMARY KEY  AUTOINCREMENT"//0 int id
                        + ", %s INTEGER"//1 int user_id
                        + ", %s TEXT"//2 String username
                        + ", %s TEXT"//3 String picture
                        + ", %s TEXT"//4 String email
                        + ", %s TEXT"//5 String server_api
                        + ", %s TEXT"//6 String created
                        + ")",
                SqliteHelper.tableSetting
                ,"id"
                ,"user_id"
                ,"username"
                ,"picture"
                ,"email"
                ,"server_api"
                ,"created"
        );
    }
    public long createNewForumSetting(ForumSetting forumSetting){

        this.sqLiteDatabase = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("user_id", forumSetting.getUserId());
        values.put("username", forumSetting.getUserId());
        values.put("picture", forumSetting.getPicture());
        values.put("email", forumSetting.getEmail());
        values.put("server_api", forumSetting.getServerApi());
        values.put("created", forumSetting.getCreated());

        long lastestId  = sqLiteDatabase.insert(tableSetting, null, values);
        sqLiteDatabase.close();
        return lastestId;

    }
    public int updateForumSetting(ForumSetting forumSetting){

        sqLiteDatabase = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put("user_id", forumSetting.getUserId());
        values.put("username", forumSetting.getUsername());
        values.put("picture", forumSetting.getPicture());
        values.put("email", forumSetting.getEmail());
        values.put("server_api", forumSetting.getServerApi());
        values.put("created", forumSetting.getCreated());

        String whereClause = " id = ? ";
        String[] whereArgs = new String[] {String.valueOf(forumSetting.getId())};
        int effectRow = sqLiteDatabase.update(tableSetting, values, whereClause, whereArgs);
        sqLiteDatabase.close();

        return effectRow;
    }
    public long  deleteForumSetting(String id) {

        sqLiteDatabase = this.getWritableDatabase();

        String whereClause = " id = ? ";
        String[] whereArgs = new String[] {id};
        long effectRow = sqLiteDatabase.delete(tableSetting, whereClause, whereArgs);
        sqLiteDatabase.close();
        return effectRow;
    }

    public ForumSetting findCurrentSetting(){

        sqLiteDatabase = this.getReadableDatabase();

        ForumSetting setting = null;
        String[] tableColumns = new String[] {"*"};
        String whereClause = "";
        String[] whereArgs = new String[] {};
        String orderBy = "";
        Cursor c = sqLiteDatabase.query(tableSetting, tableColumns, whereClause, whereArgs, null, null, orderBy);

        try {
            if (c != null && c.moveToFirst()) {
                c.moveToFirst();

                setting = new ForumSetting();

                setting.setId(c.getLong(0));
                setting.setUserId(c.getInt(1));
                setting.setUsername(c.getString(2));
                setting.setPicture(c.getString(3));
                setting.setEmail(c.getString(4));
                setting.setServerApi(c.getString(5));
                setting.setCreated(c.getString(6));

            }
            if(c != null && !c.isClosed()){
                c.close();
            }
        }finally {
            sqLiteDatabase.close();
        }

        return setting;
    }
}
