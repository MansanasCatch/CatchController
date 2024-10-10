package com.example.catchcontroller;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;

public class CommandAdapter extends FirebaseRecyclerAdapter<ModelCommand,CommandAdapter.myViewHolder> {

    public CommandAdapter(@NonNull FirebaseRecyclerOptions<ModelCommand> options) {
        super(options);
    }

    @Override
    protected void onBindViewHolder(@NonNull myViewHolder holder, int position, @NonNull ModelCommand model) {
        holder.tvCommand.setText(model.command);
        holder.tvCommandType.setText(model.commandType);
        holder.tvKey.setText(model.key);
    }

    @NonNull
    @Override
    public myViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.command_item,parent,false);
        return new myViewHolder(view);
    }

    class myViewHolder extends RecyclerView.ViewHolder{

        TextView tvCommand, tvKey,tvCommandType;

        public myViewHolder(@NonNull View itemView) {
            super(itemView);

            tvCommandType= itemView.findViewById(R.id.tvCommandType);
            tvCommand = itemView.findViewById(R.id.tvCommand);
            tvKey=itemView.findViewById(R.id.tvKey);
        }
    }

}
