package Servidor;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;

import InterfazCliente.InterfazCliente;

public class ServidorTriquiTraque {
	/**
	 * Inicializamos el puerto y el numero maximo de conexciones que acepta el servidor
	 */
	private final int puerto = 9999;
	/**
	 * Creamos una lista de sockets, donde guardaremos los sockets que se vayan conectando
	 */
	private LinkedList<Socket> usuarios = new LinkedList();
	/**
	 * Controla el turno de un jugador
	 */
	private boolean turno;
	/**
	 * Guarda los movimientos
	 */
	private int[][] tablero=new int[3][3];
	/**
	 * Numero de veces que se juega...para controlar las X y O
	 */
	private int turnos = 1;
	//Conexion que pide el nombre del cliente
	private InterfazCliente intCli;
	/**
	 * Método para que el servidor empieze a recibir conexiones de clientes
	 */
	public void escuchar(){
		try {
			for (int i = 0; i < 3; i++) {
                for (int j = 0; j < 3; j++) {
                    tablero[i][j] = -1;
                }
            }
			//Creamos el socket servidor
			ServerSocket servidor = new ServerSocket(puerto, 2);
			System.out.println("Esperando juadores...");
			//Ciclo infinito para estar escuchando por nuevos clientes
			while(true){
				//Cuando un cliente se conecte guardamos el socket en nuestra lista
				Socket cliente = servidor.accept();
				usuarios.add(cliente);
				//Determina el turno
				int turno = turnos % 2 == 0 ? 1 : 2;
                turnos++;
				//Instanciamos un hilo que estara atendiendo al cliente y lo ponemos a escuchar
				Runnable  run = new Conectar(cliente, usuarios, turno, intCli.nomCliente(), tablero);
				Thread hilo = new Thread(run);
				hilo.start();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	//Funcion main para correr el servidor
	public static void main(String[] args) {
		ServidorTriquiTraque servidor= new ServidorTriquiTraque();
		servidor.escuchar();
	}
}
