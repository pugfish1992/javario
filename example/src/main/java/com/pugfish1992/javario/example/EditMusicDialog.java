package com.pugfish1992.javario.example;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.RatingBar;

import com.pugfish1992.javario.Music;

/**
 * Created by daichi on 10/29/17.
 */

public class EditMusicDialog extends DialogFragment {

    public interface OnFinishEditingListener {
        void onFinishEditing(@Nullable Music oldMusic, @NonNull Music newMusic);
    }

    private static final String ARG_MUSIC_ID = "EditMusicDialog:musicId";
    private static final String ARG_MUSIC_TITLE = "EditMusicDialog:musicName";
    private static final String ARG_MUSIC_NOTE = "EditMusicDialog:musicNote";
    private static final String ARG_MUSIC_RATING = "EditMusicDialog:musicRating";

    private OnFinishEditingListener mListener;
    @Nullable private Music mOldMusic;

    public static EditMusicDialog newInstance(Music music) {
        EditMusicDialog fragment = new EditMusicDialog();
        if (music != null) {
            Bundle args = new Bundle();
            args.putLong(ARG_MUSIC_ID, music.id);
            args.putString(ARG_MUSIC_TITLE, music.name);
            args.putString(ARG_MUSIC_NOTE, music.note);
            args.putInt(ARG_MUSIC_RATING, music.rating);
            fragment.setArguments(args);
        }
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mOldMusic = new Music();
            mOldMusic.id = getArguments().getLong(ARG_MUSIC_ID);
            mOldMusic.name = getArguments().getString(ARG_MUSIC_TITLE);
            mOldMusic.note = getArguments().getString(ARG_MUSIC_NOTE);
            mOldMusic.rating = getArguments().getInt(ARG_MUSIC_RATING);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater()
                .inflate(R.layout.component_edit_music_dialog, null);

        final TextInputEditText nameEditor = view.findViewById(R.id.edit_title);
        final TextInputEditText noteEditor = view.findViewById(R.id.edit_note);
        final RatingBar ratingBar = view.findViewById(R.id.rating_stars);
        if (mOldMusic != null) {
            nameEditor.setText(mOldMusic.name);
            noteEditor.setText(mOldMusic.note);
            ratingBar.setRating(mOldMusic.rating);
        }

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle("Edit Music")
                .setPositiveButton("SAVE", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Music music = new Music();
                        if (mOldMusic != null) {
                            music.id = mOldMusic.id;
                            music.name = mOldMusic.name;
                            music.note = mOldMusic.note;
                            music.rating = mOldMusic.rating;
                        }

                        String text = music.name = nameEditor.getText().toString();
                        music.name = (text.length() != 0) ? text : music.name;
                        text = noteEditor.getText().toString();
                        music.note = (text.length() != 0) ? text : music.note;
                        music.rating = (int) ratingBar.getRating();

                        mListener.onFinishEditing(mOldMusic, music);
                    }
                })
                .setNegativeButton("CANCEL", null)
                .create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (OnFinishEditingListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement DialogFragment.OnFinishEditingListener");
        }
    }
}