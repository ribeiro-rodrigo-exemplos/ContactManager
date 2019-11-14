package com.anushka.androidtutz.contactmanager;

import android.content.DialogInterface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.anushka.androidtutz.contactmanager.adapter.ContactsAdapter;
import com.anushka.androidtutz.contactmanager.db.ContactsAppDatabase;
import com.anushka.androidtutz.contactmanager.db.entity.Contact;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private ContactsAdapter contactsAdapter;
    private ArrayList<Contact> contactArrayList = new ArrayList<>();
    private RecyclerView recyclerView;
    private ContactsAppDatabase contactsAppDatabase;
    //private DatabaseHelper db;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setTitle(" Contacts Manager");

        recyclerView = findViewById(R.id.recycler_view_contacts);
        contactsAppDatabase = Room.databaseBuilder(getApplicationContext(),
                ContactsAppDatabase.class, "ContactDB").addCallback(callback).build();



        //db = new DatabaseHelper(this);

        //contactArrayList.addAll(contactsAppDatabase.getContactDAO().getContacts());
        new GetAllContactsAsyncTask().execute();

        contactsAdapter = new ContactsAdapter(this, contactArrayList, MainActivity.this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext());
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(contactsAdapter);


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addAndEditContacts(false, null, -1);
            }


        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();


        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    public void addAndEditContacts(final boolean isUpdate, final Contact contact, final int position) {
        LayoutInflater layoutInflaterAndroid = LayoutInflater.from(getApplicationContext());
        View view = layoutInflaterAndroid.inflate(R.layout.layout_add_contact, null);

        AlertDialog.Builder alertDialogBuilderUserInput = new AlertDialog.Builder(MainActivity.this);
        alertDialogBuilderUserInput.setView(view);

        TextView contactTitle = view.findViewById(R.id.new_contact_title);
        final EditText newContact = view.findViewById(R.id.name);
        final EditText contactEmail = view.findViewById(R.id.email);

        contactTitle.setText(!isUpdate ? "Add New Contact" : "Edit Contact");

        if (isUpdate && contact != null) {
            newContact.setText(contact.getName());
            contactEmail.setText(contact.getEmail());
        }

        alertDialogBuilderUserInput
                .setCancelable(false)
                .setPositiveButton(isUpdate ? "Update" : "Save", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialogBox, int id) {

                    }
                })
                .setNegativeButton("Delete",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialogBox, int id) {

                                if (isUpdate) {

                                    deleteContact(contact, position);
                                } else {

                                    dialogBox.cancel();

                                }

                            }
                        });


        final AlertDialog alertDialog = alertDialogBuilderUserInput.create();
        alertDialog.show();

        alertDialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(newContact.getText().toString())) {
                    Toast.makeText(MainActivity.this, "Enter contact name!", Toast.LENGTH_SHORT).show();
                    return;
                } else {
                    alertDialog.dismiss();
                }


                if (isUpdate && contact != null) {

                    updateContact(newContact.getText().toString(), contactEmail.getText().toString(), position);
                } else {

                    createContact(newContact.getText().toString(), contactEmail.getText().toString());
                }
            }
        });
    }

    private void deleteContact(Contact contact, int position) {

        contactArrayList.remove(position);
        new DeleteContactAsyncTask().execute(contact);

    }

    private void updateContact(String name, String email, int position) {

        Contact contact = contactArrayList.get(position);

        contact.setName(name);
        contact.setEmail(email);

        new UpdateContactAsyncTask().execute(contact);

        contactArrayList.set(position, contact);


    }

    private void createContact(String name, String email) {
        new CreateContactAsyncTask().execute(new Contact(0,name,email));
    }

    private class GetAllContactsAsyncTask extends AsyncTask<Void,Void, Void>{
        @Override
        protected Void doInBackground(Void... voids) {
            contactArrayList.addAll(contactsAppDatabase.getContactDAO().getContacts());
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);

            contactsAdapter.notifyDataSetChanged();

        }
    }

    private class CreateContactAsyncTask extends AsyncTask<Contact,Void,Void>{

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            contactsAdapter.notifyDataSetChanged();
        }

        @Override
        protected Void doInBackground(Contact... contacts) {

            long id = contactsAppDatabase.getContactDAO().addContact(contacts[0]);


            Contact contact = contactsAppDatabase.getContactDAO().getContact(id);

            if (contact != null) {

                contactArrayList.add(0, contact);

            }

            return null;
        }
    }

    private class UpdateContactAsyncTask extends AsyncTask<Contact,Void,Void>{

        @Override
        protected Void doInBackground(Contact... contacts) {
            contactsAppDatabase.getContactDAO().updateContact(contacts[0]);
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            contactsAdapter.notifyDataSetChanged();
        }
    }

    private class DeleteContactAsyncTask extends AsyncTask<Contact, Void, Void>{
        @Override
        protected Void doInBackground(Contact... contacts) {
            contactsAppDatabase.getContactDAO().deleteContact(contacts[0]);

            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            contactsAdapter.notifyDataSetChanged();
        }
    }

    RoomDatabase.Callback callback = new RoomDatabase.Callback() {
        @Override
        public void onCreate(@NonNull SupportSQLiteDatabase db) {
            super.onCreate(db);

            Log.i("MainActivity","callback on create invoked");
            createContact("name 1","email 1");
            createContact("name 2","email 2");
            createContact("name 3","email 3");
            createContact("name 4","email 4");
            createContact("name 5","email 5");
        }

        @Override
        public void onOpen(@NonNull SupportSQLiteDatabase db) {
            super.onOpen(db);

            Log.i("MainActivity","callback on open invoked");
        }
    };
}
