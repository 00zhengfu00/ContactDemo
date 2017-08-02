package com.example.penny.contactdep;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;

import java.util.List;

import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity implements EasyPermissions.PermissionCallbacks {

    private static final int REQUEST_CODE_QRCODE_PERMISSIONS = 1;
    private ContentResolver mResolver;
    private ContactHelper mHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mResolver = getContentResolver();
        mHelper = new ContactHelper(mResolver);
        requestCodeQRCodePermissions();

        long start = System.currentTimeMillis();
        Log.d("start:", start + "");
        clearAllContact();
//        for (int i = 0; i < 300; i++) {
//            testInsert();
////            try {
////                deleteContact("随便");
////            } catch (Exception pE) {
////                pE.printStackTrace();
////            }
//        }
        long end = System.currentTimeMillis();
        Log.d("end:", end + "");
        long result = end - start;
        Log.d("result:", result / 1000 + "");
    }

    private void testDelete() {
        Contact lContact = new Contact();
        lContact.setName("随便");
        lContact.setNumber("13921008789");
        mHelper.deleteContact(lContact);
    }

    @AfterPermissionGranted(REQUEST_CODE_QRCODE_PERMISSIONS)
    private void requestCodeQRCodePermissions() {
        String[] perms = {Permissions.READ_CONTACTS, Permissions.WRITE_CONTACTS};
        if (!EasyPermissions.hasPermissions(this, perms)) {
            EasyPermissions.requestPermissions(this, "打开通讯录权限", REQUEST_CODE_QRCODE_PERMISSIONS, perms);
        }
    }

    private void testInsert() {
//        Contact lContact = new Contact();
//        lContact.setName("李天山");
//        lContact.setNumber("13921008789");
        ContentValues values = new ContentValues();
        /*
         * 首先向RawContacts.CONTENT_URI执行一个空值插入，目的是获得系统返回的rawContactId
         */
        Uri rawContactUri = getContentResolver().insert(ContactsContract.RawContacts.CONTENT_URI, values);
        long rawContactId = ContentUris.parseId(rawContactUri);
        mHelper.insertContact(values, rawContactId, mResolver);
    }


    @Override
    public void onPermissionsGranted(int requestCode, List<String> perms) {
        Log.d("onPermissionsGranted", "==========succees===================");
    }

    @Override
    public void onPermissionsDenied(int requestCode, List<String> perms) {
        Log.d("onPermissionsDenied", "==========fail===================");

    }


    public void deleteContact(String phoneNumber) throws Exception {
        //根据姓名求id
        Uri uri = Uri.parse("content://com.android.contacts/raw_contacts");

        Cursor cursor = mResolver.query(uri, new String[]{ContactsContract.Contacts.Data._ID}, "display_name=?", new String[]{phoneNumber}, null);
        if (cursor.moveToFirst()) {
            int id = cursor.getInt(0);
            //根据id删除data中的相应数据
            mResolver.delete(uri, "display_name=?", new String[]{phoneNumber});
            uri = Uri.parse("content://com.android.contacts/data");
            mResolver.delete(uri, "raw_contact_id=?", new String[]{id + ""});
        }
    }


    public void clearAllContact() {
        Cursor cur = mResolver.query(ContactsContract.Contacts.CONTENT_URI,
                null, null, null, null);
        while (cur.moveToNext()) {
            try {
                String lookupKey = cur.getString(cur.getColumnIndex(
                        ContactsContract.Contacts.LOOKUP_KEY));
                Uri uri = Uri.withAppendedPath(ContactsContract.
                        Contacts.CONTENT_LOOKUP_URI, lookupKey);
                System.out.println("The uri is " + uri.toString());
                mResolver.delete(uri, null, null);//删除所有的联系人
            } catch (Exception e) {
                System.out.println(e.getStackTrace());
            }
        }

    }
}
