package aramapu.notaupv;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.AsyncTask;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import aramapu.notaupv.estadisticas.FragNotas;

/**
 * Clase encargada de llevar a cabo el menú de selección de asignaturas y pruebas.
 *
 * @author Mario Aragones Lozano
 * @author Francisco Mahedero Biot
 * @author Ignacio Puche Lara
 */
public class MenuSeleccion extends AppCompatActivity {

    private int posAsigJson;
    private int posPruebaJson;

    private String nota;
    private String asignatura;
    private String prueba;
    private String enlace;
    private String error = "";

    private SesionIntranet sesion = null;

    private Toolbar barraOpciones;

    private Spinner asignaturasSeleccion;
    private Spinner pruebasSeleccion;

    private TextView nombreAlumno;
    private TextView palabraNota;
    private TextView campoNota;

    private TextView palabraNotaMaxima;
    private EditText etNotaMaxima;
    private Button botonEstadisticas;

    private TextView noAsignaturas;

    private ProgressBar progressBar;

    private boolean primeraVezSpinnerAsignatuas = true;
    private boolean primeraVezSpinnerPruebas = true;

    /**
     * Metodo llamado al crear la clase.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_menu_seleccion);

        //Obtención de los datos de la anterior antividad.
        String dni = getIntent().getStringExtra("DNI"); // obtiene de la anterior actividad con put extra el dni.
        String clave = getIntent().getStringExtra("PIN");//obtiene de la anterior actividad con put extra la clave.

        //Obtencion de los id de cada View
        this.barraOpciones = (Toolbar) findViewById(R.id.toolbarMenuSeleccion);

        this.nombreAlumno = (TextView) findViewById (R.id.nombre_alumno);

        this.asignaturasSeleccion = (Spinner) findViewById(R.id.spinnerAsignaturas);
        this.pruebasSeleccion = (Spinner) findViewById(R.id.spinnerPruebas);

        this.palabraNota = (TextView) findViewById(R.id.palabraNota);
        this.campoNota = (TextView) findViewById(R.id.campoNota);

        this.palabraNotaMaxima = (TextView) findViewById(R.id.menuSeleccion_palabraNotaMaxima);
        this.etNotaMaxima = (EditText) findViewById(R.id.menuSeleccion_textoNotaMaxima);
        this.botonEstadisticas = (Button) findViewById(R.id.menuSeleccion_estadisticas);

        this.noAsignaturas = (TextView) findViewById(R.id.noAsignaturas);

        this.progressBar = (ProgressBar) findViewById(R.id.progressBar);

        //Hacerlos invisibles
        barraOpciones.setVisibility(View.INVISIBLE);

        nombreAlumno.setVisibility(View.INVISIBLE);
        asignaturasSeleccion.setVisibility(View.INVISIBLE);//Spinner y nombre invisibles
        pruebasSeleccion.setVisibility(View.INVISIBLE);
        palabraNota.setVisibility(View.INVISIBLE);
        campoNota.setVisibility(View.INVISIBLE);

        palabraNotaMaxima.setVisibility(View.INVISIBLE);
        etNotaMaxima.setVisibility(View.INVISIBLE);
        botonEstadisticas.setVisibility(View.INVISIBLE);

        noAsignaturas.setVisibility(View.INVISIBLE);

        //Obtener los datos del alumno
        sesion = new SesionIntranet(dni, clave, this);
        ObtenerDatos obtDatos = new ObtenerDatos();
        obtDatos.execute(sesion);
    }

    /**
     * Metodo llamado al crear el menu. Se especifica el menu deseado
     *
     * @param menu Menu que va a ser establecido
     * @return Satisfactorio
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_seleccion, menu);
        return true;
    }

    /**
     * Metodo llamado al pulsar el boton de volver hacia atras.
     *
     */
    @Override
    public void onBackPressed() {
        volverActividadAnterior(true,false,null);
    }

    /**
     * Metodo pulsado al elegir un elemento especifico del menu
     *
     * @param item Elemento escogido
     * @return Satisfactorio
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                //onBackPressed();
                onBackPressed();
                return true;
            case R.id.menuItem_menuSeleccion_refrescar:

                progressBar.setVisibility(View.VISIBLE);

                ObtenerPadrino obtenerPadrino = new ObtenerPadrino();
                obtenerPadrino.execute(sesion);

                return true;

            case R.id.menuItem_menuSeleccion_ayuda:

                mostrarDialogoAyuda();
                return true;

            case R.id.menuItem_menuSeleccion_cerrarSesion:
                volverActividadAnterior(true,true,null);
                return true;
        }

        return(super.onOptionsItemSelected(item));
    }

    /**
     * Metodo encargado de mostrar el dialogo de la ayuda del menu seleccion.
     *
     */
    private void mostrarDialogoAyuda(){

        android.app.AlertDialog.Builder constructorDialogo = new android.app.AlertDialog.Builder(MenuSeleccion.this);

        LayoutInflater infladorDialogo = getLayoutInflater();

        View vistaDialogo = infladorDialogo.inflate(R.layout.dialog_menu_seleccion_ayuda,null);

        constructorDialogo.setView(vistaDialogo);

        final android.app.AlertDialog dialogo = constructorDialogo.create();

        dialogo.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        Button btnOk = vistaDialogo.findViewById(R.id.butDialogMenuSeleccion_ok);
        btnOk.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialogo.dismiss();
            }
        });

        dialogo.show();

    }

    /**
     * Metodo encargado de destruir la actividad MenuSeleccion, volver a la actividad anterior
     * (MainActivity) devolviendo los datos adecuados a esta para que se actue de la manera adecuada.
     *
     * @param satisfactorio Boolean que indica si el login ha sido correcto.
     * @param cerrarSesion Boolean que indica si se pretende cerrar la sesion la sesion actual.
     * @param error String del error obtenido mediante el metodo getError() de la clase SesionIntranet.
     */
    private void volverActividadAnterior(boolean satisfactorio,boolean cerrarSesion,String error) {

        Intent intentoRetorno = new Intent();
        Bundle b = new Bundle();

        if(satisfactorio){

            if(cerrarSesion){

                setResult(Activity.RESULT_FIRST_USER);

            }else{
                setResult(Activity.RESULT_OK);
            }

        }else{
            b.putString("error", error);

            intentoRetorno.putExtras(b);

            setResult( Activity.RESULT_CANCELED, intentoRetorno );
        }
        finish();
    }

    /**
     * Método encargado de la selección de las asignaturas en el Spinner.
     *
     * @param position posición del spinner que se está seleccionando
     * @param infoPadrino JsonObjecto del padrino con las notas
     * @param contexto
     * @param datosSpinnerAsignaturas Array de Strings en las que se indica las asginaturas que se
     * pueden seleccionar
     */
    private void cambiosSpinnerAsignaturas(int position, JSONObject infoPadrino, Context contexto, String[] datosSpinnerAsignaturas){

        if(primeraVezSpinnerAsignatuas && position != datosSpinnerAsignaturas.length){
            primeraVezSpinnerAsignatuas=false;
        }

        posAsigJson = position;

        palabraNota.setVisibility(View.INVISIBLE);
        campoNota.setVisibility(View.INVISIBLE); //Cada vez que se seleccione una asignatura, la nota anterior desaparece

        //Si se ha seleccionado una asignatura
        if(!primeraVezSpinnerAsignatuas) {
            //Obtener Prueba
            try {

                final JSONObject asignaturaSeleccionada = infoPadrino.getJSONArray("asignaturas").getJSONObject(posAsigJson);

                int numPruebas = asignaturaSeleccionada.getInt("numeroPruebas");
                String[] datosSpinnerPruebas = new String[numPruebas];

                for (int i = 0; i < numPruebas; i++) {
                    JSONObject prueba = asignaturaSeleccionada.getJSONArray("pruebas").getJSONObject(i);

                    datosSpinnerPruebas[i] = prueba.getString("nombre");
                }

                spinnerAdapter adaptadorSpinnerPrueb = new spinnerAdapter(contexto, android.R.layout.simple_spinner_item);
                adaptadorSpinnerPrueb.addAll(datosSpinnerPruebas);
                adaptadorSpinnerPrueb.add(getResources().getString(R.string.menuSeleccion_seleccionePrueb));
                pruebasSeleccion.setAdapter(adaptadorSpinnerPrueb);
                pruebasSeleccion.setSelection(adaptadorSpinnerPrueb.getCount());

                pruebasSeleccion.setVisibility(View.VISIBLE);// hacemos visible el spinner de pruebas, una vez obrenido el array de pruebas.
                pruebasSeleccion.setOnItemSelectedListener( //Creamos la accion a partir de la seleccion de una prueba.
                        new AdapterView.OnItemSelectedListener() {
                            public void onItemSelected(AdapterView<?> parent, android.view.View v, int position, long id) {

                                cambiosSpinnerPruebas(position,asignaturaSeleccionada);

                            }

                            public void onNothingSelected(AdapterView<?> parent) {
                            }
                        }

                );

            } catch (Exception e) {
                e.printStackTrace();

            }
        }//Si no se ha seleccionado ninguna prueba, no se hace nada.

    }

    /**
     * Método encargado de la selección de las pruebas en el Spinner.
     *
     * @param position posición del spinner que se está seleccionando
     * @param asignaturaSeleccionada JsonObjecto de la asigntura seleccionada en el anterior spinner.
     */
    private void cambiosSpinnerPruebas(int position, JSONObject asignaturaSeleccionada ){

        if(primeraVezSpinnerPruebas){
            primeraVezSpinnerPruebas=false;
        }

        posPruebaJson = position;

        //En principio no se puede acceder a las estadisticas
        palabraNotaMaxima.setVisibility(View.INVISIBLE);
        etNotaMaxima.setVisibility(View.INVISIBLE);
        botonEstadisticas.setVisibility(View.INVISIBLE);

        //Obtenemos la nota de la prueba seleccionada:
        if(!primeraVezSpinnerPruebas) {
            try {
                //Obtenemos los datos
                nota = asignaturaSeleccionada.getJSONArray("pruebas").getJSONObject(posPruebaJson).getString("nota");
                asignatura = asignaturaSeleccionada.getString("nombre");
                prueba = asignaturaSeleccionada.getJSONArray("pruebas").getJSONObject(posPruebaJson).getString("nombre");
                enlace = asignaturaSeleccionada.getJSONArray("pruebas").getJSONObject(posPruebaJson).getString("enlace");

                palabraNota.setVisibility(View.VISIBLE);
                campoNota.setVisibility(View.VISIBLE);
                campoNota.setText(nota); //Representacion de la nota obtenida

                try{
                    double notaObtenida = Double.parseDouble(nota);

                    palabraNotaMaxima.setVisibility(View.VISIBLE);
                    etNotaMaxima.setVisibility(View.VISIBLE);
                    botonEstadisticas.setVisibility(View.VISIBLE);

                    etNotaMaxima.setText("10");

                    botonEstadisticas.setOnClickListener(new View.OnClickListener(){
                        @Override public void onClick(View v){

                            botonEstadisticas.setVisibility(View.INVISIBLE);
                            progressBar.setVisibility(View.VISIBLE);

                            ObtenerNotas obtNotas = new ObtenerNotas();
                            obtNotas.execute(sesion);

                        }
                    });

                }catch(NumberFormatException ex){
                    Log.w("menuSeleccion-notaObtenida","La nota obtenida no es un formato valido");
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    /**
     * Clase asíncrona encargada de obtener los datos necesarios
     */
    class ObtenerDatos extends AsyncTask<SesionIntranet, Void, Boolean> {

        /**
         * Método encargado de la conexión con el servidor de la UPV paralelamente
         * al hilo principal.
         * @param sesion objeto con los métodos para la conexión.
         */
        @Override
        protected Boolean doInBackground(SesionIntranet... sesion) {

            boolean Conexion_satifactoria = sesion[0].establecerConexion();

            if (Conexion_satifactoria) {
                Conexion_satifactoria = sesion[0].obtenerPadrino();
            }else{
                volverActividadAnterior(false,false,sesion[0].getError());
            }

            return Conexion_satifactoria;

        }

        /**
         * Método encargado de obtener la informacion del padrino.
         * @param Conexion boolean que indica si la conexión ha sido satisfactoria o no.
         */
        @Override
        protected void onPostExecute(Boolean Conexion) {

            String Json_padrino = sesion.getInformacionPadrino();
            Context contexto = getApplicationContext();

            if (Conexion){

                conexionSatisfactoria(Json_padrino,contexto);

            }else{
                progressBar.setVisibility(View.VISIBLE);
                error = sesion.getError();
            }
        }
    }

    /**
     * Metodo llamado cuando la conexion ha sido satisfactoria. Es el encargado de establecer los
     * distintos elementos del layout
     *
     * @param Json_padrino String con el Json del padrino
     * @param contexto Contexto de la aplicacion
     */
    private void conexionSatisfactoria(String Json_padrino, final Context contexto){

        //Barra superior
        barraOpciones.setVisibility(View.VISIBLE);
        setSupportActionBar(barraOpciones);

        //Mostrar boton atras
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar.setVisibility(View.INVISIBLE); // ocultamos progressbar
        noAsignaturas.setVisibility(View.INVISIBLE);

        nombreAlumno.setVisibility(View.VISIBLE);

        asignaturasSeleccion.setVisibility(View.VISIBLE);// hacemos visible el spinner de asignaturas

        nombreAlumno.setText(getResources().getString(R.string.menuSeleccion_nombre) + " "+ sesion.getNombreUsuario() );

        try {
            //Obtención de las asignaturas (generalizado para numero de asignaturas)
            final JSONObject infoPadrino = new JSONObject(Json_padrino);

            int numAsignaturas = infoPadrino.getInt("numeroAsignaturas");

            if(numAsignaturas != 0){
                final String[] datosSpinnerAsignaturas = new String[numAsignaturas]; //datosSpinnerAsignaturas + 1

                for(int i=0; i<=numAsignaturas-1;i++) {
                    JSONObject asignatura = infoPadrino.getJSONArray("asignaturas").getJSONObject(i);
                    datosSpinnerAsignaturas[i] = asignatura.getString("nombre");
                }

                //spinnerAdapter adaptadorSpinnerAsig = new spinnerAdapter(contexto, android.R.layout.simple_list_item_1);
                spinnerAdapter adaptadorSpinnerAsig = new spinnerAdapter(contexto, android.R.layout.simple_spinner_item);
                adaptadorSpinnerAsig.addAll(datosSpinnerAsignaturas);
                adaptadorSpinnerAsig.add(getResources().getString(R.string.menuSeleccion_seleccioneAsig));
                asignaturasSeleccion.setAdapter(adaptadorSpinnerAsig);
                asignaturasSeleccion.setSelection(adaptadorSpinnerAsig.getCount());

                asignaturasSeleccion.setOnItemSelectedListener(
                        new AdapterView.OnItemSelectedListener() {
                            public void onItemSelected(AdapterView<?> parent,android.view.View v, int position, long id) {

                                //Se llama a dicho metodo para que realize las tareas
                                cambiosSpinnerAsignaturas(position,infoPadrino,contexto,datosSpinnerAsignaturas);

                            }

                            public void onNothingSelected(AdapterView<?> parent) {
                            }

                        }
                );

            }else{

                noAsignaturas.setVisibility(View.VISIBLE);
                asignaturasSeleccion.setVisibility(View.INVISIBLE);

                error = sesion.getError();

            }

        }catch(JSONException e) {

            Log.w("menuSeleccion-ObtenerDatos-OnPostExecute","Error al analizar el JSON");

            noAsignaturas.setVisibility(View.VISIBLE);
            asignaturasSeleccion.setVisibility(View.INVISIBLE);
            pruebasSeleccion.setVisibility(View.INVISIBLE);

            error = sesion.getError();

        }

    }

    /**
     * Clase encargado de obtener el padrino cuando se desean actualizar las notas del menu
     * seleccion.
     */
    class ObtenerPadrino extends AsyncTask<SesionIntranet, Void, Boolean> {

        /**
         * Método encargado de la obtención del Padrino.
         * @param sesion objeto con el método encargado de la obtencion del padrino.
         */
        @Override
        protected Boolean doInBackground(SesionIntranet... sesion) {

            boolean Conexion_satifactoria = sesion[0].obtenerPadrino();

            return Conexion_satifactoria;

        }

        /**
         * Método encargado de la relización de la selección de asignturas y pruebas y display de notas al
         * terminar el doInBackground
         * @param Conexion boolean que indica si la conexion ha sido satisfactoria o no.
         */
        @Override
        protected void onPostExecute(Boolean Conexion) {

            error = null;

            String Json_padrino = sesion.getInformacionPadrino();
            final Context contexto = getApplicationContext();

            if (Conexion){

                noAsignaturas.setVisibility(View.INVISIBLE);

                asignaturasSeleccion.setVisibility(View.INVISIBLE);
                pruebasSeleccion.setVisibility(View.INVISIBLE);

                palabraNotaMaxima.setVisibility(View.INVISIBLE);
                etNotaMaxima.setVisibility(View.INVISIBLE);
                botonEstadisticas.setVisibility(View.INVISIBLE);

                asignaturasSeleccion.setVisibility(View.VISIBLE);// hacemos visible el spinner de asignaturas

                try {
                    //Obtención de las asignaturas (generalizado para numero de asignaturas)
                    final JSONObject infoPadrino = new JSONObject(Json_padrino);

                    int numAsignaturas = infoPadrino.getInt("numeroAsignaturas");

                    if(numAsignaturas != 0){
                        final String[] datosSpinnerAsignaturas = new String[numAsignaturas]; //datosSpinnerAsignaturas + 1

                        for(int i=0; i<=numAsignaturas-1;i++) {
                            JSONObject asignatura = infoPadrino.getJSONArray("asignaturas").getJSONObject(i);
                            datosSpinnerAsignaturas[i] = asignatura.getString("nombre");
                        }

                        //spinnerAdapter adaptadorSpinnerAsig = new spinnerAdapter(contexto, android.R.layout.simple_list_item_1);
                        spinnerAdapter adaptadorSpinnerAsig = new spinnerAdapter(contexto, android.R.layout.simple_spinner_item);
                        adaptadorSpinnerAsig.addAll(datosSpinnerAsignaturas);
                        adaptadorSpinnerAsig.add(getResources().getString(R.string.menuSeleccion_seleccioneAsig));
                        asignaturasSeleccion.setAdapter(adaptadorSpinnerAsig);
                        asignaturasSeleccion.setSelection(adaptadorSpinnerAsig.getCount());

                        asignaturasSeleccion.setOnItemSelectedListener(
                                new AdapterView.OnItemSelectedListener() {
                                    public void onItemSelected(AdapterView<?> parent,android.view.View v, int position, long id) {

                                        //Se llama a dicho metodo para que realize las tareas
                                        cambiosSpinnerAsignaturas(position,infoPadrino,contexto,datosSpinnerAsignaturas);

                                    }

                                    public void onNothingSelected(AdapterView<?> parent) {
                                    }

                                }
                        );

                    }else{

                        noAsignaturas.setVisibility(View.VISIBLE);
                        asignaturasSeleccion.setVisibility(View.INVISIBLE);

                        error = sesion.getError();

                    }

                }catch(JSONException e) {

                    Log.w("menuSeleccion-ObtenerDatos-OnPostExecute","Error al analizar el JSON");

                    noAsignaturas.setVisibility(View.VISIBLE);
                    asignaturasSeleccion.setVisibility(View.INVISIBLE);
                    pruebasSeleccion.setVisibility(View.INVISIBLE);

                    error = sesion.getError();

                }

            }else{
                error = sesion.getError();
            }

            progressBar.setVisibility(View.INVISIBLE);

            if(error != null){

                AlertDialog.Builder constructor = new AlertDialog.Builder(MenuSeleccion.this);
                constructor.setTitle(R.string.error);
                constructor.setMessage(error);
                constructor.setPositiveButton(R.string.aceptar, null); //Null indica que al pulsar aceptar no se realizará ningua opcion.

                Dialog cuadroDialogo = constructor.create();
                cuadroDialogo.show();

            }

        }
    }

    /**
     * Clase encargada de obtener las notas cuando se desea realizar las estadisticas.
     *
     */
    class ObtenerNotas extends AsyncTask<SesionIntranet, Void, String> {

        @Override
        protected String doInBackground(SesionIntranet... sesion) {

            return sesion[0].obtenerNotas(enlace);

        }

        @Override
        protected void onPostExecute(String jsonNotas) {

            botonEstadisticas.setVisibility(View.VISIBLE);
            progressBar.setVisibility(View.INVISIBLE);

            if(jsonNotas != null){

                //Lanzar la nueva actividad
                Intent intentoActividadEstadisticas = new Intent( MenuSeleccion.this, EstadisticasNotas.class );

                Bundle bundleActividadEstadisticas = new Bundle();

                bundleActividadEstadisticas.putString("jsonNotas",jsonNotas);
                bundleActividadEstadisticas.putDouble("notaMaximaExamen",Double.parseDouble( etNotaMaxima.getText().toString() ) );
                bundleActividadEstadisticas.putString("nombreExaminado",sesion.getNombreUsuario());
                bundleActividadEstadisticas.putString("asignatura",asignatura);
                bundleActividadEstadisticas.putString("prueba",prueba );
                intentoActividadEstadisticas.putExtras(bundleActividadEstadisticas);

                startActivity(intentoActividadEstadisticas);

            }else{

                error = sesion.getError();

                AlertDialog.Builder constructor = new AlertDialog.Builder(MenuSeleccion.this);
                constructor.setTitle(R.string.error);
                constructor.setMessage(error);
                constructor.setPositiveButton(R.string.aceptar, null); //Null indica que al pulsar aceptar no se realizará ningua opcion.

                Dialog cuadroDialogo = constructor.create();
                cuadroDialogo.show();

            }

        }
    }

    /**
     * Adaptador de los spinners. Este adaptador devuelve un valor de la cuenta menos para que se
     * puede establecer un "hint" en el mismo.
     *
     */
    class spinnerAdapter extends ArrayAdapter<String> {

        public spinnerAdapter(Context context, int textViewResourceId) {
            super(context, textViewResourceId);
        }

        @Override
        public int getCount() {
            int count = super.getCount();

            //Se devuelve un numero menos si la cuenta es distinta de 0 para eliminar el hint
            return count>0 ? count-1 : count ;
        }

    }

}
