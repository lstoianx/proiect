package com.example.player;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class PlaylistSongsAdapter extends RecyclerView.Adapter<PlaylistSongsAdapter.PlaylistSongsAdapterViewHolder> {

    Context context;
    List<Song> arrayListSongs;
    Song soong;

    public PlaylistSongsAdapter(Context context, List<Song> arrayListSongs){
        this.context=context;
        this.arrayListSongs = arrayListSongs;
    }

    @NonNull
    @Override
    public PlaylistSongsAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.song_item,parent, false);
        return new PlaylistSongsAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlaylistSongsAdapterViewHolder holder, int position) {
        Song song = arrayListSongs.get(position);
        holder.titleTxt.setText(song.getStageName()+" - "+song.getTitle());
        holder.durationTxt.setText(song.getDuration());
        holder.link = song.getLink();

        soong = song;
    }

    @Override
    public int getItemCount() {
        return arrayListSongs.size();
    }

    public class PlaylistSongsAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView titleTxt, durationTxt;
        String link;

        public PlaylistSongsAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTxt = itemView.findViewById(R.id.song_title);
            durationTxt = itemView.findViewById(R.id.song_duration);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    ((PlaylistActivity)context).shareSong(getAdapterPosition());
                    return false;
                }
            });
        }

        @Override
        public void onClick(View v) {

            ((PlaylistActivity)context).playSong(getAdapterPosition());
        }


    }
}
