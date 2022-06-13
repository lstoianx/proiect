package com.example.player;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class AllSongsAdapter extends RecyclerView.Adapter<AllSongsAdapter.AllSongsAdapterViewHolder> {

    Context context;
    List<Song> arrayListSongs;
    Song soong;

    public AllSongsAdapter(Context context, List<Song> arrayListSongs){
        this.context=context;
        this.arrayListSongs = arrayListSongs;
    }

    @NonNull
    @Override
    public AllSongsAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.song_item,parent, false);
        return new AllSongsAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AllSongsAdapterViewHolder holder, int position) {
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

    public void deleteSong(int adapterPosition) {

        Song song = arrayListSongs.get(adapterPosition);

        final String test = song.getUploadId();

        DatabaseReference drUser = FirebaseDatabase.getInstance().getReference("Songs").child(test);
        drUser.removeValue();

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Playlists");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot ds:snapshot.getChildren()){
                    if(ds.child("melodii").hasChild(test)){
                        ds.child("melodii").child(test).getRef().removeValue();
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        // ref.removeValue();
        // arrayListSongs.remove(adapterPosition);
        // notifyItemRemoved(adapterPosition);

        Toast.makeText(context, "The song has been deleted", Toast.LENGTH_SHORT).show();

    }

    public Song getModel() {
        return soong;
    }


    public class AllSongsAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        TextView titleTxt, durationTxt;
        String link;

        public AllSongsAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTxt = itemView.findViewById(R.id.song_title);
            durationTxt = itemView.findViewById(R.id.song_duration);
            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(View v) {

            ((DeleteSongActivity)context).playSong(getAdapterPosition());
        }


    }
}
