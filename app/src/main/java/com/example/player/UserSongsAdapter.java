package com.example.player;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class UserSongsAdapter extends RecyclerView.Adapter<UserSongsAdapter.UserSongsAdapterViewHolder> {

    Context context;
    List<Song> arrayListSongs;
    Song soong;

    public UserSongsAdapter(Context context, List<Song> arrayListSongs){
        this.context=context;
        this.arrayListSongs = arrayListSongs;
    }

    @NonNull
    @Override
    public UserSongsAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.song_item,parent, false);
        return new UserSongsAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserSongsAdapterViewHolder holder, int position) {
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

    public void filterList(ArrayList<Song> filteredList) {
        arrayListSongs = filteredList;
        notifyDataSetChanged();
    }

    public Song getModel() {
        return soong;
    }

    public void shareSong(int adapterPosition) {
        Song song = arrayListSongs.get(adapterPosition);
        String link = song.getLink();

        Intent sharingIntent = new Intent(Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(Intent.EXTRA_SUBJECT, "Write your subject here");
        sharingIntent.putExtra(Intent.EXTRA_TEXT, link);
        context.startActivity(Intent.createChooser(sharingIntent, "Share text via"));
    }

    public class UserSongsAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView titleTxt, durationTxt;
        String link;

        public UserSongsAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTxt = itemView.findViewById(R.id.song_title);
            durationTxt = itemView.findViewById(R.id.song_duration);
            itemView.setOnClickListener(this);
            itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {

                    ((MainPage)context).selectPlaylist(arrayListSongs,getAdapterPosition());

                    return false;
                }
            });
        }

        @Override
        public void onClick(View v) {

            ((MainPage)context).playSong(getAdapterPosition());
        }


    }
}
