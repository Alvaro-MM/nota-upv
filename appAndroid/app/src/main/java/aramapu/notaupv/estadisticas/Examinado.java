package aramapu.notaupv.estadisticas;

import java.io.Serializable;

/**
 * La clase Examinado se usara para crear los objetos a mostrar en el listado de notas.
 *
 * @author Mario Aragones Lozano
 */
public class Examinado implements Serializable {

    private String nombre;
    private double nota;
    private int posicion;
    private int numeroNotasIguales;

    /**
     * Constructor
     *
     * @param nombre Nombre del examinado
     * @param nota Nota del examinado
     * @param posicion Posicion. 1.- Nota mas alta, ....
     */
    public Examinado(String nombre, double nota, int posicion, int numeroNotasIguales){
        this.nombre = nombre;
        this.nota = nota;
        this.posicion = posicion;
        this.numeroNotasIguales = numeroNotasIguales;
    }

    /**
     * Getter Nombre
     *
     * @return Nombre del alumno
     */
    public String getNombre(){
        return nombre;
    }

    /**
     * Getter nota
     *
     * @return nota del alumno
     */
    public double getNota(){
        return nota;
    }

    /**
     * Getter posicion
     *
     * @return Posicion que ocupa la nota del alumno
     */
    public int getPosicion() {
        return posicion;
    }

    /**
     * Getter numeroNotasIguales
     *
     * @return Numero de notas iguales correspondiente a la nota del alumno
     */
    public int getNumeroNotasIguales() {
        return numeroNotasIguales;
    }
}
