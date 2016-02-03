package com.example.n00132610.mycalendarapp;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AppCompatActivity;
import android.text.format.DateFormat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import static com.example.n00132610.mycalendarapp.DBOpenHelper.NOTE_DATE;
import static com.example.n00132610.mycalendarapp.DBOpenHelper.NOTE_TEXT;
import static com.example.n00132610.mycalendarapp.DBOpenHelper.NOTE_TIME;

public class EditorActivity extends AppCompatActivity {

    public static final String KEY_ID = "id";
    public static final String KEY_TIME = "time" ;

    private String action;
    private EditText editor;
    private EditText editorDate;
    private EditText editorTime;
    private String noteFilter;
    private String oldText;
    private String oldDate;
    private String oldTime;

    //private FloatingActionButton enableEdit;
    //private FloatingActionButton enableSave;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        editor = (EditText) findViewById(R.id.editText);
        editorDate = (EditText) findViewById(R.id.editDate);
        editorTime = (EditText) findViewById(R.id.editTime);
        //enableSave = (FloatingActionButton) findViewById(R.id.fabSave);
        //enableEdit = (FloatingActionButton) findViewById(R.id.fabEdit);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        long id = extras.getLong(KEY_ID);

        if (id == 0){
            action = Intent.ACTION_INSERT;
            setTitle(getString(R.string.new_note));

            long time = intent.getLongExtra(KEY_TIME, 0);
            if (time != 0) {
                Date d = new Date(time);
                String dateString= DateFormat.format("yyyy-MM-dd", d).toString();
                editorDate.setText(dateString);
            }
        }
        else {
            action = Intent.ACTION_EDIT;
            setTitle(getString(R.string.edit_note));

            Uri uri = Uri.parse(NotesProvider.CONTENT_URI + "/" + id);
            noteFilter = DBOpenHelper.NOTE_ID + "=" + uri.getLastPathSegment();

            Cursor cursor;
            cursor = getContentResolver().query(uri,
                    DBOpenHelper.ALL_COLUMNS, noteFilter, null, null);
            cursor.moveToFirst();
            oldText = cursor.getString(cursor.getColumnIndex(NOTE_TEXT));
            oldDate = cursor.getString(cursor.getColumnIndex(NOTE_DATE));
            oldTime = cursor.getString(cursor.getColumnIndex(NOTE_TIME));
            editor.setText(oldText);
            editor.setEnabled(false);
            editorDate.setText(oldDate);
            editorDate.setEnabled(false);
            editorTime.setText(oldTime);
            editorTime.setEnabled(false);
            editor.requestFocus();
            //enableEdit.setEnabled(true);
            //enableSave.setEnabled(false);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        if (action.equals(Intent.ACTION_EDIT)){
            getMenuInflater().inflate(R.menu.menu_editor, menu);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        switch (item.getItemId()) {
            case android.R.id.home:
                finishEditing();
                break;
            case R.id.action_delete:
                deleteNote();
                break;
            case R.id.action_edit:
                enableFields();
                break;
            //case R.id.fabEdit:
                //enableFields();
                //break;
        }

        return true;
    }
    private void enableFields(){
        if(NotesProvider.CONTENT_URI != null) {
            editor.setEnabled(true);
            editorDate.setEnabled(true);
            editorTime.setEnabled(true);
            //enableSave.setEnabled(true);
            //enableEdit.setEnabled(false);
        }
    }
    private void deleteNote() {
        getContentResolver().delete(NotesProvider.CONTENT_URI,
                noteFilter,null);
        Toast.makeText(this, R.string.note_deleted,
                Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
        finish();
    }

    private void finishEditing(){
        String newText = editor.getText().toString().trim();
        String newDate = editorDate.getText().toString().trim();
        String newTime = editorTime.getText().toString().trim();

        switch (action) {
            case Intent.ACTION_INSERT:
                if (newText.length() == 0 && newDate.length() == 0 && newTime.length() == 0){
                    setResult(RESULT_CANCELED);
                } else{
                    insertNote(newText, newDate, newTime);
                }
                break;
            case Intent.ACTION_EDIT:
                if (newText.length() == 0 && newDate.length() == 0 && newTime.length() == 0){
                    deleteNote();
                }else if (oldText.equals(newText) && oldDate.equals(newDate) && oldTime.equals(newTime)){
                    setResult(RESULT_CANCELED);
                }else {
                    updateNote(newText, newDate, newTime);
                }
        }
        finish();
    }

    private void updateNote(String noteText, String noteDate, String noteTime) {
        ContentValues values = new ContentValues();
        values.put(NOTE_TEXT, noteText);
        values.put(NOTE_DATE, noteDate);
        values.put(NOTE_TIME, noteTime);
        getContentResolver().update(NotesProvider.CONTENT_URI, values, noteFilter, null);
        Toast.makeText(this, R.string.note_updated, Toast.LENGTH_SHORT).show();
        setResult(RESULT_OK);
    }

    private void insertNote(String noteText, String noteDate, String noteTime) {
        ContentValues values = new ContentValues();
        values.put(NOTE_TEXT, noteText);
        values.put(NOTE_DATE, noteDate);
        values.put(NOTE_TIME, noteTime);
        getContentResolver().insert(NotesProvider.CONTENT_URI, values);
        setResult(RESULT_OK);
    }

    @Override
    public void onBackPressed() {
        finishEditing();
    }

    public void onSaveNote(View view) { finishEditing();}

    public void onButtonClicked(View v){
        TimePickerFragment newFragment = new TimePickerFragment();
        newFragment.show(getSupportFragmentManager(), "timePicker");
    }

    public void showDatePickerDialog(View v) {
        DatePickerFragment newFragment = new DatePickerFragment();
        newFragment.show(getSupportFragmentManager(), "datePicker");
    }
}
