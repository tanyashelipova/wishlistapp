package ru.wishlistapp.wishlist.anotherStuff;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.List;

import ru.wishlistapp.wishlist.giftPost.GiftItem;
import ru.wishlistapp.wishlist.model.wishmodel.WishModel;

public class DataBase extends SQLiteOpenHelper {
    private static final String TAG = "DB";
    private static final int DATABASE_VERSION = 3;
    private static final String DATABASE_NAME = "DataBase";
    private static final String TABLE_WISHES = "Wishes";
    private static final String TABLE_USERS = "Users";

    private static final String WISH_ID = "id";
    private static final String WISH_TITLE = "title";
    private static final String WISH_DESCRIPTION = "description";
    private static final String WISH_DATE = "date";
    private static final String WISH_STATUS = "status";
    private static final String WISH_IMAGE_LINK = "imageLink";

    private static final String USER_ID = "id";
    private static final String USER_ID_SERVER = "id_server";
    private static final String USER_NAME = "name";
    private static final String USER_PASSWORD = "password";
    private static final String USER_USERNAME = "username";
    private static final String USER_EMAIL = "email";
    private static final String USER_BDAY = "bday";
    private static final String USER_IMAGE_LINK = "imageLink";
    private static final String USER_IMAGE_HASH_DELETE = "imageHashDelete";

    public DataBase(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String CREATE_WISHES_TABLE = "CREATE TABLE " + TABLE_WISHES + "("
                + WISH_ID + " INTEGER PRIMARY KEY," + WISH_TITLE + " TEXT," + WISH_DESCRIPTION + " TEXT,"
                + WISH_DATE + " TEXT," +
                WISH_STATUS + " INTEGER," + WISH_IMAGE_LINK + " TEXT" + ");";
        db.execSQL(CREATE_WISHES_TABLE);

        String CREATE_USERS_TABLE = "CREATE TABLE " + TABLE_USERS + "("
                + USER_ID + " INTEGER PRIMARY KEY,"
                + USER_ID_SERVER + " TEXT,"
                + USER_NAME + " TEXT," + USER_PASSWORD + " TEXT," + USER_USERNAME + " TEXT,"
                + USER_EMAIL + " TEXT," + USER_BDAY + " TEXT,"
                + USER_IMAGE_LINK + " TEXT," + USER_IMAGE_HASH_DELETE + " TEXT);";
        db.execSQL(CREATE_USERS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WISHES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
    }

    public void addWish(WishModel wishModel) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(WISH_TITLE, wishModel.getTitle());
        values.put(WISH_DESCRIPTION, wishModel.getContent());
        values.put(WISH_DATE, wishModel.getCreatedDate());
        int status = 0;
        if (wishModel.getIsReceived()) status = 1;
        values.put(WISH_STATUS, status);
        values.put(WISH_IMAGE_LINK, wishModel.getLink());
        long id = db.insert(TABLE_WISHES, null, values);
        Log.d(TAG, "inserted: " + id + " " + wishModel.getId());
        db.close();
    }

    public List<WishModel> getAllWishes() {
        List<WishModel> wishes = new ArrayList<WishModel>();
        String selectQuery = "SELECT * FROM " + TABLE_WISHES;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                String id = cursor.getString(0);
                String title = cursor.getString(1);
                String description = cursor.getString(2);
                String date = cursor.getString(3);
                int statusInt = cursor.getInt(4);
                boolean status = true;
                if (statusInt == 0) status = false;
                String imageLink = cursor.getString(5);

                WishModel wishModel = new WishModel();
                wishModel.setTitle(title);
                wishModel.setContent(description);
                wishModel.setCreatedDate(date);
                wishModel.setIsReceived(status);
                wishModel.setId(id);
                wishModel.setImageLink(imageLink);
                wishes.add(0, wishModel);
            } while (cursor.moveToNext());
        }
        return wishes;
    }

    public void deleteAllWishes() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_WISHES, null, null);
        db.close();
    }

    public void addUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USER_ID_SERVER, user.id_server);
        values.put(USER_NAME, user.name);
        values.put(USER_PASSWORD, user.password);
        values.put(USER_USERNAME, user.username);
        values.put(USER_EMAIL, user.email);
        values.put(USER_BDAY, user.bday);
        values.put(USER_IMAGE_LINK, user.imageLink);
        values.put(USER_IMAGE_HASH_DELETE, user.imageHashDelete);
        long id = db.insert(TABLE_USERS, null, values);
        db.close();
    }

    public List<User> getAllUsers() {
        List<User> users = new ArrayList<User>();
        String selectQuery = "SELECT * FROM " + TABLE_USERS;
        SQLiteDatabase db = this.getWritableDatabase();
        Cursor cursor = db.rawQuery(selectQuery, null);
        if (cursor.moveToFirst()) {
            do {
                int id = Integer.parseInt(cursor.getString(0));
                String id_server = cursor.getString(1);
                String name = cursor.getString(2);
                String password = cursor.getString(3);
                String username = cursor.getString(4);
                String email = cursor.getString(5);
                String bday = cursor.getString(6);
                String imageLink = cursor.getString(7);
                String imageHashDelete = cursor.getString(8);

                User user = new User();
                user.id_server = id_server;
                user.name = name;
                user.password = password;
                user.username = username;
                user.email = email;
                user.bday = bday;
                user.imageLink = imageLink;
                user.imageHashDelete = imageHashDelete;

                users.add(user);
            } while (cursor.moveToNext());
        }
        return users;
    }

    public void updateUser(User user) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(USER_ID_SERVER, user.id_server);
        values.put(USER_NAME, user.name);
        Log.i(TAG, user.name);
        values.put(USER_USERNAME, user.username);
        values.put(USER_PASSWORD, user.password);
        values.put(USER_EMAIL, user.email);
        values.put(USER_BDAY, user.bday);
        values.put(USER_IMAGE_LINK, user.imageLink);
        values.put(USER_IMAGE_HASH_DELETE, user.imageHashDelete);
        db.update(TABLE_USERS, values, USER_ID + " = ?",
                new String[]{String.valueOf(user.id)});
    }

    public void deleteUsers() {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_USERS, null, null);
        db.close();
    }
}

