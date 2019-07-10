package aramapu.notaupv;

import android.content.Context;
import android.util.Log;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.CookieHandler;
import java.net.CookieManager;
import java.net.HttpCookie;
import java.net.URL;
import java.util.List;

import javax.net.ssl.HttpsURLConnection;

/**
 * Clase encargada de establecer la conexion con el servidor de la UPV
 *
 * @author Mario Aragonés Lozano
 */
public class SesionIntranet {

    private final String dni;
    private final String clave;

    private String cursoMostrar; //cursoMostrar = "1" -> Todos los cursos, cursoMostrar = "2" -> Curso actual

    private Context context;

    private CookieManager cookieManager;

    private final int tiempoEsperaMaximo = 15000;

    private final String userAgentPeticion = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/74.0.3729.131 Safari/537.36";
    private final String acceptPeticion = "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8";
    private final String acceptLanguagePeticion = "es,ca;q=0.9";

    private final String IDIOMA = "c"; //c -> Caste , v -> Valenc, i -> Ingles

    private final String charsetServidorUpv = "iso-8859-15";

    private boolean conexionEstablecida;
    private String error;
    private String nombreUsuario;

    private String informacionPadrino;

    /**
     * Constructor de la clase.
     * Muestra el curso actual
     *
     * @param dni DNI de acceso a PoliformaT del usuario
     * @param clave Clave de acceso a PoliformaT del usuario
     * @param context Contexto de la aplicacion para obtener acceso a res.
     */
    SesionIntranet(String dni, String clave, Context context){

        this.dni = dni;
        this.clave = clave;

        this.cursoMostrar = "2";

        this.context = context;

        cookieManager = new CookieManager();
        CookieHandler.setDefault(cookieManager);

    }

    /**
     * Constructor de la clase
     * Permite la opción de mostrar todos los cursos.
     *
     * @param dni DNI de acceso a PoliformaT del usuario
     * @param clave Clave de acceso a PoliformaT del usuario
     * @param mostrarTodosLosCursos Si es "True" se obtienen todos los cursos del usuario.
     * @param context Contexto de la aplicacion para obtener acceso a res.
     */
    SesionIntranet(String dni, String clave, boolean mostrarTodosLosCursos, Context context){

        this(dni,clave,context);

        if(mostrarTodosLosCursos){
            this.cursoMostrar = "1";
        }

    }

    /**
     * Metodo encargado de establecer la conexion con el servidor
     *
     * @return True si ha sido satisfactoria. False en caso contrario.
     */
    boolean establecerConexion(){

        conexionEstablecida = false;

        //Comprobar DNI y Claves valido
        if(dni.length() == 0  || clave.length() == 0 || dni == null || clave == null){

            error = context.getResources().getString(R.string.sesionIntranet_datosNoValidos);

            Log.w("SesionIntranet-establecerConexion","Contraseña o clave vacia.");

            return conexionEstablecida;
        }

        String urlEstablecerConexion = "https://intranet.upv.es/exp/aute_intranet";

        try{

            URL urlPost = new URL(urlEstablecerConexion);
            HttpsURLConnection conexion = (HttpsURLConnection) urlPost.openConnection();

            //Establecer tiempo máximo de espera del servidor
            conexion.setReadTimeout(tiempoEsperaMaximo);
            conexion.setConnectTimeout(tiempoEsperaMaximo);

            conexion.setRequestMethod("POST");

            //Anadir las cabeceras
            conexion.setRequestProperty("User-Agent",userAgentPeticion );
            conexion.setRequestProperty("Accept", acceptPeticion);
            conexion.setRequestProperty("Accept-Language", acceptLanguagePeticion);

            //Establecer los parametros del usuario al post
            String postParameters = "id="+IDIOMA+"&dni="+dni+"&clau="+clave;

            conexion.setDoOutput(true);
            DataOutputStream outToServer = new DataOutputStream(conexion.getOutputStream());
            outToServer.writeBytes(postParameters);
            outToServer.flush();
            outToServer.close();

            int responseCode = conexion.getResponseCode();

            if(responseCode == HttpsURLConnection.HTTP_OK){

                Log.d("SesionIntranet-establecerConexion","El servidor ha devuelto un 200. La conexion ha sido satisfactoria.");

                //Leer la respuesta del servidor. Se pone el charset para que obtenga la n, acentos,...
                BufferedReader lectorRespuesta = new BufferedReader(new InputStreamReader(conexion.getInputStream(),charsetServidorUpv));

                try {

                    String lineaLeida;

                    //Mientras la linea leida no contenga la etiqueta title, continua leyendo
                    while ((lineaLeida = lectorRespuesta.readLine()) != null) {
                        if (lineaLeida.contains("<title>")) {
                            break;
                        }
                    }

                    lineaLeida = lineaLeida.substring(lineaLeida.indexOf(">") + 1, lineaLeida.lastIndexOf("<"));

                    Log.i("SesionIntranet-establecerConexion", "Title pagina web: " + lineaLeida);

                    /* Se comprueba si ha sido satisfactorio el inicio de sesion.
                     *
                     * Si no ha sido satifastoctoria la conexion. Hay 4 errores a valorar
                     * 1.- DNI incorrecto
                     * 2.- Clave incorrecta. Devolver numero de intentos
                     * 3.- Se han realizado 5 intentos. Cuenta bloqueada
                     * 4.- Error desconocido.
                     *
                     */

                    if (lineaLeida.equals("Intranet Alumno UPV")) {

                        //Ha sido satisfactoria la conexion.
                        conexionEstablecida = true;

                        //Mientras la linea leida no contenga la etiqueta panel_155, continua leyendo. Aqui se encuentra el nombre
                        while ((lineaLeida = lectorRespuesta.readLine()) != null) {
                            if (lineaLeida.contains("panel_155")) {
                                break;
                            }
                        }

                        lineaLeida = lineaLeida.substring(lineaLeida.indexOf(">") + 1, lineaLeida.lastIndexOf("<"));

                        nombreUsuario = lineaLeida;

                        Log.i("SesionIntranet-establecerConexion", "Nombre del usuario: " + nombreUsuario);

                    } else if(lineaLeida.length() > 8 && lineaLeida.substring(0,9).equals("Informaci")) {

                        //1. DNI incorrecto. La persona no esta asociada con la UPV

                        //Mientras la linea leida no contenga la etiqueta upv_textoerror.
                        while ((lineaLeida = lectorRespuesta.readLine()) != null) {
                            if (lineaLeida.contains("upv_textoerror")) {
                                break;
                            }
                        }

                        lineaLeida = lineaLeida.substring(lineaLeida.indexOf(">") + 1);

                        if(lineaLeida.equals("Persona no asociada como alumno en la UPV")){
                            error = context.getResources().getString(R.string.sesionIntranet_personaNoAsociada);

                            Log.w("SesionIntranet-establecerConexion","DNI incorrecto. Persona no asociada. DNI: " + dni);
                        } else {
                            //Error desconocido
                            error = context.getResources().getString(R.string.sesionIntranet_errorDesconocido);

                            Log.w("SesionIntranet-establecerConexion","DNI incorrecto. Error desconocido");
                        }

                    } else if(lineaLeida.equals("Error")){

                        //2. Clave incorrecta o 3. Cuenta bloqueada

                        //Mientras la linea leida no contenga la etiqueta upv_textoerror.
                        while ((lineaLeida = lectorRespuesta.readLine()) != null) {
                            if (lineaLeida.contains("upv_textoerror")) {
                                break;
                            }
                        }

                        lineaLeida = lineaLeida.substring(lineaLeida.indexOf(">")+1);

                        if(lineaLeida.contains("Aute_Alu")) {

                            lineaLeida = lineaLeida.substring(lineaLeida.indexOf(":"));

                            if(lineaLeida.contains("El PIN ha de ser un valor num")){

                                error = context.getResources().getString(R.string.sesionIntranet_pinNum);

                                Log.w("SesionIntranet-establecerConexion","El usuario ha escrito un PIN no numérico");

                            }else{
                                error = context.getResources().getString(R.string.sesionIntranet_errorDesconocido);

                                Log.w("SesionIntranet-establecerConexion","El servidor ha devuelto Aute_Alu pero no es problema de que el pin no es un valor numerico. Aute_Alu: "+lineaLeida);
                            }

                        }else if(lineaLeida.length() > 40 && lineaLeida.substring(0,41).equals("Clave de acceso no validada. Registrando ")){

                            //2. Clave incorrecta
                            error = context.getResources().getString(R.string.sesionIntranet_claveIncorrecta_1)+lineaLeida.charAt(41)+ context.getResources().getString(R.string.sesionIntranet_claveIncorrecta_2);

                            Log.w("SesionIntranet-establecerConexion","Clave incorrecta. "+ lineaLeida.charAt(41) + " intentos.");

                        }else if( lineaLeida.contains("cinco intentos no correctos") ){

                            //3. Cuenta bloqueada
                            error = context.getResources().getString(R.string.sesionIntranet_cuentaBloqueada);

                            Log.w("SesionIntranet-establecerConexion","Cuenta bloqueada.");

                        }else{
                            //Error desconocido
                            error = context.getResources().getString(R.string.sesionIntranet_errorDesconocido);

                            Log.w("SesionIntranet-establecerConexion","Clave incorrecta / Cuenta bloqueada. Error desconocido. DNI " + dni +".");
                        }

                    } else {
                        //4. Error desconocido.
                        error = context.getResources().getString(R.string.sesionIntranet_errorDesconocido);

                        Log.w("SesionIntranet-establecerConexion","Error general desconocido.");
                    }



                }catch (IOException e){
                    error = context.getResources().getString(R.string.sesionIntranet_errorDesconocido);

                    Log.w("SesionIntranet-establecerConexion","Error leyendo los datos del servidor.");
                    Log.w("SesionIntranet-establecerConexion",e);
                }catch(NullPointerException e) {
                    error = context.getResources().getString(R.string.sesionIntranet_errorDesconocido);

                    Log.w("SesionIntranet-establecerConexion", "Null pointer expception.");
                    Log.w("SesionIntranet-establecerConexion", e);
                }catch(StringIndexOutOfBoundsException e){
                    error = context.getResources().getString(R.string.sesionIntranet_errorDesconocido);

                    Log.w("SesionIntranet-establecerConexion", "String index out of bound exception.");
                    Log.w("SesionIntranet-establecerConexion", e);
                }finally {
                    lectorRespuesta.close();
                }

            }else{
                Log.w("SesionIntranet-establecerConexion","El servidor ha devuelto un valor distinto a 200. Hay un problema en el mismo.");

                error = context.getResources().getString(R.string.sesionIntranet_errorDesconocido);
            }

        }catch (IOException e){
            error = context.getResources().getString(R.string.sesionIntranet_tiempoEsperaRespuesta);

            Log.w("SesionIntranet-establecerConexion","No se ha podido establecer la conexion con el servidor. Error IO.");
            Log.w("SesionIntranet-establecerConexion",e);
        }catch(NullPointerException e){
            error = context.getResources().getString(R.string.sesionIntranet_errorDesconocido);

            Log.w("SesionIntranet-establecerConexion","Null pointer exception.");
            Log.w("SesionIntranet-establecerConexion",e);
        }

        return conexionEstablecida;

    }

    /**
     * Metodo encargado de devolver el String privado error
     *
     * @return String con la informacion del error
     */
    String getError(){
        return error;
    }

    /**
     * Metodo encargado de devolver el String privado Nombre De Usuario
     *
     * @return String con el Nombre de Usuario
     */
    String getNombreUsuario(){
        return nombreUsuario;
    }

    /**
     * Metodo encargado de solicitar y devolver un JSON con las asignaturas, examenes, calificaciones y enlaces
     *
     * @return "True" si se han obtenido los datos correctamente. "False" en caso contrario.
     */
    boolean obtenerPadrino(){

        boolean jsonCreadoCorrectamente = false;

        if(conexionEstablecida){

            //URL a la que realizar el POST
            String urlObtenerAsignaturas = "https://intranet.upv.es/pls/soalu/sic_asi.notes_temaalu_asi";

            try{

                URL urlPost = new URL(urlObtenerAsignaturas);
                HttpsURLConnection conexion = (HttpsURLConnection) urlPost.openConnection();

                //Establecer tiempo máximo de espera del servidor
                conexion.setReadTimeout(tiempoEsperaMaximo);
                conexion.setConnectTimeout(tiempoEsperaMaximo);

                conexion.setRequestMethod("POST");

                //Anadir las cabeceras
                conexion.setRequestProperty("User-Agent",userAgentPeticion );
                conexion.setRequestProperty("Accept", acceptPeticion);
                conexion.setRequestProperty("Accept-Language", acceptLanguagePeticion);

                //Anadir las cookies
                List<HttpCookie> cookies = cookieManager.getCookieStore().getCookies();
                String cookiesToSend = "";
                for (HttpCookie cookie : cookies) {
                    cookiesToSend += cookie+";";
                }
                conexion.setRequestProperty("Cookie", cookiesToSend);

                //Establecer los parametros para obtener el padrino del ano actual
                //p_curso = 1 -> Todos los cursos, p_curso = 2 -> Curso actual
                String postParameters = "P_VISTA=intranet&P_IDIOMA=c&P_PRIMERAVEZ=N&P_PADRINO2=S&p_curso="+cursoMostrar;

                conexion.setDoOutput(true);
                DataOutputStream outToServer = new DataOutputStream(conexion.getOutputStream());
                outToServer.writeBytes(postParameters);
                outToServer.flush();
                outToServer.close();

                int responseCode = conexion.getResponseCode();

                if(responseCode == HttpsURLConnection.HTTP_OK){

                    Log.d("SesionIntranet-obtenerPadrino","El servidor ha devuelto un 200. La conexion ha sido satisfactoria.");

                    //Leer la respuesta del servidor. Se pone el charset para que obtenga la n, acentos,....
                    BufferedReader lectorRespuesta = new BufferedReader(new InputStreamReader(conexion.getInputStream(),charsetServidorUpv));

                    try {

                        String respuestaServidor = "";
                        String lineaLeida;

                        //Mientras no se ha llegado al final
                        while ((lineaLeida = lectorRespuesta.readLine()) != null) {
                            respuestaServidor += lineaLeida;
                        }

                        //Analizar respuesta.
                        Document paginaHtmlRespuesta = Jsoup.parse(respuestaServidor);

                        //Se selecciona la tabla que tiene la clase upv_listacolumnas
                        Element table = paginaHtmlRespuesta.select("table.upv_listacolumnas").get(0);

                        /* Variables para el Json */

                        //Variables auxiliares
                        int numeroAsignaturas = 0;
                        int numeroPruebas = 0;

                        //Se empieza a definir el JSON
                        informacionPadrino = "{";
                        informacionPadrino += "\"asignaturas\":";
                        informacionPadrino += "[";

                        /* Fin de Variables para el Json */

                        //Se obtienen las filas de la tabla
                        Elements rows = table.select("tr");

                        //Se recorren las filas. La primera se obvia, es el titulo
                        for (int i = 1; i < rows.size(); i++) {

                            //Se escoge la fila i
                            Element row = rows.get(i);

                            //Se obtienen las columnas de la tabla
                            Elements cols = row.select("td");

                            //Se verifica si es titulo o prueba.
                            if(cols.size() == 1){
                                //Es un titulo
                                numeroAsignaturas ++; //Se incrementa en uno las asignaturas.


                                if(numeroAsignaturas != 1){

                                    //No es la primera asignatura
                                    informacionPadrino += "],";
                                    informacionPadrino += "\"numeroPruebas\":";
                                    informacionPadrino += numeroPruebas;
                                    informacionPadrino += "},";

                                    numeroPruebas = 0;

                                }

                                informacionPadrino += "{";
                                informacionPadrino += "\"nombre\":";
                                informacionPadrino += "\""+cols.get(0).text()+"\",";

                                informacionPadrino += "\"pruebas\":";
                                informacionPadrino += "[";

                            }else{
                                //Son pruebas
                                numeroPruebas ++; //Se incrementa en uno el numero de pruebas

                                if(numeroPruebas!=1){
                                    //No es la primer prueba
                                    informacionPadrino += ",";
                                }

                                informacionPadrino += "{";

                                informacionPadrino += "\"curso\":";
                                informacionPadrino += cols.get(0).text();

                                informacionPadrino += ",";

                                informacionPadrino += "\"fecha\":";
                                informacionPadrino += "\""+cols.get(1).text()+"\"";

                                informacionPadrino += ",";

                                informacionPadrino += "\"nombre\":";
                                informacionPadrino += "\""+cols.get(2).text()+"\"";

                                informacionPadrino += ",";

                                //Revisar la creacion de la nota en el JSON.
                                informacionPadrino += "\"nota\":";
                                informacionPadrino += "\""+cols.get(3).text().replace(",",".")+"\"";
                                /*
                                if(cols.get(3).text().replace(",",".").equals("")){
                                    informacionPadrino += "-1";
                                }else{
                                    informacionPadrino += cols.get(3).text().replace(",",".");
                                }
                                */

                                informacionPadrino += ",";

                                informacionPadrino += "\"enlace\":";
                                informacionPadrino += "\"https://intranet.upv.es/pls/soalu/"+cols.select("a[href]").get(0).attr("href")+"\"";

                                informacionPadrino += "}";

                            }

                        }
                        //Se añade a la ultima asignatura el numero de pruebas
                        informacionPadrino += "],";
                        informacionPadrino += "\"numeroPruebas\":";
                        informacionPadrino += numeroPruebas;
                        informacionPadrino += "}";

                        //Se ha finalizado el analisis de la pagina

                        informacionPadrino += "],";
                        informacionPadrino += "\"numeroAsignaturas\":";
                        informacionPadrino += numeroAsignaturas;
                        informacionPadrino += "}";

                        jsonCreadoCorrectamente = true;

                        Log.i("SesionIntranet-obtenerPadrino","Se ha finalizado la creacion del JSON");

                    }catch (IOException e){
                        Log.w("SesionIntranet-obtenerPadrino","Error leyendo la respuesta del servidor.");
                        Log.w("SesionIntranet-obtenerPadrino",e);

                        error = context.getResources().getString(R.string.sesionIntranet_errorDesconocido);

                    }catch (IndexOutOfBoundsException e){
                        Log.w("SesionIntranet-obtenerPadrino","No se ha encontrado la tabla de las notas.");
                        Log.w("SesionIntranet-obtenerPadrino",e);

                        error = context.getResources().getString(R.string.sesionIntranet_errorDesconocido);
                    }finally {
                        lectorRespuesta.close();
                    }

                }else{
                    Log.w("SesionIntranet-obtenerAsignaturas","El servidor ha devuelto un valor distinto a 200. Hay un problema en el mismo.");

                    error = context.getResources().getString(R.string.sesionIntranet_errorDesconocido);
                }

            }catch (IOException e){
                error = context.getResources().getString(R.string.sesionIntranet_tiempoEsperaRespuesta);

                Log.w("SesionIntranet-obtenerPadrino","No se ha podido establecer la conexion con el servidor. Error IO.");
                Log.w("SesionIntranet-obtenerPadrino",e);
            }catch(NullPointerException e){
                error = context.getResources().getString(R.string.sesionIntranet_errorDesconocido);

                Log.w("SesionIntranet-obtenerPadrino","Null pointer exception.");
                Log.w("SesionIntranet-obtenerPadrino",e);
            }

        }

        return jsonCreadoCorrectamente;
    }

    /**
     * Metodo encargado de devolver un JSON con las asignaturas, examenes, calificaciones y enlaces
     *
     * @return String con un JSON que contiene la anterior informacion. Null si no hay nada
     */
    String getInformacionPadrino(){
        return informacionPadrino;
    }

    /**
     * Solicita a la urlPrueba las notas del examen. Devuelve un JSON con el nombre del alumno y la nota obtenida en el examen.
     * Si hay algun error, devuelve un null. El error se refleja en la variable error
     *
     * @param urlPrueba URL de la prueba de la que se desea obtener las notas.
     * @return JSON con la relacion de Examnido-Nota. null si hay algun error
     */
    String obtenerNotas(String urlPrueba){

        String jsonNotasClase = null;
        boolean formatoValido = true;

        if(conexionEstablecida && urlPrueba != null){

            try{

                URL urlPost = new URL(urlPrueba);
                HttpsURLConnection conexion = (HttpsURLConnection) urlPost.openConnection();

                //Establecer tiempo máximo de espera del servidor
                conexion.setReadTimeout(tiempoEsperaMaximo);
                conexion.setConnectTimeout(tiempoEsperaMaximo);

                conexion.setRequestMethod("GET");

                //Anadir las cabeceras
                conexion.setRequestProperty("User-Agent",userAgentPeticion );
                conexion.setRequestProperty("Accept", acceptPeticion);
                conexion.setRequestProperty("Accept-Language", acceptLanguagePeticion);

                //Anadir las cookies
                List<HttpCookie> cookies = cookieManager.getCookieStore().getCookies();
                String cookiesToSend = "";
                for (HttpCookie cookie : cookies) {
                    cookiesToSend += cookie+";";
                }
                conexion.setRequestProperty("Cookie", cookiesToSend);

                int responseCode = conexion.getResponseCode();

                if(responseCode == HttpsURLConnection.HTTP_OK){

                    Log.d("SesionIntranet-obtenerNotas","El servidor ha devuelto un 200. La conexion ha sido satisfactoria.");

                    //Leer la respuesta del servidor. Se pone el charset para que obtenga la n, acentos,....
                    BufferedReader lectorRespuesta = new BufferedReader(new InputStreamReader(conexion.getInputStream(),charsetServidorUpv));

                    try {

                        String respuestaServidor = "";
                        String lineaLeida;

                        //Mientras no se ha llegado al final
                        while ((lineaLeida = lectorRespuesta.readLine()) != null) {
                            respuestaServidor += lineaLeida;
                        }

                        //Analizar respuesta.
                        Document paginaHtmlRespuesta = Jsoup.parse(respuestaServidor);

                        if(paginaHtmlRespuesta.title().equals("Notas")){

                            Log.w("SesionIntranet-obtenerNotas","Las notas llevan mas de 30 dias en el servidor");
                            Log.w("SesionIntranet-obtenerNotas",paginaHtmlRespuesta.select("div.upv_texto").first().toString());

                            error = context.getResources().getString(R.string.sesionIntranet_notasViejas);

                        }else if(paginaHtmlRespuesta.title().equals("Error")){

                            Log.w("SesionIntranet-obtenerNotas","Error desconocido al solicitar las notas.");
                            Log.w("SesionIntranet-obtenerNotas",paginaHtmlRespuesta.select("div.upv_textoerror").first().toString());

                            error = context.getResources().getString(R.string.sesionIntranet_errorDesconocido);

                        }else {

                            //Se selecciona la tabla que tiene la clase upv_listacolumnas
                            Element tablaExaminados = paginaHtmlRespuesta.select("table.upv_listacolumnas").get(0);

                            /* Variables para el Json */

                            //Variables auxiliares
                            int numeroNotas = 0;

                            //Se empieza a definir el JSON
                            jsonNotasClase = "{";
                            jsonNotasClase += "\"notas\":";
                            jsonNotasClase += "[";

                            /* Fin de Variables para el Json */

                            //Se obtienen las filas de la tabla
                            Elements filasTablaExaminados = tablaExaminados.select("tr");

                            //Se recorren las filas. La primera se obvia, es el titulo
                            for (int i = 1; i < filasTablaExaminados.size() && formatoValido; i++) {

                                numeroNotas++; //Se incrementa en uno las asignaturas.

                                if (numeroNotas != 1) {
                                    jsonNotasClase += ",";
                                }

                                //Se escoge la fila i
                                Element filaEspecifica = filasTablaExaminados.get(i);

                                //Se obtienen las columnas de la tabla
                                Elements columnasFila = filaEspecifica.select("td");

                                //Se obtiene la informacion de dicho Examinado
                                jsonNotasClase += "{";
                                jsonNotasClase += "\"nombre\":";
                                jsonNotasClase += "\"" + columnasFila.get(0).text() + "\"";

                                jsonNotasClase += ",";

                                jsonNotasClase += "\"nota\":";
                                //jsonNotasClase += columnasFila.get(1).text().replace(",", ".");

                                //Si el valor de la nota esta vacio se pone un -1
                                if( columnasFila.get(1).text().replace(",", ".").equals("") ){
                                    jsonNotasClase += "-1";
                                }else {

                                    try{
                                        //Si es un valor numerico se suma
                                        Double.parseDouble(columnasFila.get(1).text().replace(",", "."));

                                        jsonNotasClase += columnasFila.get(1).text().replace(",", ".");

                                    }catch(NumberFormatException | NullPointerException ex){
                                        //Si no es un valor numerico se devuelve un error
                                        formatoValido = false;

                                        Log.w("SesionIntranet-obtenerNotas","El valor de la nota no es numerico.");
                                        Log.w("SesionIntranet-obtenerNotas",ex);

                                        error = context.getResources().getString(R.string.sesionIntranet_notasNoNumericas);

                                    }

                                }

                                jsonNotasClase += "}";

                            }
                            //Se añade a la ultima asignatura el numero de pruebas
                            jsonNotasClase += "],";
                            jsonNotasClase += "\"numeroNotas\":";
                            jsonNotasClase += numeroNotas;
                            jsonNotasClase += "}";

                            Log.i("SesionIntranet-obtenerNotas", "Se ha finalizado la creacion del JSON");
                        }

                    }catch (IOException e){
                        Log.w("SesionIntranet-obtenerNotas","Error leyendo la respuesta del servidor.");
                        Log.w("SesionIntranet-obtenerNotas",e);

                        error = context.getResources().getString(R.string.sesionIntranet_errorDesconocido);

                    }catch (IndexOutOfBoundsException e){
                        Log.w("SesionIntranet-obtenerNotas","No se ha encontrado la tabla de los nombres.");
                        Log.w("SesionIntranet-obtenerNotas",e);

                        error = context.getResources().getString(R.string.sesionIntranet_errorDesconocido);
                    }finally {
                        lectorRespuesta.close();
                    }

                }else{
                    Log.w("SesionIntranet-obtenerNotas","El servidor ha devuelto un valor distinto a 200. Hay un problema en el mismo.");

                    error = context.getResources().getString(R.string.sesionIntranet_errorDesconocido);
                }

            }catch (IOException e){
                error = context.getResources().getString(R.string.sesionIntranet_tiempoEsperaRespuesta);

                Log.w("SesionIntranet-obtenerNotas","No se ha podido establecer la conexion con el servidor. Error IO.");
                Log.w("SesionIntranet-obtenerNotas",e);
            }catch(NullPointerException e){
                error = context.getResources().getString(R.string.sesionIntranet_errorDesconocido);

                Log.w("SesionIntranet-obtenerNotas","Null pointer exception.");
                Log.w("SesionIntranet-obtenerNotas",e);
            }

        }

        if(!formatoValido){
            jsonNotasClase = null;

            Log.d("SesionIntranet-obtenerNotas","JSON con valor NULL.");
        }

        return jsonNotasClase;

    }

}


