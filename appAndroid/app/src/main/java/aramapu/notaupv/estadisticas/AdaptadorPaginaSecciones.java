package aramapu.notaupv.estadisticas;


import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import java.util.ArrayList;

import aramapu.notaupv.R;

/**
 * Esta clase es la encargada de transformar una actividad normal en una actividad basada en pestanas.
 * En cada pestana habra un elemento de tipo fragmento donde se realizaran las acciones
 *
 * @author Mario Aragones Lozano
 */
public class AdaptadorPaginaSecciones extends FragmentPagerAdapter {

    @StringRes
    private static final int[] NOMBRE_PESTANAS = new int[]{R.string.adaptadorPaginaSecciones_pestana1, R.string.adaptadorPaginaSecciones_pestana2};
    private final Context contexto;

    private ArrayList<Examinado> listaExaminados;
    private String nombreExaminado;

    private String asignatura;
    private String prueba;

    private int[] datosEnteros;
    private double[] datosFraccionarios;

    /**
     * Constructor de la clase
     *
     * @param fm Fragment manager a emplear
     * @param contexto Contexto de la aplicacion
     * @param listaExaminados Lista con los examinados en la prueba ordenados por nota de forma descendiente.
     * @param nombreExaminado Nombre de la persona que ha iniciado sesion
     * @param asignatura Asignatura
     * @param prueba Prueba de la asignatura
     * @param datosEnteros [0] -> Aprobados, [1] -> Suspendidos, [2] -> En Blanco, [3] -> Total
     * @param datosFraccionarios [0] -> Media, [1]-> N. Maxima, [2]-> N. Minima
     */
    public AdaptadorPaginaSecciones(FragmentManager fm, Context contexto, ArrayList<Examinado> listaExaminados, String nombreExaminado, String asignatura, String prueba, int[] datosEnteros, double[] datosFraccionarios) {
        super(fm);
        this.contexto = contexto;

        this.listaExaminados = listaExaminados;
        this.nombreExaminado = nombreExaminado;

        this.asignatura = asignatura;
        this.prueba = prueba;

        this.datosEnteros = datosEnteros;
        this.datosFraccionarios = datosFraccionarios;
    }

    /**
     * Clase encargada de escoger el fragmento a mostrar en la actividad
     *
     * @param posicion Vista actual seleccionada.
     * @return Fragmento a mostrar.
     */
    @Override
    public Fragment getItem(int posicion) {

        Bundle b;

        switch(posicion){
            case 0 :
                FragEstadisticas frEstadisticas = new FragEstadisticas();
                b = new Bundle();
                b.putString("asignatura",asignatura);
                b.putString("prueba",prueba);
                b.putIntArray("datosEnteros",datosEnteros);
                b.putDoubleArray("datosFraccionarios",datosFraccionarios);
                frEstadisticas.setArguments(b);
                return frEstadisticas;
            case 1 :
                FragNotas frNotas = new FragNotas();
                b = new Bundle();
                b.putSerializable("listaExaminados",listaExaminados);
                b.putString("nombreExaminado",nombreExaminado);
                frNotas.setArguments(b);
                return frNotas;
        }

        return null;
    }

    /**
     * Numero total de pestanas (Tabs)
     *
     * @return Numero total de pestanas
     */
    @Override
    public int getCount() {
        return NOMBRE_PESTANAS.length;
    }

    /**
     * Devuelve el titulo de cada pestana
     *
     * @param position Posicion de la pestana
     * @return Nombre de dicha pestana
     */
    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return contexto.getResources().getString(NOMBRE_PESTANAS[position]);
    }

}
