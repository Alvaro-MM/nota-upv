package aramapu.notaupv.estadisticas;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.Locale;

import aramapu.notaupv.R;

/**
 * Fragmento donde se mostraran las estadisticas de la prueba en cuestion.
 *
 * @author Mario Aragones Lozano
 */
public class FragEstadisticas extends Fragment {

    public FragEstadisticas() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Metodo encargado de establecer el layout
     *
     * @param inflater
     * @param container Container donde esta el fragmento
     * @param savedInstanceState
     * @return Vista del fragmento
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        //Vista de los elementos
        View vista = inflater.inflate(R.layout.fragment_estadisticas, container, false);

        //Obtenemos los datos recibidos del programa principal
        String asignatura = getArguments().getString("asignatura");
        String prueba = getArguments().getString("prueba");

        int[] datosEnteros = getArguments().getIntArray("datosEnteros");
        double[] datosFraccionarios = getArguments().getDoubleArray("datosFraccionarios");

        //Obtener los TxtView
        TextView txtViewAsignatura = vista.findViewById(R.id.txtViewEstadisticasAsignatura);
        TextView txtViewPrueba = vista.findViewById(R.id.txtViewEstadisticasPrueba);
        TextView txtViewNumAprobados = vista.findViewById(R.id.txtViewEstadisticasNumAprobados);
        TextView txtViewNumSuspendidos = vista.findViewById(R.id.txtViewEstadisticasNumSuspendidos);
        TextView txtViewNumEnBlanco = vista.findViewById(R.id.txtViewEstadisticasNumEnBlanco);
        TextView txtViewNumTotal = vista.findViewById(R.id.txtViewEstadisticasNumTotal);
        TextView txtViewNotaMedia = vista.findViewById(R.id.txtViewEstadisticasNotaMedia);
        TextView txtViewNotaMaxima = vista.findViewById(R.id.txtViewEstadisticasNotaMaxima);
        TextView txtViewNotaMinima = vista.findViewById(R.id.txtViewEstadisticasNotaMinima);

        //Asignar los datos a los TxtView
        txtViewAsignatura.setText(asignatura);
        txtViewPrueba.setText(prueba);

        txtViewNumAprobados.setText(""+datosEnteros[0] + " (" + String.format(Locale.US,"%.2f",datosEnteros[0]*100.0/datosEnteros[3] ) +"%)" );
        txtViewNumSuspendidos.setText(""+datosEnteros[1] + " (" + String.format(Locale.US,"%.2f",datosEnteros[1]*100.0/datosEnteros[3] ) +"%)");
        txtViewNumEnBlanco.setText(""+datosEnteros[2] + " (" + String.format(Locale.US,"%.2f",datosEnteros[2]*100.0/datosEnteros[3] ) +"%)");
        txtViewNumTotal.setText(""+datosEnteros[3] );

        txtViewNotaMedia.setText(String.format(Locale.US,"%.2f", datosFraccionarios[0]));
        txtViewNotaMaxima.setText(String.format(Locale.US,"%.2f", datosFraccionarios[1]));
        txtViewNotaMinima.setText(String.format(Locale.US,"%.2f", datosFraccionarios[2]));

        return vista;
    }


}
