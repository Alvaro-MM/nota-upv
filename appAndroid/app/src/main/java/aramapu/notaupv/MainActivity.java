package aramapu.notaupv;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;

import java.util.Arrays;
import java.util.Base64;
import java.util.Locale;

/**
 * Clase encargada de generar la primera actividad (Pantalla de Login)
 *
 * @author Mario Aragones Lozano
 * @author Ignacio Puche Lara
 */
public class MainActivity extends AppCompatActivity {


    private String DNI; //Valores que serán pasados a la segunda actividad con esos mismos nombres.
    private String PIN;

    private final int peticion = 1;
    private String error;

    private EditText dniEditText;
    private EditText pinEditText;
    private ImageButton menuOverflow;

    private Configuration config = new Configuration();

    private SharedPreferences prefsUsuario;
    private Encriptacion encr;

    private boolean recordarDatos;

    /**
     * Metodo encargado de crear la actividad. No se ha utilizado el Bundle de entrada.
     *
     * En este metodo se definen las acciones de todos los objetos View de la actividad.
     *
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //Preferencias usuario
        recordarDatos = false;

        prefsUsuario = getSharedPreferences("NotasUPV", Context.MODE_PRIVATE);
        encr = new Encriptacion();

        //Se establece el idioma del usuario
        String Idioma = prefsUsuario.getString("Idioma",MainActivity.this.getResources().getConfiguration().getLocales().get(0).toString());
        config.setLocale(new Locale(Idioma));
        getResources().updateConfiguration(config, null);

        comprobarPreferenciasUsuario();

        setContentView(R.layout.activity_main);

        this.dniEditText = (EditText) findViewById(R.id.activityMain_inputDNI);
        this.pinEditText = (EditText) findViewById(R.id.activityMain_inputPIN);

        Button BotonAcceder = (Button) findViewById(R.id.activityMain_botonAcceder);
        BotonAcceder.setOnClickListener(new View.OnClickListener(){
            /**
             * El boton acceder, lanza la siguiente actividad (MenuSeleccion) y registra el
             * DNI y el PIN de forma cifrada, en el caso de haber seleccionado el checkbox "Recuerdame".
             * @param v
             */
            @Override public void onClick(View v){

                DNI = ((EditText) findViewById(R.id.activityMain_inputDNI)).getText().toString();
                PIN = ((EditText) findViewById(R.id.activityMain_inputPIN)).getText().toString();

                if( ( (CheckBox) findViewById(R.id.activityMain_checkRecordarme) ).isChecked() ){

                    recordarDatos = true;

                    SharedPreferences.Editor editorPreferencias = prefsUsuario.edit();

                    encr.generateKey();

                    editorPreferencias.putString("DNI",Arrays.toString(encr.encryptText(DNI)));
                    editorPreferencias.putString("PIN",Arrays.toString(encr.encryptText(PIN)));

                    editorPreferencias.commit();

                    Log.d("MainActivity-checkBoton","Preferencias guardadas");

                }

                lanzarActividadMenuSeleccion();

            }
        });

        menuOverflow = (ImageButton) findViewById(R.id.activityMain_botonMenuDesplegable);
        menuOverflow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final PopupMenu menu = new PopupMenu(MainActivity.this, menuOverflow);
                menu.getMenuInflater().inflate(R.menu.activity_main_overflow_boton,menu.getMenu());

                menu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    @Override
                    public boolean onMenuItemClick(MenuItem item) {

                        switch (item.getItemId()){
                            case R.id.mainActivity_menuIdioma:

                                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);

                                final String[] idiomas = getResources().getStringArray(R.array.idiomas_disponibles);

                                builder.setTitle(getResources().getString(R.string.escoge_idioma))
                                        .setItems(idiomas, new DialogInterface.OnClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int pos) {
                                                //Opcion seleccionada
                                                Log.d("MainActivity-menuSeleccionIdioma", "Se ha seleccionado " + idiomas[pos]);

                                                //Se almacena en las preferencias de usuario el idioma
                                                SharedPreferences.Editor editorPreferencias = prefsUsuario.edit();

                                                switch(pos){
                                                    case 0:
                                                        editorPreferencias.putString("Idioma","en");
                                                        break;
                                                    case 1:
                                                        editorPreferencias.putString("Idioma","es");
                                                        break;
                                                    case 2:
                                                        editorPreferencias.putString("Idioma","ca");
                                                        break;
                                                }

                                                editorPreferencias.commit();

                                                //Se recarga la pagina para aplicar los cambios
                                                Intent refresh = new Intent(MainActivity.this, MainActivity.class);
                                                startActivity(refresh);
                                                finish();

                                            }
                                        });

                                builder.create().show();

                                return true;
                            case R.id.mainActivity_menuSobreNosotros:

                                Intent intentoSobreNosotros = new Intent(MainActivity.this,SobreNosotros.class);
                                startActivity(intentoSobreNosotros);

                                return true;
                            case R.id.mainActivity_menuGithub:

                                Uri uri = Uri.parse("https://github.com/maarlo9/NotaUPV");
                                Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                                startActivity(intent);

                                return true;

                        }

                        return true;
                    }
                });
                menu.show();

            }
        });

    }

    /**
     * Método que comprobará si el usuario previamente ha seleccionado la opcion "recuerdame" y
     * por tanto su DNI y PIN estan previamente almacenados en el dispositivo.
     *
     * Si hay datos almacenados se cargara directamente la actividad de menu seleccion
     * En caso de que no hayan valores almacenados, el metodo no hace nada.
     *
     */
    private void comprobarPreferenciasUsuario(){

        DNI = prefsUsuario.getString("DNI",null);
        PIN = prefsUsuario.getString("PIN",null);

        if( (DNI  != null)&&(PIN !=null) ){

            recordarDatos = true;

            String[] stringSeparado;
            byte[] arrayBytes;

            //DNI
            stringSeparado = DNI.substring(1, DNI.length()-1).split(", ");
            arrayBytes = new byte[stringSeparado.length];

            for(int i=0; i < arrayBytes.length; i++){
                arrayBytes[i] = Byte.parseByte(stringSeparado[i]);
            }

            DNI = encr.decryptData(arrayBytes);

            Log.d("MainActivity-comprobarPreferenciasUsuario",DNI);

            //PIN
            stringSeparado = PIN.substring(1, PIN.length()-1).split(", ");
            arrayBytes = new byte[stringSeparado.length];

            for(int i=0; i < arrayBytes.length; i++){
                arrayBytes[i] = Byte.parseByte(stringSeparado[i]);
            }

            PIN = encr.decryptData(arrayBytes);

            lanzarActividadMenuSeleccion();

        }

    }

    /**
     * Metodo encargado de lanzar la actividad  "menu seleccion", pasandole a dicha actividad
     * el DNI y el PIN almacenados como variables de clase y esperando el resultado de si el login
     * ha sido correcto o incorrecto.
     *
     */
    private void lanzarActividadMenuSeleccion(){

        Intent intento = new Intent(MainActivity.this, MenuSeleccion.class); //Segunda Clase

        Bundle b = new Bundle();

        b.putString("DNI",DNI);  //Nombres con los que serán llamados desde Main2Activity
        b.putString("PIN",PIN);

        intento.putExtras(b);

        startActivityForResult(intento,peticion);

    }

    /**
     * Metodo llamado cuando se finaliza la actividad llamada.
     * Se contemplan distintos casos segun como ha finalizado la actividad que ha sido llamada.
     *
     * @param codigoPeticion Codigo peticion de la actividad llamada
     * @param codigoResultado Codigo resultado de la actividad llamada
     * @param intentoVuelta Intento de vuelta
     */
    @Override
    protected void onActivityResult(int codigoPeticion, int codigoResultado, Intent intentoVuelta ) {

        //Ok -> -1, Cancelled -> 0
        Log.d("MainActivity-onActivityResult","Codigo resultado "+codigoResultado);

        if(codigoResultado== Activity.RESULT_CANCELED) {
            //Si ha habido error en la segunda actividad:
            Bundle b = new Bundle();
            b = intentoVuelta.getExtras();
            this.error = b.getString("error");

            if(error!=null) {

                AlertDialog.Builder constructor = new AlertDialog.Builder(this);
                constructor.setTitle(R.string.error);
                constructor.setMessage(error);
                constructor.setPositiveButton(R.string.aceptar, null); //Null indica que al pulsar aceptar no se realizará ningua opcion.

                Dialog cuadroDialogo = constructor.create();
                cuadroDialogo.show();
            }

            recordarDatos = false;

            SharedPreferences.Editor editorPreferencias = prefsUsuario.edit();

            editorPreferencias.remove("DNI");
            editorPreferencias.remove("PIN");

            editorPreferencias.commit();

        }else if(codigoResultado == Activity.RESULT_OK){

            //Si la actividad se ha devuelto correctamente.
            if(recordarDatos){
                finish();
            }

        }else{

            //Si se ha cerrado sesion
            Log.d("MainActivity-onActivityResult","Sesion de usuario cerrada");
            recordarDatos = false;

            SharedPreferences.Editor editorPreferencias = prefsUsuario.edit();

            editorPreferencias.remove("DNI");
            editorPreferencias.remove("PIN");

            editorPreferencias.commit();

            ( (CheckBox) findViewById(R.id.activityMain_checkRecordarme) ).setChecked(false);

        }

        dniEditText.getText().clear();
        pinEditText.getText().clear();

    }

}

