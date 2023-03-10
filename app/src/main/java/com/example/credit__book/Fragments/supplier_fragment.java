package com.example.credit__book.Fragments;

import static java.lang.Float.parseFloat;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.example.credit__book.Activities.AddSupplierActivity;
import com.example.credit__book.Activities.MyApplication;
import com.example.credit__book.Activities.ViewSupplierDetailsActivity;
import com.example.credit__book.Adapter.SupplierAdapter;
import com.example.credit__book.Model.SessionManager;
import com.example.credit__book.Model.Supplier;
import com.example.credit__book.R;
import com.example.credit__book.RecycleViewClientInterface;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Locale;
import java.util.Objects;

public class supplier_fragment extends Fragment  implements View.OnClickListener, RecycleViewClientInterface {

    SupplierAdapter supplierAdapter;
    RecyclerView recyclerView;
    SearchView searchView;
    FloatingActionButton ajouter;
    DatabaseReference database;
    ArrayList<Supplier> suppliersList;
    TextView nbr_suppliers;
    TextView Cashin, Cashout;
    float cashIn = 0;
    float cashOut = 0;
    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_supplier, container, false);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MyApplication context = (MyApplication) this.getActivity().getApplicationContext();
displayCashinAndCashOut();
        nbr_suppliers=view.findViewById(R.id.textViewSupplierNbr);
        searchView = view.findViewById(R.id.searchView);
        searchView.clearFocus();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }
        });

        recyclerView = view.findViewById(R.id.recyclerViewSupplier);
        SessionManager sessionManager = new SessionManager(context);
        HashMap<String, String> data = sessionManager.getUserDetails();
        database = FirebaseDatabase.getInstance().getReference("suppliers "+ data.get(SessionManager.TELEPHONE));
        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(context));

        swipeRefreshLayout=view.findViewById(R.id.refresh);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                cashOut=0;
                cashIn=0;
               displayCashinAndCashOut();
                swipeRefreshLayout.setRefreshing(false);
            }
        });

        suppliersList = new ArrayList<>();
        supplierAdapter = new SupplierAdapter(this, context, suppliersList);
        recyclerView.setAdapter(supplierAdapter);
        Cashin = view.findViewById(R.id.cashin);
        Cashout = view.findViewById(R.id.cashout);
        ajouter= view.findViewById(R.id.btnAddsupplier);
        ajouter.setOnClickListener(this);

        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                if(dy>0){
                    ajouter.hide();
                }else{
                    ajouter.show();
                }
            }
        });

        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                suppliersList.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Supplier supplier = dataSnapshot.getValue(Supplier.class);
                    suppliersList.add(supplier);
                }
                supplierAdapter.notifyDataSetChanged();
                nbr_suppliers.setText("Suppliers ("+suppliersList.size()+")");

            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void filterList(String text) {
        ArrayList<Supplier> filteredList = new ArrayList<>();
        for (Supplier item : suppliersList ){
            if (item.getFull_name().toLowerCase().contains(text.toLowerCase())){
                filteredList.add(item);
            }
        }
        if (filteredList.isEmpty()){
            Toast.makeText(this.getActivity(), "No Data Found !", Toast.LENGTH_SHORT).show();
        }
        else {
            supplierAdapter.setFilteredList(filteredList);
        }
    }

    @Override
    public void onClick(View view) {
        Intent intent=new Intent(getActivity(), AddSupplierActivity.class);
        startActivity(intent);
    }

    @Override
    public void onItemClick(int position) {
        Intent intent = new Intent(getContext(), ViewSupplierDetailsActivity.class);
        intent.putExtra("Supplier Name", suppliersList.get(position).getFull_name());
        intent.putExtra("Supplier Phone", suppliersList.get(position).getPhone_number());
        intent.putExtra("Supplier Email", suppliersList.get(position).getEmail());
        intent.putExtra("Supplier Address", suppliersList.get(position).getAddress());
        startActivity(intent);
    }
    public void displayCashinAndCashOut() {
        FirebaseDatabase dbRef = FirebaseDatabase.getInstance();

        dbRef.getReference("OperationsSuppliers").child(Objects.requireNonNull(new SessionManager(getContext()).getUserDetails().get(SessionManager.TELEPHONE))).get().addOnSuccessListener(new OnSuccessListener<DataSnapshot>() {
            @Override
            public void onSuccess(DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                        for (DataSnapshot snapshot1 : snapshot.getChildren()) {
                            if (snapshot1.child("operationType").getValue().toString().equalsIgnoreCase("Cash In")) {
                                cashIn += parseFloat(snapshot1.child("balance_supplier").getValue().toString());
                            } else {
                                cashOut += parseFloat(snapshot1.child("balance_supplier").getValue().toString());
                            }
                        }
                    }
                }
                Log.i("debug", "cashIn: " + cashIn + " cashOut: " + cashOut);
                // format to morrocan dirham
                NumberFormat formatter = NumberFormat.getCurrencyInstance(new Locale("fr", "MA"));
                Cashin.setText(formatter.format(cashIn));
                Cashout.setText(formatter.format(cashOut));
            }
        }).addOnFailureListener(new OnFailureListener() {
            @Override
            public void onFailure(@NonNull Exception e) {
                Log.e("debug", "onFailure: " + e.getMessage());
            }
        });
    }
}