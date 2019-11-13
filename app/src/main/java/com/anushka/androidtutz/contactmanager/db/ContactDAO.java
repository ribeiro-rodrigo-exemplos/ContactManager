package com.anushka.androidtutz.contactmanager.db;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import com.anushka.androidtutz.contactmanager.db.entity.Contact;

import java.util.List;

@Dao
public interface ContactDAO {

    @Insert
    Long addContact(Contact conact);

    @Update
    void updateContact(Contact contact);

    @Delete
    void deleteContact(Contact contact);

    @Query("select * from contacts")
    List<Contact> getContacts();

    @Query("select * from contacts where contact_id == :contactId")
    Contact getContact(long contactId);
}
