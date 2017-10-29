package com.pugfish1992.javario.example;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.util.SortedList;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pugfish1992.javario.Music;

import java.util.List;

public class MainActivity extends AppCompatActivity
        implements EditMusicDialog.OnFinishEditingListener {

    private MusicAdapter mMusicAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        final FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab_add_new_music);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showEditMusicDialog(null);
            }
        });

        mMusicAdapter = new MusicAdapter(Music.listItems(), this);
        mMusicAdapter.setOnMusicCardClickListener(new MusicAdapter.OnMusicCardClickListener() {
            @Override
            public void onMusicCardClick(int position) {
                showEditMusicDialog(mMusicAdapter.getMusicAt(position));
            }
        });

        RecyclerView feedList = (RecyclerView) findViewById(R.id.recyc_music_card_list);
        feedList.setLayoutManager(new LinearLayoutManager(this));
        feedList.setAdapter(mMusicAdapter);

        ItemTouchHelper.Callback callback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
            @Override
            public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target) {
                return false;
            }

            @Override
            public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
                int position = viewHolder.getAdapterPosition();
                mMusicAdapter.getMusicAt(position).delete();
                mMusicAdapter.removeMusicAt(position);
            }
        };
        new ItemTouchHelper(callback).attachToRecyclerView(feedList);
    }

    private void showEditMusicDialog(Music music) {
        EditMusicDialog dialog = EditMusicDialog.newInstance(music);
        dialog.show(getSupportFragmentManager(), null);
    }

    /**
     * INTERFACE IMPL -> EditMusicDialog.OnFinishEditingListener
     * ---------- */

    @Override
    public void onFinishEditing(@Nullable Music oldMusic, @NonNull Music newMusic) {
        newMusic.save();
        mMusicAdapter.addMusic(newMusic);
    }
    
    /* ------------------------------------------------------------------------------- *
     * ADAPTER CLASS
     * ------------------------------------------------------------------------------- */

    private static class MusicAdapter extends RecyclerView.Adapter<MusicViewHolder> {

        private final int RATING_COLOR_0;
        private final int RATING_COLOR_1;
        private final int RATING_COLOR_2;
        private final int RATING_COLOR_3;
        private final int RATING_COLOR_4;

        interface OnMusicCardClickListener {
            void onMusicCardClick(int position);
        }

        private SortedList<Music> mMusics;
        private OnMusicCardClickListener mOnMusicCardClickListener;

        MusicAdapter(List<Music> musics, Context context) {
            RATING_COLOR_0 = ContextCompat.getColor(context, R.color.rating_0);
            RATING_COLOR_1 = ContextCompat.getColor(context, R.color.rating_1);
            RATING_COLOR_2 = ContextCompat.getColor(context, R.color.rating_2);
            RATING_COLOR_3 = ContextCompat.getColor(context, R.color.rating_3);
            RATING_COLOR_4 = ContextCompat.getColor(context, R.color.rating_4);

            mMusics = new SortedList<>(Music.class,
                    new SortedList.Callback<Music>() {
                        @Override
                        public int compare(Music o1, Music o2) {
                            return Long.valueOf(o2.id).compareTo(o1.id);
                        }

                        @Override
                        public void onChanged(int position, int count) {
                            notifyItemRangeChanged(position, count);
                        }

                        @Override
                        public boolean areContentsTheSame(Music oldItem, Music newItem) {
                            return oldItem.equals(newItem);
                        }

                        @Override
                        public boolean areItemsTheSame(Music item1, Music item2) {
                            return item1.id == item2.id;
                        }

                        @Override
                        public void onInserted(int position, int count) {
                            notifyItemRangeInserted(position, count);
                        }

                        @Override
                        public void onRemoved(int position, int count) {
                            notifyItemRangeRemoved(position, count);
                        }

                        @Override
                        public void onMoved(int fromPosition, int toPosition) {
                            notifyItemMoved(fromPosition, toPosition);
                        }
                    });

            if (musics != null) {
                mMusics.addAll(musics);
            }
        }

        void setOnMusicCardClickListener(OnMusicCardClickListener onMusicCardClickListener) {
            mOnMusicCardClickListener = onMusicCardClickListener;
        }

        @Override
        public MusicViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.music_card, parent, false);
            final MusicViewHolder holder = new MusicViewHolder(view);
            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    mOnMusicCardClickListener.onMusicCardClick(holder.getAdapterPosition());
                }
            });

            return holder;
        }

        @Override
        public void onBindViewHolder(MusicViewHolder holder, int position) {
            Music music = mMusics.get(position);
            holder.nameView.setText(music.name);
            holder.noteView.setText(music.note);

            if (music.rating <= 0) {
                holder.bgCardView.setCardBackgroundColor(RATING_COLOR_0);
            } else if (music.rating == 1) {
                holder.bgCardView.setCardBackgroundColor(RATING_COLOR_1);
            } else if (music.rating == 2) {
                holder.bgCardView.setCardBackgroundColor(RATING_COLOR_2);
            } else if (music.rating == 3) {
                holder.bgCardView.setCardBackgroundColor(RATING_COLOR_3);
            } else if (4 <= music.rating) {
                holder.bgCardView.setCardBackgroundColor(RATING_COLOR_4);
            }
        }

        @Override
        public int getItemCount() {
            return mMusics.size();
        }

        void addMusic(Music music) {
            mMusics.add(music);
        }

        Music getMusicAt(int position) {
            return mMusics.get(position);
        }

        void removeMusicAt(int position) {
            mMusics.removeItemAt(position);
        }
    }

    /* ------------------------------------------------------------------------------- *
     * VIEW HOLDER CLASS
     * ------------------------------------------------------------------------------- */

    private static class MusicViewHolder extends RecyclerView.ViewHolder {

        CardView bgCardView;
        TextView nameView;
        TextView noteView;

        MusicViewHolder(View view) {
            super(view);
            bgCardView = (CardView) view;
            nameView = view.findViewById(R.id.txt_name);
            noteView = view.findViewById(R.id.txt_note);
        }
    }
}
