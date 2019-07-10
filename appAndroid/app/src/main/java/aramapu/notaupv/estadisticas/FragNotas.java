package aramapu.notaupv.estadisticas;

import android.app.AlertDialog;
import android.app.SearchManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import java.util.ArrayList;

import aramapu.notaupv.R;

/**
 * Fragmento (vista) de la pestana NOTAS donde se muestra la relacion de Examinado-nota ordeanada
 * de mayor a menor nota.
 *
 * @author Mario Aragones Lozano
 */
public class FragNotas extends Fragment {

    private RecyclerView recyclerNotas;

    private ArrayList<Examinado> listaExaminados;
    private String nombreExaminado;

    private AdaptadorListaDinamicaNotas adaptador;

    //Widget para buscar en las notas
    private SearchView searchView = null;
    private SearchView.OnQueryTextListener queryTextListener;

    /**
     * Constructor vacio.
     *
     */
    public FragNotas() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //Habilitar menu
        setHasOptionsMenu(true);
    }

    /**
     * Metoodo donde se obtiene el mapa para pasarselo al adaptador y se crea el adapatador encargado
     * de generar la lista de forma dinamica
     *
     * @param inflater
     * @param container Container donde esta el fragmento
     * @param savedInstanceState
     * @return Vista del fragmento
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View vista = inflater.inflate(R.layout.fragment_notas, container, false);

        //Se define y establece el recycler view
        recyclerNotas = (RecyclerView) vista.findViewById(R.id.recyclerNotas);

        recyclerNotas.setHasFixedSize(true);
        recyclerNotas.setLayoutManager(new LinearLayoutManager(getContext()));

        listaExaminados = (ArrayList<Examinado>) getArguments().getSerializable("listaExaminados");
        nombreExaminado = (String) getArguments().getString("nombreExaminado");

        //Se crea el adapatador
        adaptador = new AdaptadorListaDinamicaNotas(listaExaminados);
        recyclerNotas.setAdapter(adaptador);

        return vista;
    }

    /**
     * Metodo encargado de establecer el menu especifico para este fragmento
     *
     * @param menu Menu del fragmento
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {

        inflater.inflate(R.menu.menu_estadisticas_notas, menu);

        //Se anade el filtro de nombres
        MenuItem searchItem = menu.findItem(R.id.menuEstadisticasNotas_itemBuscar);
        SearchManager searchManager = (SearchManager) getActivity().getSystemService(Context.SEARCH_SERVICE);

        if (searchItem != null) {
            searchView = (SearchView) searchItem.getActionView();

            //Se establece que el hint sea el nombre del examinado
            searchView.setQueryHint(nombreExaminado);
        }

        if (searchView != null) {
            searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));

            queryTextListener = new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextChange(String newText) {
                    Log.d("fragNotas_onCreateOptionsMenu_onQueryTextChange", "Se ha escrito " + newText);

                    if(newText.equals("")){
                        //Si no hay filtro
                        Log.d("fragNotas_onCreateOptionsMenu_onQueryTextChange","Se ha eliminado el filtro");
                        adaptador.actualizarElementos(listaExaminados);

                    }else{
                        //Si hay filtro
                        Log.d("fragNotas_onCreateOptionsMenu_onQueryTextChange","El filtro es " + newText);
                        ArrayList<Examinado> listaFiltrada = new ArrayList<Examinado>();

                        for(int i = 0; i < listaExaminados.size(); i++){
                            if(listaExaminados.get(i).getNombre().toLowerCase().contains(newText.toLowerCase())){
                                listaFiltrada.add(listaExaminados.get(i));
                            }
                        }

                        adaptador.actualizarElementos(listaFiltrada);
                    }

                    return true;
                }

                //No usado
                @Override
                public boolean onQueryTextSubmit(String query) {
                    Log.d("fragNotas_onCreateOptionsMenu_onQueryTextSubmit", query);
                    return true;
                }
            };

            searchView.setOnQueryTextListener(queryTextListener);

        }

        //Se establece el menu
        super.onCreateOptionsMenu(menu, inflater);
    }

    /**
     * Metodo encargado de devolver una accion al pulsar un boton
     *
     * @param item Item pulsado
     * @return Si ha sido satisfactorio
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle item selection
        switch (item.getItemId()) {
            case R.id.menuEstadisticasNotas_itemBuscar:
                return true;
            case R.id.menuEstadisticasNotas_itemAyuda:
                mostrarDialogoAyuda();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * Metodo encargado de mostrar el dialogo de la ayuda de las notas.
     *
     */
    private void mostrarDialogoAyuda(){

        AlertDialog.Builder constructorDialogo = new AlertDialog.Builder(FragNotas.this.getContext());

        LayoutInflater infladorDialogo = getLayoutInflater();

        View vistaDialogo = infladorDialogo.inflate(R.layout.dialog_estadisticas_notas_ayuda,null);

        constructorDialogo.setView(vistaDialogo);

        final AlertDialog dialogo = constructorDialogo.create();

        dialogo.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button btnOk = vistaDialogo.findViewById(R.id.butDialogEstadisticasNotasNotas_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogo.dismiss();
            }
        });

        dialogo.show();

    }

}
