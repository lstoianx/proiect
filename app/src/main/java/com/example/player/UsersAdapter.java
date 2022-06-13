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
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class UsersAdapter extends RecyclerView.Adapter<UsersAdapter.UsersAdapterViewHolder>{

    Context context;
    List<User> arrayListUsers;
    List<User> arrayListUsersFull;

    public UsersAdapter(Context context, List<User> arrayListUsers){
        this.context=context;
        this.arrayListUsers = arrayListUsers;
        arrayListUsersFull = new ArrayList<>(arrayListUsers); //copierea listei initiale in alta variabila
    }

    @NonNull
    @Override
    public UsersAdapterViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.user_item,parent, false);
        return new UsersAdapterViewHolder(view);
    }

    public void banUser(int position){

        User user = arrayListUsers.get(position);

        final DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Users").child(user.getUid());

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                final HashMap<String, Object> userType = new HashMap<>();
                userType.put("userType", "banned");
                ref.updateChildren(userType);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        final DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference("Applications");
        final Query query = ref2.orderByChild("uid").equalTo(user.getUid());
        query.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                snapshot.getRef().setValue(null);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        Toast.makeText(context, "The account has been banned", Toast.LENGTH_SHORT).show();
        notifyDataSetChanged();

    }

    @Override
    public void onBindViewHolder(@NonNull final UsersAdapterViewHolder holder, final int position) {
        final User user = arrayListUsers.get(position);

        String firstName = user.getFirstName();
        String lastName = user.getLastName();

        holder.nameTxt.setText("Name: "+firstName+" "+lastName);
        holder.emailTxt.setText("Email: "+user.getEmail());
        holder.userTypeTxt.setText("Type: "+user.getUserType());

        if(Objects.equals(user.getUserType(), "artist")){
            holder.stageNameTxt.setText("Stage name: "+user.getStageName());
        }
    }

    @Override
    public int getItemCount() {
        return arrayListUsers.size();
    }

    public void filterList(ArrayList<User> filteredList) {
        arrayListUsers = filteredList;
        notifyDataSetChanged();
    }


    public class UsersAdapterViewHolder extends RecyclerView.ViewHolder {
        TextView nameTxt, stageNameTxt, emailTxt, userTypeTxt;

        public UsersAdapterViewHolder(@NonNull View itemView) {
            super(itemView);

            nameTxt = itemView.findViewById(R.id.user_name);
            stageNameTxt = itemView.findViewById(R.id.user_stageName);
            emailTxt = itemView.findViewById(R.id.user_email);
            userTypeTxt = itemView.findViewById(R.id.user_type);

        }
    }
}
