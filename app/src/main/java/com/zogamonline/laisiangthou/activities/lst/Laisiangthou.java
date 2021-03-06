package com.zogamonline.laisiangthou.activities.lst;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.Spinner;
import android.widget.TextView;

import com.zogamonline.laisiangthou.R;
import com.zogamonline.laisiangthou.activities.BaseActivity;
import com.zogamonline.laisiangthou.adapters.lst.ListAdapter;
import com.zogamonline.laisiangthou.adapters.lst.LstBookGridAdapter;
import com.zogamonline.laisiangthou.items.lst.Book;
import com.zogamonline.laisiangthou.providers.alui.LaisiangthouLibrary;
import com.zogamonline.laisiangthou.setting.LSTPreferenceActivity;
import com.zogamonline.laisiangthou.setting.SettingsActivity;

import java.util.List;

/**
 * An activity extending {@link BaseActivity} class.
 * Hiai ah {@link Spinner} zang a {@link Book te}, {@link com.zogamonline.laisiangthou.items.lst.Chapter te} leh changte load ding in
 * Huai a pan a bung leh tang select penpen a lut geih na ding {@linkplain ChapterActivity};
 * Created by Maktaduai
 */
@SuppressWarnings("deprecation")
@SuppressLint("InflateParams")
public class Laisiangthou extends BaseActivity {

    private List<Book> books = null;
    private ListView thukhunlui;
    private GridView thukhunluiGrid;
    private Spinner BookSpinner;
    private Spinner ChapterSpinner;
    private Spinner VerseSpinner;
    private ImageView goToVerse;
    private ArrayAdapter<Book> adapter;

    private int selected_chapter;
    private Book selected_book;
    private int selected_verse;

    //int testamentId = 1;
    private boolean isList;
    private Menu menu;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        checkPreferences();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.thukhunlui);
        overridePendingTransition(R.anim.activity_in, R.anim.activity_out);

        setUpSpinners();

        thukhunlui = (ListView) findViewById(R.id.luiLaibu);
        thukhunluiGrid = (GridView) findViewById(R.id.gridBook);
        navigationView.getMenu().getItem(0).setChecked(true);
    }

    private void changeView() {

        if (isList) {
            thukhunlui.setVisibility(ListView.VISIBLE);
            thukhunluiGrid.setVisibility(GridView.GONE);
            isList = false;
            books = LaisiangthouLibrary.getBooks(getContentResolver());
            thukhunlui.setAdapter(new ListAdapter(this, books));
            thukhunlui.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                @Override
                public void onItemClick(AdapterView<?> parent, View view,
                                        int position, long id) {
                    final Book book = ((ListAdapter) thukhunlui
                            .getAdapter()).getItem(position);
                    gotoChapter(book, 1);
                }
            });
        } else {
            thukhunlui.setVisibility(ListView.GONE);
            thukhunluiGrid.setVisibility(GridView.VISIBLE);
            isList = true;
            books = LaisiangthouLibrary.getBooks(getContentResolver());
            thukhunluiGrid.setAdapter(new LstBookGridAdapter(this, books));
            thukhunluiGrid.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                    final Book book = ((LstBookGridAdapter) thukhunluiGrid.getAdapter()).getItem(position);
                    gotoChapter(book, 1);
                    Intent intent = new Intent(Laisiangthou.this, BungActivity.class);
                    intent.putExtra(BungActivity.TITLE, book.name);
                    intent.putExtra(BungActivity.BOOK_ID, book.id);
                    intent.putExtra(BungActivity.CHAPTER, selected_chapter);
                    startActivity(intent);
                }
            });
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.menu = menu;
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.grid_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.search:
                Intent intent = new Intent(this, SearchActivity.class);
                startActivity(intent);
                break;
            case R.id.list_menu:
                changeView();
                if (isList) {
                    menu.getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_action_list));
                } else {
                    menu.getItem(0).setIcon(getResources().getDrawable(R.drawable.ic_action_grid));
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    //Spinner load
    private void setUpSpinners() {
        thukhunlui = (ListView) findViewById(R.id.luiLaibu);

        BookSpinner = (Spinner) findViewById(R.id.BookSpinner);
        books = LaisiangthouLibrary.getBooks(getContentResolver());
        adapter = new ArrayAdapter<>(this, R.layout.simple_dropdown_item,
                books);
        int position = adapter.getPosition(selected_book);
        BookSpinner.getItemAtPosition(position);
        BookSpinner.setAdapter(adapter);
        adapter.notifyDataSetChanged();

        ChapterSpinner = (Spinner) findViewById(R.id.ChapterSpinner);
        VerseSpinner = (Spinner) findViewById(R.id.VerseSpinner);
        goToVerse = (ImageView) findViewById(R.id.goToVerse);

        BookSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                selected_book = adapter.getItem(position);
                int count = LaisiangthouLibrary.getChapterCount(getContentResolver(),
                        selected_book);
                final String[] chapterNames = new String[count];
                for (int i = 0; i < count; i++) {
                    chapterNames[i] = " " + (i + 1);
                }
                final ArrayAdapter<String> chapterAdapter = new ArrayAdapter<>(Laisiangthou.this,
                        R.layout.simple_dropdown_item, chapterNames);
                selected_chapter = (position + 1);
                ChapterSpinner.setAdapter(chapterAdapter);
                chapterAdapter.notifyDataSetChanged();

                int selected = BookSpinner.getSelectedItemPosition();
                SharedPreferences sharedPreferences = getSharedPreferences("LaibuSpinner", 0);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putInt("selectedLaibu", selected);
                editor.apply();
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        ChapterSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long id) {
                selected_chapter = position + 1;
                int chaptercount = LaisiangthouLibrary.getVerseCount(getContentResolver(), selected_book,
                        selected_chapter);
                final String[] verseNames = new String[chaptercount];
                for (int i = 0; i < chaptercount; i++) {
                    verseNames[i] = "" + (i + 1);
                    ArrayAdapter<String> verseAdapter = new ArrayAdapter<>(Laisiangthou.this,
                            R.layout.simple_dropdown_item, verseNames);
                    SharedPreferences pref = getSharedPreferences("ChangSpinner", Context.MODE_PRIVATE);
                    SharedPreferences.Editor verseEditor = pref.edit();
                    verseEditor.putInt("ChangSpinner", VerseSpinner.getSelectedItemPosition());
                    verseEditor.apply();
                    VerseSpinner.setAdapter(verseAdapter);
                    verseAdapter.notifyDataSetChanged();

                    int selectedChapter = ChapterSpinner.getSelectedItemPosition();
                    SharedPreferences sharedPreferences = getSharedPreferences("BungSpinner", 0);
                    SharedPreferences.Editor editor = sharedPreferences.edit();
                    editor.putInt("selectedBung", selectedChapter);
                    editor.apply();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        VerseSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, final int position, long id) {
                goToVerse.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        selected_verse = position + 1;

                        int selectedVerse = VerseSpinner.getSelectedItemPosition();
                        SharedPreferences sharedPreferences = getSharedPreferences("ChangSpinner", 0);
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        editor.putInt("selectedChang", selectedVerse);
                        editor.apply();
                        Intent intent = new Intent(Laisiangthou.this, ChapterActivity.class);
                        intent.putExtra(ChapterActivity.TITLE, selected_book.name);
                        intent.putExtra(ChapterActivity.BOOK_ID, selected_book.id);
                        intent.putExtra(ChapterActivity.CHAPTER, selected_chapter);
                        intent.putExtra(ChapterActivity.VERSE, selected_verse);
                        startActivity(intent);
                    }
                });
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        books = LaisiangthouLibrary.getBooks(getContentResolver());
        thukhunlui.setAdapter(new ListAdapter(this, books));
        thukhunlui.setOnItemClickListener(new AdapterView.OnItemClickListener() {

            @Override
            public void onItemClick(AdapterView<?> parent, View view,
                                    int position, long id) {
                final Book book = ((ListAdapter) thukhunlui
                        .getAdapter()).getItem(position);
                gotoChapter(book, 1);
            }
        });
    }

    private void gotoChapter(Book book, int chapter) {
        Intent intent = new Intent(this, ChapterActivity.class);
        intent.putExtra(ChapterActivity.TITLE, book.name);
        intent.putExtra(ChapterActivity.BOOK_ID, book.id);
        intent.putExtra(ChapterActivity.CHAPTER, chapter);
        startActivity(intent);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (SettingsActivity.sChanged) {
            Laisiangthou.this.recreate();
            SettingsActivity.sChanged = false;
        }
        checkPreferences();
    }

    private void getPref() {
        SharedPreferences sharedPref;
        sharedPref = getSharedPreferences("LaibuSpinner", Context.MODE_PRIVATE);
        int bookValue = sharedPref.getInt("selectedLaibu", -1);
        if (bookValue != -1) {
            BookSpinner.setSelection(bookValue);
        }
        sharedPref = getSharedPreferences("BungSpinner", Context.MODE_PRIVATE);
        int chapterValue = sharedPref.getInt("selectedBung", -1);
        if (chapterValue != -1) {
            ChapterSpinner.setSelection(chapterValue);
        }
        sharedPref = getSharedPreferences("ChangSpinner", Context.MODE_PRIVATE);
        int verseValue = sharedPref.getInt("selectedChang", -1);
        if (verseValue != -1) {
            VerseSpinner.setSelection(verseValue);
        }
    }

    @SuppressLint("SetTextI18n")
    public void selectChapter(final Book book) {
        int count = LaisiangthouLibrary.getChapterCount(getContentResolver(), book);
        if (count == 1) {
            gotoChapter(book, 1);
        } else {
            final String[] chapterNames = new String[count];
            for (int i = 0; i < count; i++) {
                chapterNames[i] = " " + (i + 1);
            }
            View chapterListView = LayoutInflater.from(Laisiangthou.this)
                    .inflate(R.layout.bung_dialog, null);
            final AlertDialog dialog = new AlertDialog.Builder(Laisiangthou.this).create();
            dialog.getWindow().getAttributes().windowAnimations = R.style.Dialog_Animation;
            dialog.setView(chapterListView);
            View bungte = getLayoutInflater().inflate(R.layout.dialog_bung, null);
            TextView bungTitle = (TextView) bungte.findViewById(R.id.dialog_bung_title);
            bungTitle.setText(book.name + " " + "Laibu");
            bungTitle.setCompoundDrawablesWithIntrinsicBounds(0, R.drawable.book, 0, 0);
            dialog.setCustomTitle(bungte);
            final GridView gridView = (GridView) chapterListView
                    .findViewById(R.id.bungList);
            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    Laisiangthou.this, R.layout.list_chapter, chapterNames);
            gridView.setAdapter(adapter);
            gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view,
                                        int position, long id) {
                    gotoChapter(book, position + 1);
                    dialog.cancel();
                }
            });
            dialog.show();
        }
    }

    private void checkPreferences() {
        SharedPreferences sharedPreferences = PreferenceManager
                .getDefaultSharedPreferences(this);
        if (sharedPreferences.getBoolean("notifications_new_message", false)) {
            getWindow()
                    .addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        }
    }

    @Override
     public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ((keyCode == KeyEvent.KEYCODE_BACK)){
            DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
            if (drawer.isDrawerOpen(GravityCompat.START)) {
                drawer.closeDrawer(GravityCompat.START);
            } else {
                Laisiangthou.this.finish();
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}