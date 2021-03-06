package com.example.dowy.whatsapp.activity;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.AdapterView;
import android.widget.LinearLayout;

import com.example.dowy.whatsapp.R;
import com.example.dowy.whatsapp.adapter.ContactosAdapter;
import com.example.dowy.whatsapp.adapter.GrupoSelecionadoAdapter;
import com.example.dowy.whatsapp.config.ConfiguracaoFirebase;
import com.example.dowy.whatsapp.helper.RecyclerItemClickListener;
import com.example.dowy.whatsapp.helper.UsuarioFirebase;
import com.example.dowy.whatsapp.model.Usuario;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class GrupoActivity extends AppCompatActivity {

    private RecyclerView recyclerMembrosSelecionados;
    private RecyclerView recyclerMembros;
    private ContactosAdapter contactosAdapter;
    private GrupoSelecionadoAdapter grupoSelecionadoAdapter;
    private List<Usuario> listaMembros = new ArrayList<>();
    private List<Usuario> listaMembrosSelecionados = new ArrayList<>();
    private ValueEventListener valueEventListenerMembros;
    private DatabaseReference usuarioRef;
    private FirebaseUser usuarioActual;
    private Toolbar toolbar;
    private FloatingActionButton fabAvancarCadastro;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_grupo);
        toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Novo grupo");
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);


        // Configuracoes iniciais
        recyclerMembros = findViewById(R.id.recyclerMembros);
        recyclerMembrosSelecionados = findViewById(R.id.recyclerMembrosSelecionados);
        fabAvancarCadastro = findViewById(R.id.fabAvancarCadastro);

        usuarioRef = ConfiguracaoFirebase.getFirebaseDatabase().child("usuarios");
        usuarioActual = UsuarioFirebase.getUsuarioActual();

        // Configurar adapter
        contactosAdapter = new ContactosAdapter(listaMembros, getApplicationContext());

        // Configurar recyclerView pata contactos
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this);
        recyclerMembros.setLayoutManager(layoutManager);
        recyclerMembros.setHasFixedSize(true);
        recyclerMembros.setAdapter(contactosAdapter);

        recyclerMembros.addOnItemTouchListener(new RecyclerItemClickListener(
                getApplicationContext(),
                recyclerMembros,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Usuario usuarioSelecionado = listaMembros.get(position);

                        // Remover Usuario selecionado
                        listaMembros.remove(usuarioSelecionado);
                        contactosAdapter.notifyDataSetChanged();

                        // Adicionar Usuario selecionado a lista
                        listaMembrosSelecionados.add(usuarioSelecionado);
                        grupoSelecionadoAdapter.notifyDataSetChanged();
                        actualizarMembrosToolbar();
                    }

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }


                    @Override
                    public void onLongItemClick(View view, int position) {

                    }
                }));

        // Configurar o Recyclerview para os membros selecionados
        grupoSelecionadoAdapter = new GrupoSelecionadoAdapter(listaMembrosSelecionados, getApplicationContext());
        RecyclerView.LayoutManager layoutManager1 = new LinearLayoutManager(getApplicationContext(),
                LinearLayoutManager.HORIZONTAL,
                false);
        recyclerMembrosSelecionados.setLayoutManager(layoutManager1);
        recyclerMembrosSelecionados.setHasFixedSize(true);
        recyclerMembrosSelecionados.setAdapter(grupoSelecionadoAdapter);

        recyclerMembrosSelecionados.addOnItemTouchListener(new RecyclerItemClickListener(
                getApplicationContext(),
                recyclerMembrosSelecionados,
                new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        Usuario usuarioSeleccionado = listaMembrosSelecionados.get(position);
                        // Remover da lista de seleccionados
                        listaMembrosSelecionados.remove(usuarioSeleccionado);
                        grupoSelecionadoAdapter.notifyDataSetChanged();

                        // Adicionar na lista dos nao seleccionados
                        listaMembros.add(usuarioSeleccionado);
                        contactosAdapter.notifyDataSetChanged();
                        actualizarMembrosToolbar();
                    }

                    @Override
                    public void onLongItemClick(View view, int position) {

                    }

                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                    }
                }));

        fabAvancarCadastro.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(GrupoActivity.this, CadastroGrupoActivity.class);
                i.putExtra("membros", (Serializable) listaMembrosSelecionados);
                startActivity(i);
            }
        });

    }

    public void recuperarContactos() {


        valueEventListenerMembros = usuarioRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                listaMembros.clear();
                for (DataSnapshot dados : dataSnapshot.getChildren()) {
                    Usuario usuario = dados.getValue(Usuario.class);

                    String emailUsuarioActual = usuarioActual.getEmail();
                    if (!emailUsuarioActual.equals(usuario.getEmail())) {
                        listaMembros.add(usuario);
                    }
                }
                contactosAdapter.notifyDataSetChanged();
                actualizarMembrosToolbar();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void actualizarMembrosToolbar() {
        int totalSelecionados = listaMembrosSelecionados.size();
        int total = listaMembros.size() + totalSelecionados;
        toolbar.setSubtitle(totalSelecionados + " de " + total + " selecionados");
    }

    @Override
    protected void onStart() {
        super.onStart();
        recuperarContactos();
    }

    @Override
    protected void onStop() {
        super.onStop();

        usuarioRef.removeEventListener(valueEventListenerMembros);
    }
}
