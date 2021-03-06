package com.score.senz.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.MediaStore;
import android.telephony.TelephonyManager;

import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.score.senz.R;
import com.score.senzc.pojos.User;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Utility class to deal with Phone book
 * 1. Read all contacts
 * 2. Read contact name from phone no
 */
public class PhoneBookUtils {

    /**
     * Read contact name from phone no
     *
     * @param context     application context
     * @param phoneNumber phone no
     * @return contact name
     */
    public static String getContactName(Context context, String phoneNumber) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = contentResolver.query(uri, new String[]{ContactsContract.PhoneLookup.DISPLAY_NAME}, null, null, null);
        if (cursor == null) {
            return phoneNumber;
        }
        String contactName = phoneNumber;
        if (cursor.moveToFirst()) {
            contactName = cursor.getString(cursor.getColumnIndex(ContactsContract.PhoneLookup.DISPLAY_NAME));
        }

        if (cursor != null && !cursor.isClosed()) {
            cursor.close();
        }

        return contactName;
    }

    /**
     * Get image of the matching contact
     *
     * @param context
     * @param phoneNumber
     * @return
     */
    public static Bitmap getContactImage(Context context, String phoneNumber) {
        ContentResolver contentResolver = context.getContentResolver();
        Uri uri = Uri.withAppendedPath(ContactsContract.PhoneLookup.CONTENT_FILTER_URI, Uri.encode(phoneNumber));
        Cursor cursor = contentResolver.query(uri, new String[]{Phone.PHOTO_URI}, null, null, null);
        if (cursor == null) return null;

        try {
            if (cursor.moveToFirst()) {
                String image_uri = cursor.getString(cursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.PHOTO_URI));
                if (image_uri != null)
                    return MediaStore.Images.Media.getBitmap(contentResolver, Uri.parse(image_uri));
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            cursor.close();
        }

        return null;
    }

    /**
     * Read all contacts from contact database, we read
     * 1. name
     * 2. phone no
     *
     * @param context application context
     * @return contact list
     */
    public static ArrayList<User> readContacts(Context context) {
        ArrayList<User> contactList = new ArrayList<User>();

        String _ID = ContactsContract.Contacts._ID;
        String DISPLAY_NAME = ContactsContract.Contacts.DISPLAY_NAME;
        String NUMBER = ContactsContract.CommonDataKinds.Phone.NUMBER;

        Cursor managedCursor = context.getContentResolver().query(
                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                new String[]{Phone._ID, Phone.DISPLAY_NAME, Phone.NUMBER},
                null,
                null,
                Phone.DISPLAY_NAME + " ASC");

        while (managedCursor.moveToNext()) {
            String contact_id = managedCursor.getString(managedCursor.getColumnIndex(_ID));
            String name = managedCursor.getString(managedCursor.getColumnIndex(DISPLAY_NAME));
            String phoneNo = managedCursor.getString(managedCursor.getColumnIndex(NUMBER));

            //User user = new User(contact_id, getFormattedPhoneNo(context, phoneNo), "password");
            //user.setUsername(name.toLowerCase());
            //contactList.add(user);
        }

        managedCursor.close();

        return contactList;
    }

    /**
     * Get country code according to SIM card details
     *
     * @param context application context
     * @return country telephone code (ex: +94)
     */
    public static String getCountryCode(Context context) {
        TelephonyManager telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        String countryId = telephonyManager.getSimCountryIso().toUpperCase();
        String[] countryCodes = context.getResources().getStringArray(R.array.CountryCodes);

        for (int i = 0; i < countryCodes.length; i++) {
            String[] tmp = countryCodes[i].split(",");
            if (tmp[1].trim().equals(countryId.trim())) {
                return "+" + tmp[0];
            }
        }

        return "";
    }

    /**
     * Remove unwanted characters and get internationalized phone no
     * Actually format local no to international format
     *
     * @param phoneNo phone no
     * @return internationalized phone no (ex: +94775432015)
     */
    public static String getFormattedPhoneNo(Context context, String phoneNo) {
        String formattedPhoneNo = phoneNo.replaceAll("[^+0-9]", "");
        String countryCode = getCountryCode(context);

        try {
            // phone must begin with '+'
            // verify it is a internationalized no
            PhoneNumberUtil.getInstance().parse(formattedPhoneNo, "");
        } catch (NumberParseException e) {
            // non internationalized no(local no)
            if (formattedPhoneNo.length() >= 10)
                formattedPhoneNo = countryCode + formattedPhoneNo.substring(formattedPhoneNo.length() - 9);
            else
                formattedPhoneNo = countryCode + formattedPhoneNo;
        }

        return formattedPhoneNo;
    }

}
