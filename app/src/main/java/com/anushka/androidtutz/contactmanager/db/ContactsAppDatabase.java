package com.anushka.androidtutz.contactmanager.db;

import androidx.room.Database;
import androidx.room.RoomDatabase;

import com.anushka.androidtutz.contactmanager.db.entity.Contact;

@Database(entities = {Contact.class}, version = 1, exportSchema = false)
public abstract class ContactsAppDatabase extends RoomDatabase {

    public abstract ContactDAO getContactDAO();
}
