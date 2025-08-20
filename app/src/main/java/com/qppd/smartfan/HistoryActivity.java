package com.qppd.smartfan;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

public class HistoryActivity extends AppCompatActivity {
    private RecyclerView recyclerViewLogs;
    private LogAdapter adapter;
    private ArrayList<String> logsList = new ArrayList<>();
    private DatabaseReference dbRef;
    private String uid, linkedDeviceId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_history);

        recyclerViewLogs = findViewById(R.id.recyclerViewLogs);
        recyclerViewLogs.setLayoutManager(new LinearLayoutManager(this));
        adapter = new LogAdapter();
        recyclerViewLogs.setAdapter(adapter);

        dbRef = FirebaseDatabase.getInstance().getReference();
        uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // Get linked device
        dbRef.child("users").child(uid).child("devices").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot deviceSnap : snapshot.getChildren()) {
                        linkedDeviceId = deviceSnap.getKey();
                        break;
                    }
                    loadLogs();
                } else {
                    Toast.makeText(HistoryActivity.this, "No device linked.", Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    private void loadLogs() {
        dbRef.child("devices").child(linkedDeviceId).child("logs").limitToLast(50).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot snapshot) {
                logsList.clear();
                for (DataSnapshot logSnap : snapshot.getChildren()) {
                    Long timestamp = logSnap.child("timestamp").getValue(Long.class);
                    Double temp = logSnap.child("temperature").getValue(Double.class);
                    Long fanSpeed = logSnap.child("fanSpeed").getValue(Long.class);
                    String timeStr = timestamp != null ? new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(new Date(timestamp * 1000)) : "-";
                    logsList.add(timeStr + " | Temp: " + temp + "°C | Fan: " + fanSpeed);
                }
                adapter.notifyDataSetChanged();
            }
            @Override
            public void onCancelled(DatabaseError error) {}
        });
    }

    // RecyclerView Adapter for logs
    private class LogAdapter extends RecyclerView.Adapter<LogAdapter.LogViewHolder> {

        @NonNull
        @Override
        public LogViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
            return new LogViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull LogViewHolder holder, int position) {
            holder.textView.setText(logsList.get(position));
        }

        @Override
        public int getItemCount() {
            return logsList.size();
        }

        class LogViewHolder extends RecyclerView.ViewHolder {
            TextView textView;

            LogViewHolder(@NonNull View itemView) {
                super(itemView);
                textView = itemView.findViewById(android.R.id.text1);
            }
        }
    }
}
