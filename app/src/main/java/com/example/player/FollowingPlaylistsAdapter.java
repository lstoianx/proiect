package com.example.player;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Objects;

public class FollowingPlaylistsAdapter extends RecyclerView.Adapter<FollowingPlaylistsAdapter.FollowingPlaylistsAdapterViewHolder> {

    Context context;
    List<Playlist> arrayListPlaylists;

    public FollowingPlaylistsAdapter(Context context, List<Playlist> arrayListPlaylists){
        this.context=context;
        this.arrayListPlaylists = arrayListPlaylists;
    }

    @NonNull
    @Override
    public FollowingPlaylistsAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.playlist_item,parent, false);
        return new FollowingPlaylistsAdapterViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FollowingPlaylistsAdapterViewHolder holder, int position) {
        Playlist playlist= arrayListPlaylists.get(position);
        holder.titleTxt.setText(playlist.getTitle());
        holder.playlistPhoto.setScaleType(ImageView.ScaleType.FIT_XY);
        String playlistImage = playlist.getImage();
        if(!Objects.equals(playlistImage, "")){
            Picasso.get().load(playlist.getImage()).into(holder.playlistPhoto);
        }
        else{

        }
    }

    @Override
    public int getItemCount() {
        return arrayListPlaylists.size();
    }


    public class FollowingPlaylistsAdapterViewHolder extends RecyclerView.ViewHolder implements View.OnLongClickListener {
        TextView titleTxt;
        ImageView playlistPhoto;

        public FollowingPlaylistsAdapterViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTxt = itemView.findViewById(R.id.numePlaylist);
            playlistPhoto = itemView.findViewById(R.id.imagePlaylist);
            itemView.setOnLongClickListener(this);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ((FollowingPlaylistsActivity)context).openPlaylistActivity(arrayListPlaylists,getAdapterPosition());
                }
            });

        }

        @Override
        public boolean onLongClick(View v) {
            ((FollowingPlaylistsActivity)context).deletePlaylist(arrayListPlaylists,getAdapterPosition());
            return false;
        }
    }
}
