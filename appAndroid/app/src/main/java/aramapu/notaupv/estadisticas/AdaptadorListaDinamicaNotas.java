package aramapu.notaupv.estadisticas;

import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;

import aramapu.notaupv.R;

/**
 * Clase encargada de generar la lista de elementos de las notas a mostrar a partir de la plantilla
 * de elementos definida en el layout card_nota
 *
 * @author Mario Aragones Lozano
 */
public class AdaptadorListaDinamicaNotas extends RecyclerView.Adapter<AdaptadorListaDinamicaNotas.ViewHolderNotas> {

    private ArrayList<Examinado> listaExaminados;

    /**
     * Constructor
     *
     * @param listaExaminados Lista con los examinados
     */
    public AdaptadorListaDinamicaNotas(ArrayList<Examinado> listaExaminados){

        this.listaExaminados = listaExaminados;

    }

    /**
     * Clase encargada de obtener la plantilla que se usara en cada elementos de la lista
     *
     * @param parent
     * @param i
     * @return ViewHolderNotas para este objeto
     */
    @NonNull
    @Override
    public ViewHolderNotas onCreateViewHolder(@NonNull ViewGroup parent, int i) {

        // R.layout.card_nota es la plantilla que se usara para cada elemento de la lista
        View itemLayoutView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_nota,parent,false);

        // Se crea el elemento de la clase viewHolder
        ViewHolderNotas viewHolder = new ViewHolderNotas(itemLayoutView);

        return viewHolder;
    }

    /**
     * Crea los elementos de la lista a partir de la plantilla anteriormente declarada.
     * Hay tantos elementos como devuelve getItemCount.
     *
     * @param viewHolderNotas ViewHolder anteriormente creando
     * @param posicionLista Posicion actual de la lista
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolderNotas viewHolderNotas, int posicionLista) {

        //La informacion la recoge del mapa pasado por el constructor
        viewHolderNotas.txtViewNombre.setText( listaExaminados.get(posicionLista).getNombre() );

        if(listaExaminados.get(posicionLista).getNota() <0){
            viewHolderNotas.txtViewNota.setText("");
            viewHolderNotas.txtViewPos.setText("");
        }else{
            viewHolderNotas.txtViewNota.setText(""+ listaExaminados.get(posicionLista).getNota() );
            viewHolderNotas.txtViewPos.setText(""+listaExaminados.get(posicionLista).getPosicion() + " (" + listaExaminados.get(posicionLista).getNumeroNotasIguales() + ")");
        }

    }

    /**
     * Devuelve la cantidad de elementos que tiene que tener la lista a partir de los elementos del mapa
     *
     * @return Elementos de la lista
     */
    @Override
    public int getItemCount() {
        //Si es null, no hay elementos
        if(listaExaminados != null){
            return listaExaminados.size();
        }else{
            return 0;
        }

    }

    /**
     * Metodo encargado de actualizar los elementos mostrados por los obtenidos
     *
     * @param listaActualizada Elementos a mostrar
     */
    public void actualizarElementos(ArrayList<Examinado> listaActualizada){
        this.listaExaminados = listaActualizada;
        notifyDataSetChanged();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
    }

    /**
     * Clase encargada de relacionar los objetos del layout con cada elemento del RecyclerView para
     * elaborar la lista dinamica repitiendo estos elementos como plantilla.
     */
    public static class ViewHolderNotas extends RecyclerView.ViewHolder {

        private TextView txtViewNombre;
        private TextView txtViewNota;
        private TextView txtViewPos;

        public ViewHolderNotas(@NonNull View itemView) {
            super(itemView);

            txtViewNombre = (TextView) itemView.findViewById(R.id.txtViewCardNotaNombre);
            txtViewNota = (TextView) itemView.findViewById(R.id.txtViewCardNotaNota);
            txtViewPos = (TextView) itemView.findViewById(R.id.txtViewCardNotaPos);

        }
    }
    //Fin clase ViewHolderNotas

}
