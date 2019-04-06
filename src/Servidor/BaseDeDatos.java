package Servidor;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Collection;
import java.util.LinkedList;

public class BaseDeDatos {
	// -----------------------------------------------------------------
    // Atributos
    // -----------------------------------------------------------------

    /**
     * Conexión a la base de datos
     */
    private Connection conexion;
    //constantes
    public static final String USER="dayron";
    public static final String RUTA="jdbc:postgresql://localhost:5432/TriquiTraque";
    public static final String PASSWORD="unimar";
    /**
     * Conjunto de propiedades que contienen la configuración de la aplicación
     */

    // -----------------------------------------------------------------
    // Constructores
    // -----------------------------------------------------------------

    /**
     * Construye el administrador de resultados y lo deja listo para conectarse a la base de datos
     * @param propiedades Las propiedades para la configuración del administrador - Debe tener las propiedades "admin.db.path", "admin.db.driver", "admin.db.url" y
     *        "admin.db.shutdown"
     */
    public BaseDeDatos( )
    {
        try {
			conectarABD();
			inicializarTabla();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

    // -----------------------------------------------------------------
    // Métodos
    // -----------------------------------------------------------------

    /**
     * Conecta el administrador a la base de datos
     * @throws SQLException Se lanza esta excepción si hay problemas realizando la operación
     * @throws Exception Se lanza esta excepción si hay problemas con los controladores
     */
    public void conectarABD( ) throws SQLException, Exception
    {
        Class.forName("org.postgresql.Driver");
        conexion=DriverManager.getConnection(RUTA, USER, PASSWORD);
    }

    /**
     * Desconecta el administrador de la base de datos y la detiene
     * @throws SQLException Se lanza esta excepción si hay problemas realizando la operación
     */
    public void desconectarBD( ) throws SQLException
    { 
        conexion.close( );
    }

    /**
     * Crea la tabla necesaria para guardar los resultados. Si la tabla ya estaba creada entonces no hace nada. <br>
     * @throws SQLException Se lanza esta excepción si hay problemas creando la tabla
     */
    public void inicializarTabla( ) throws SQLException
    {
        Statement s = conexion.createStatement( );

        boolean crearTabla = false;
        try
        {
            // Verificar si ya existe la tabla resultados
            s.executeQuery( "SELECT * FROM resultados WHERE 1=2" );
        }
        catch( SQLException se )
        {
            // La excepción se lanza si la tabla resultados no existe
            crearTabla = true;
        }

        // Se crea una nueva tabla vacía
        if( crearTabla )
        {
            s.execute( "CREATE TABLE resultados (nombre character varying(32), ganados int, perdidos int, empates int, PRIMARY KEY (nombre))" );
        }

        s.close( );
    }
    /**
     * Registra un nuevo jugador
     */
    public String registarJugador(String nombre) throws SQLException{
    	String[] registro = null;
    	
    	Statement st = conexion.createStatement( );
    	String insert = "INSERT INTO resultados (nombre, ganados, perdidos, empates, tipo) VALUES ('" + nombre + "', 0, 0, 0)";
        st.execute( insert );

        registro[0]=nombre;
        registro[1]="0";
        registro[2]="0";
        registro[3]="0";
        
        if(consultarRegistroJugador(nombre)!=null) return nombre;
        return null;
    }
    /**
     * Este método se encarga de consultar la información de un jugador (encuentros ganados y encuentros perdidos). <br>
     * Si no se encuentra un registro del jugador en la base de datos, entonces se crea uno nuevo.
     * @param nombre El nombre del jugador del cual se está buscando información - nombre != null
     * @return Retorna el registro de victorias y derrotas del jugador
     * @throws SQLException Se lanza esta excepción si hay problemas en la comunicación con la base de datos
     */
    public String[] consultarRegistroJugador( String nombre ) throws SQLException
    {
        String[] registro = null;

        String sql = "SELECT ganados, perdidos, empates FROM resultados WHERE nombre ='" + nombre + "'";

        Statement st = conexion.createStatement( );
        ResultSet resultado = st.executeQuery( sql );

        if( resultado.next( ) ) // Se encontró el jugador
        {
            int ganados = resultado.getInt( 1 );
            int perdidos = resultado.getInt( 2 );
            int empates = resultado.getInt( 3 );
            registro[0]=nombre;
            registro[1]=String.valueOf(ganados);
            registro[2]=String.valueOf(perdidos);
            registro[3]=String.valueOf(empates);
            resultado.close( );
        }
        else
        // Hay que crear un nuevo registro porque es un jugador nuevo
        {
            resultado.close( );
            registarJugador(nombre);
        }
        st.close( );
        return registro;
    }

    /**
     * Este método se encarga de consultar la información de todos los jugadores (encuentros ganados y encuentros perdidos).
     * @return Retorna una colección de los registros (RegistroJugador) de victorias y derrotas
     * @throws SQLException Se lanza esta excepción si hay problemas en la comunicación con la base de datos
     */
    public String[] consultarRegistrosJugadores( ) throws SQLException
    {
        String[] registros = null;
        String registro="";
        int contador=0;
        
        String sql = "SELECT nombre, ganados, perdidos, empates FROM resultados";

        Statement st = conexion.createStatement( );
        ResultSet resultado = st.executeQuery( sql );

        while( resultado.next( ) )
        {
        	contador++;
            String nombre = resultado.getString( 1 );
            String ganados = resultado.getString( 2 );
            String perdidos = resultado.getString( 3 );
            String empates = resultado.getString( 4 );
            registro += nombre+":"+ganados+":"+perdidos+":"+empates;
            registros[contador]=registro;
        }

        resultado.close( );
        st.close( );

        return registros;
    }

    /**
     * Este método se encarga de registrar una victoria más a un jugador
     * @param nombre El nombre del jugador al cual se le va a registrar la victoria - nombre != null && corresponde a un registro en la base de datos
     * @throws SQLException Se lanza esta excepción si hay problemas en la comunicación con la base de datos
     */
    public void registrarVictoria( String nombre ) throws SQLException
    {
        String sql = "UPDATE resultados SET ganados = ganados+1 WHERE nombre ='" + nombre + "'";

        Statement st = conexion.createStatement( );
        st.executeUpdate( sql );
        st.close( );
    }

    /**
     * Este método se encarga de registrar una derrota más a un jugador
     * @param nombre El nombre del jugador al cual se le va a registrar la derrota - nombre != null && corresponde a un registro en la base de datos
     * @throws SQLException Se lanza esta excepción si hay problemas en la comunicación con la base de datos
     */
    public void registrarDerrota( String nombre ) throws SQLException
    {
        String sql = "UPDATE resultados SET perdidos = perdidos+1 WHERE nombre ='" + nombre + "'";

        Statement st = conexion.createStatement( );
        st.executeUpdate( sql );
        st.close( );
    }
    
    /**
     * Registra que hubo un empate
     * @param string mensaje
     */
    public void registrarEmpate(String nombre1, String nombre2) throws SQLException{
		String sql1="UPDATE resultados SET empate=empate+1 WHERE nombre ='" + nombre1 + "'";
		String sql2="UPDATE resultados SET empate=empate+1 WHERE nombre ='" + nombre2 + "'";
		Statement st=conexion.createStatement();
		st.executeUpdate(sql1);
		st.executeUpdate(sql2);
		st.close();
	}
}
