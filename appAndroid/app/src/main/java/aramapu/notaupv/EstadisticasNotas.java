package aramapu.notaupv;


import android.support.design.widget.TabLayout;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;

import aramapu.notaupv.estadisticas.AdaptadorPaginaSecciones;
import aramapu.notaupv.estadisticas.Examinado;

/**
 * Clase encargada de generar la actividad encargada de mostrar las estadisticas de una prueba dada.
 *
 * @author Mario Aragones Lozano
 */
public class EstadisticasNotas extends AppCompatActivity {

    private String jsonNotas;
    private double notaMaximaExamen;

    private String nombreExaminado;
    private String asignatura;
    private String prueba;

    private ArrayList<Examinado> listaExaminados;

    private int aprobados;
    private int suspendidos;
    private int enBlanco;
    private int total;

    private double media;
    private double notaMaxima;
    private double notaMinima;

    /**
     * Metodo llamado al crear la clase.
     * Necesita en el Bundle:
     *  "jsonNotas": String con el JSON de las notas
     *  "notaMaximaExamen": Double con la nota maxima del examen
     *  "nombreExaminado": String con el nombre del Examinado
     *  "asignatura": String con la asignatura
     *  "prueba": String con la prueba
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_estadisticas_notas);

        //Barra superior
        Toolbar barraOpciones = findViewById(R.id.toolbarEstadisticasNotas);
        barraOpciones.setTitle(getResources().getString(R.string.title_activity_estadisticas_notas));
        setSupportActionBar(barraOpciones);

        //Mostrar boton atras
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        barraOpciones.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });

        //Informacion obtenida
        jsonNotas = getIntent().getExtras().getString("jsonNotas");
        notaMaximaExamen = getIntent().getExtras().getDouble("notaMaximaExamen");

        nombreExaminado = getIntent().getExtras().getString("nombreExaminado");
        asignatura = getIntent().getExtras().getString("asignatura");
        prueba = getIntent().getExtras().getString("prueba");

        //Lista de objeto examinados
        listaExaminados = generarListaExaminados(generarMapa(jsonNotas));

        establecerEstadisticas();

        int[] datosEnteros = new int[]{aprobados,suspendidos,enBlanco,total};
        double[] datosFraccionarios = new double[]{media,notaMaxima,notaMinima};

        //Pestanas (Tabs)
        AdaptadorPaginaSecciones adaPagSec = new AdaptadorPaginaSecciones(getSupportFragmentManager(),this,listaExaminados,nombreExaminado,asignatura,prueba,datosEnteros,datosFraccionarios);

        ViewPager vistaPaginaEstadisticasNotas = findViewById(R.id.view_pager_Estadisticas_Notas);
        vistaPaginaEstadisticasNotas.setAdapter(adaPagSec);

        TabLayout pestanasEstadisticasNotas = findViewById(R.id.tabsEstadisticasNotas);
        pestanasEstadisticasNotas.setupWithViewPager(vistaPaginaEstadisticasNotas);

    }

    /**
     * Metodo encargado de establecer las estadisticas con las variables del objeto.
     *
     */
    private void establecerEstadisticas(){

        aprobados = 0;
        suspendidos = 0;
        enBlanco = 0;
        total = 0;

        notaMaxima = listaExaminados.get(0).getNota();
        notaMinima = listaExaminados.get(0).getNota();

        for(Examinado examinado : listaExaminados){

            double nota = examinado.getNota();

            if(nota >= notaMaximaExamen/2){
                aprobados++;
            }else if(nota >= 0){
                suspendidos++;
            }else{
                enBlanco++;
            }

            if(nota > notaMaxima){
                notaMaxima = nota;
            }

            if(nota >= 0 && nota < notaMinima){
                notaMinima = nota;
            }

            if(nota >=0){
                media += nota;
            }

        }

        total = aprobados+suspendidos+enBlanco;

        media /= (aprobados+suspendidos);

    }


    /**
     * Metodo encargado de generar el mapa Examinado-nota a partir del JSON
     *
     * @param jsonNotas JSON con la informacion
     * @return Mapa desordenado
     */
    private TreeMap<String,Double> generarMapa(String jsonNotas){

        TreeMap<String,Double> mapaExaminados = new TreeMap<String,Double>();

        try {
            //Se analiza el JSON recibido
            JSONObject objeto = new JSONObject(jsonNotas);

            JSONArray json_array = objeto.optJSONArray("notas");

            for (int i = 0; i < json_array.length(); i++) {

                //Se verifica que no hay ningun error en el valor de la nota
                try{
                    mapaExaminados.put(json_array.getJSONObject(i).getString("nombre"),json_array.getJSONObject(i).getDouble("nota"));
                }catch(JSONException ex){
                    Log.w("EstadisticasNotas","El JSON tiene valor NO numericos.");
                    Log.w("EstadisticasNotas",ex.toString());
                }

            }

        }catch (JSONException e){
            Log.w("EstadisticasNotas","El JSON generado por la clase sesion intranet esta mal definido");
            Log.w("EstadisticasNotas",e.toString());
        }

        return mapaExaminados;

    }


    /**
     * Clase encargada de generar una lista de examinados ordenada por el valor de la nota de forma descendente
     *
     * @param mapa Mapa desordenado
     * @return Lista de examinados
     */
    private ArrayList<Examinado> generarListaExaminados(TreeMap<String,Double> mapa){

        //Se obtiene el mapa ordenado de forma descendente en el sortedSet
        SortedSet set = entriesSortedByValues( mapa );
        Object[] arrayExaminados = set.toArray();

        //Se crea un nuevo mapa
        ArrayList<Examinado> listaDeExaminados = new ArrayList<Examinado>();

        //Posicion que ocupa cierta nota en la lista
        int posicionLista = 1;

        //Nota a comparar la cantidad de iguales
        double notaCompararCantidadIgual = -2;

        //Numero de notas iguales. Se inicializa a 0, si hay alguna nota cambiara el valor
        int numeroNotasIguales = 0;

        //Indice de la matriz para empezar a recorrer
        int indexInicial = 0;

        //Se llena el mapa de forma ordenada
        Iterator it = set.iterator();
        while(it.hasNext()){

            //Se obtiene la fila actual
            Map.Entry<String,Double> entradaMapa = (Map.Entry<String,Double>) it.next();

            if(notaCompararCantidadIgual == -2) {

                //Se incrementa el numero de notas iguales
                numeroNotasIguales++;

                notaCompararCantidadIgual = entradaMapa.getValue();

            }else if(notaCompararCantidadIgual != entradaMapa.getValue()){

                for(int i = 0; i < numeroNotasIguales;i++){
                    Map.Entry<String,Double> examinado = (Map.Entry<String,Double>) arrayExaminados[i+indexInicial];
                    listaDeExaminados.add(new Examinado(examinado.getKey(),examinado.getValue(),posicionLista,numeroNotasIguales));
                }

                indexInicial+=numeroNotasIguales;

                posicionLista++;
                notaCompararCantidadIgual = entradaMapa.getValue();

                numeroNotasIguales = 1;

            }else{
                //Se incrementa el numero de notas iguales
                numeroNotasIguales++;

            }

        }

        if(numeroNotasIguales!=0){
            for(int i = 0; i < numeroNotasIguales;i++){
                Map.Entry<String,Double> examinado = (Map.Entry<String,Double>) arrayExaminados[i+indexInicial];
                listaDeExaminados.add(new Examinado(examinado.getKey(),examinado.getValue(),posicionLista,numeroNotasIguales));
            }
        }

        return listaDeExaminados;

    }

    /**
     * Metodo encargado de ordenar el mapa de forma descendente.
     * Se ha obtenido de internet, aunque modificado para que sea de forma descendente.
     *
     * @param map Mapa desordenado
     * @param <K> Clave
     * @param <V> Valor
     * @return SortedSet ordeando
     */
    private static <K,V extends Comparable<? super V>> SortedSet<Map.Entry<K,V>> entriesSortedByValues(Map<K,V> map) {

        SortedSet<Map.Entry<K,V>> sortedEntries = new TreeSet<Map.Entry<K,V>>(
                new Comparator<Map.Entry<K,V>>() {
                    @Override public int compare(Map.Entry<K,V> e1, Map.Entry<K,V> e2) {

                        //OJO, el - es para que sea orden inverso.
                        int res = - e1.getValue().compareTo(e2.getValue());

                        return res != 0 ? res : 1;
                    }
                }
        );

        sortedEntries.addAll(map.entrySet());
        return sortedEntries;
    }

}