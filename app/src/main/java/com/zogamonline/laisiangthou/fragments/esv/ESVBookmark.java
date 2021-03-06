package com.zogamonline.laisiangthou.fragments.esv;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import com.zogamonline.laisiangthou.R;
import com.zogamonline.laisiangthou.activities.esv.VerseActivity;
import com.zogamonline.laisiangthou.adapters.common.BookmarkAdapter;
import com.zogamonline.laisiangthou.items.esv.Books;
import com.zogamonline.laisiangthou.items.esv.KJVBookmarks;
import com.zogamonline.laisiangthou.items.esv.Verses;
import com.zogamonline.laisiangthou.providers.esv.KJVLibrary;

import java.util.HashSet;
import java.util.List;

/**
 * Creator: Maktaduai on 24-10-2015.
 */
@SuppressWarnings("StatementWithEmptyBody")
public class ESVBookmark extends Fragment{
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.chiamtehte, container, false);

        ListView chiamtehsate = (ListView)rootView.findViewById(R.id.bookmarkste);

        KJVBookmarks savedBookmarks = new KJVBookmarks(getActivity());
        savedBookmarks.loadBookmarks();
        final List<Integer> bookmarkListing = savedBookmarks.bookmarks;
        final String[] bookmarkStrings = new String[bookmarkListing.size()];
        if (bookmarkListing.size() == 0) {
        } else{
            for (int i = 0; i < bookmarkListing.size(); i++) {
                Verses bookmarkedVerse = KJVLibrary.getVerse(
                        getActivity().getContentResolver(),
                        bookmarkListing.get(i));
                final Books book = KJVLibrary.getBook(getActivity()
                        .getContentResolver(), bookmarkedVerse.bookId);
                bookmarkStrings[i] = book.name + ":"
                        + bookmarkedVerse.chapter + ":"
                        + bookmarkedVerse.number + " "+ bookmarkedVerse.text;
            }
        }
        chiamtehsate.setAdapter(new BookmarkAdapter(getActivity(), bookmarkStrings));
        chiamtehsate.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                Verses selectedBookmark = KJVLibrary
                        .getVerse(getActivity().getContentResolver(),
                                bookmarkListing.get(i));
                final Books book = KJVLibrary.getBook(getActivity().getContentResolver(),
                        selectedBookmark.bookId);

                Intent intent = new Intent(getActivity(),VerseActivity.class);
                intent.putExtra(VerseActivity.TITLE, book.name);
                intent.putExtra(VerseActivity.BOOK_ID, book.id);
                intent.putExtra(VerseActivity.CHAPTER,
                        selectedBookmark.chapter);
                intent.putExtra(VerseActivity.VERSE,
                        selectedBookmark.number);
                startActivity(intent);
            }
        });
        setHasOptionsMenu(true);
        return rootView;
    }
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.home, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.chiamtehte_lakhia:
                removeBookmark();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }
    private void removeBookmark() {
        final KJVBookmarks bookmarks = new KJVBookmarks(getActivity());
        bookmarks.loadBookmarks();
        final List<Integer> bookmarkListing = bookmarks.bookmarks;
        final String[] bookmarkStrings = new String[bookmarkListing.size()];
        if (bookmarkListing.size() == 0) {
            Toast.makeText(getActivity(), "Chiamteh masak ngai",
                    Toast.LENGTH_LONG).show();
        } else {
            for (int i = 0; i < bookmarkListing.size(); i++) {
                Verses bookmarkedVerse = KJVLibrary.getVerse(
                        getActivity().getContentResolver(),
                        bookmarkListing.get(i));
                final Books book = KJVLibrary.getBook(getActivity()
                        .getContentResolver(), bookmarkedVerse.bookId);
                bookmarkStrings[i] = book.name + ":"
                        + bookmarkedVerse.chapter + ":"
                        + bookmarkedVerse.number +" "+ "lakhe di?";
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Chiamtehsate Lakhia" + "\n" + "Tick inla OK meek in");
            builder.setIcon(android.R.drawable.ic_delete);
            final HashSet<Integer> bookmarksToDelete = new HashSet<>();
            builder.setMultiChoiceItems(bookmarkStrings, null,
                    new DialogInterface.OnMultiChoiceClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which,
                                            boolean isChecked) {
                            if (isChecked)
                                bookmarksToDelete.add(bookmarkListing
                                        .get(which));
                            else
                                bookmarksToDelete.remove(bookmarkListing
                                        .get(which));
                        }
                    });
            builder.setPositiveButton("Ok",
                    new DialogInterface.OnClickListener() {

                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            for (final Integer bookmark : bookmarksToDelete) {
                                bookmarks.removeBookmark(bookmark);
                                restartActivity(getActivity());
                                Toast.makeText(getActivity(), "Chiamtehsa na select te lakhe khin", Toast.LENGTH_SHORT).show();
                            }
                            dialog.dismiss();
                        }
                    });
            builder.setNegativeButton("Cancel",
                    new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog,
                                            int whichButton) {
                            dialog.cancel();
                        }
                    });
            builder.show();
        }
    }

    private void restartActivity(FragmentActivity activity) {
        activity.startActivity(getActivity().getIntent().addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));
    }
}
