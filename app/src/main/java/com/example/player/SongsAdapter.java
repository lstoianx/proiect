package com.example.player;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class SongsAdapter extends RecyclerView.Adapter<SongsAdapter.SongsAdapterViewHolder> {

    Context context;
    List<Song> arrayListSongs;

    public SongsAdapter(Context context, List<Song> arrayListSongs){
        this.context=context;
        this.arrayListSongs = arrayListSongs;
    }

    @NonNull
    @Override
    public SongsAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.song_item,parent, false);
        return new SongsAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongsAdapterViewHolder holder, int position) {
        Song song = arrayListSongs.get(position);
        holder.titleTxt.setText(song.getStageName()+" - "+song.getTitle());
        holder.durationTxt.setText(song.getDuration());
        holder.uid = song.getUid();

    }

    @Override
    public int getItemCount() {
        return arrayListSongs.size();
    }

    public class SongsAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener{
        TextView titleTxt, durationTxt;
        String uid;

        public SongsAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTxt = itemView.findViewById(R.id.song_title);
            durationTxt = itemView.findViewById(R.id.song_duration);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {
            //ruleaza melodia selectata

            //deschide fereastra
            ((AdminActivity)context).openPlayerActivity(getAdapterPosition(), titleTxt.getText().toString(), uid, durationTxt.getText().toString());



           /* try {
                ((AdminActivity)context).playSong(arrayListSongs,getAdapterPosition());
            } catch (IOException e) {
                e.printStackTrace();
            }*/
        }


    }
}
