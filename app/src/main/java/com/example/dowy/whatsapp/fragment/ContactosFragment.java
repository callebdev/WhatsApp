package com.example.dowy.whatsapp.fragment;


import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Toast;

import com.example.dowy.whatsapp.R;
import com.example.dowy.whatsapp.activity.ChatActivity;
import com.example.dowy.whatsapp.activity.GrupoActivity;
import com.example.dowy.whatsapp.adapter.ContactosAdapter;
import com.example.dowy.whatsapp.adapter.ConversasAdapter;
import com.example.dowy.whatsapp.config.ConfiguracaoFirebase;
import com.example.dowy.whatsapp.helper.RecyclerItemClickListener;
import com.example.dowy.whatsapp.helper.UsuarioFirebase;
import com.example.dowy.whatsapp.model.Conversa;
import com.example.dowy.whatsapp.model.Usuario;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactosFragment extends Fragment {

    private RecyclerView recyclerViewListaContactos;
    private ContactosAdapter adapter;
    private ArrayList<Usuario> listaContactos = new ArrayList<>();
    private DatabaseReference usuariosRef;
    private ValueEventListener valueEventListenerContactos;
    private FirebaseUser usuarioActual;

    public ContactosFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_contactos, container, false);

        //Configuracoes Iniciais
        recyclerViewListaContactos = view.findViewById(R.id.contactos_recyclerView);
        usuariosRef = ConfiguracaoFirebase.getFirebaseDatabase().child("usuarios");
        usuarioActual = UsuarioFirebase.getUsuarioActual();
        //Adapter Config
        adapter = new ContactosAdapter(listaContactos, getActivity());

        //RecyclerView Config
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        recyclerViewListaContactos.setLayoutManager(layoutManager);
        recyclerViewListaContactos.setHasFixedSize(true);
        recyclerViewListaContactos.setAdapter(adapter);

        //Config Click Events on RecyclerView
        recyclerViewListaContactos.addOnItemTouchListener(new RecyclerItemClickListener(getActivity(), recyclerViewListaContactos, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {

                // Para melhor usar o searchview no Fragmento de Contactos
                List<Usuario> listaUsuariosActualizada = adapter.getContactos();
                Usuario usuarioSelecionado = listaUsuariosActualizada.get(position);
                boolean cabecalho = usuarioSelecionado.getEmail().isEmpty();

                if (cabecalho) {
                    Intent i = new Intent(getActivity(), GrupoActivity.class);
                    startActivity(i);
                } else {
                    Intent i = new Intent(getActivity(), ChatActivity.class);
                    i.putExtra("chatContacto", usuarioSelecionado);
                    startActivity(i);
                }

            }

            @Override
            public void onLongItemClick(View view, int position) {

            }

            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

            }
        }));

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        recuperarContactos();
    }

    @Override
    public void onStop() {
        super.onStop();
        usuariosRef.removeEventListener(valueEventListenerContactos);
    }

    public void recuperarContactos() {
        listaContactos.clear();

        Usuario itemGrupo = new Usuario();
        itemGrupo.setNome("Novo Grupo");
        itemGrupo.setEmail("");

        listaContactos.add(itemGrupo);
        valueEventListenerContactos = usuariosRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                //listaContactos.clear();
                for (DataSnapshot dados : dataSnapshot.getChildren()) {
                    Usuario usuario = dados.getValue(Usuario.class);
                    String emailUsuarioActual = usuarioActual.getEmail();
                    if (!emailUsuarioActual.equals(usuario.getEmail())) {
                        listaContactos.add(usuario);

                    }
                }

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    public void pesquisarContactos(String texto) {
        List<Usuario> listaContactosBusca = new ArrayList<>();
        for (Usuario usuario : listaContactos) {

            String nome = usuario.getNome().toLowerCase();
            if (nome.contains(texto)) {
                listaContactosBusca.add(usuario);
            }
        }

        adapter = new ContactosAdapter(listaContactos, getActivity());
        recyclerViewListaContactos.setAdapter(adapter);
        adapter.notifyDataSetChanged();

    }

    public void recarregarContactos() {
        adapter = new ContactosAdapter(listaContactos, getActivity());
        recyclerViewListaContactos.setAdapter(adapter);
        adapter.notifyDataSetChanged();
    }

}
