package com.example.player;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SearchPlaylistAdapter extends RecyclerView.Adapter<SearchPlaylistAdapter.SearchPlaylistAdapterViewHolder> {

    Context context;
    List<Song> arrayListSongs;
    Song soong;

    public SearchPlaylistAdapter(Context context, List<Song> arrayListSongs){
        this.context=context;
        this.arrayListSongs = arrayListSongs;
    }

    @NonNull
    @Override
    public SearchPlaylistAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.song_item,parent, false);
        return new SearchPlaylistAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SearchPlaylistAdapterViewHolder holder, int position) {
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

    public class SearchPlaylistAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView titleTxt, durationTxt;
        String link;

        public SearchPlaylistAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTxt = itemView.findViewById(R.id.song_title);
            durationTxt = itemView.findViewById(R.id.song_duration);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            ((FoundPlaylist)context).playSong(getAdapterPosition());
        }


    }
}
