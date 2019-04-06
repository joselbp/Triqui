package Servidor;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.LinkedList;

import Cliente.TriquiTraque;

public class Conectar implements Runnable{
	//Declaramos las variables que utiliza el hilo para estar recibiendo y mandando mensajes
	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;
	/**
	 * Varible para guardar que le toco al jugador X o O
	 */
	private int tipo;
	/**
	 * Matriz del juego
	 */
	private int tablero[][];
	/**
	 * Turno
	 */
	private boolean turno;
	/**
	 * Lista de los usuarios conectados al servidor
	 */
	private LinkedList<Socket> usuarios = new LinkedList();
	/**
	 * Nombre del cliente
	 */
	private String[] nombre;

	//Constructor que recibe el socket que atendera el hilo y la lista de los jugadores el turno y la matriz del juego
	public Conectar(Socket soc,LinkedList users,int tipoP, String nom,int[][] tabJ){
		socket = soc;
		usuarios = users;
		tipo = tipoP;
		tablero = tabJ;
		int temp=(tipo==1)?0:1;
		nombre[temp]=nom;
	}


	@Override
	public void run() {
		try {
			//Inicializamos los canales de comunicacion y mandamos el turno a cada jugador
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
			out = new PrintWriter(socket.getOutputStream(), true);
			turno = tipo == 1;
			String msg = "";
			msg += "Turno " + (turno ? "X:":"O:");
			msg +=turno;
			out.println(msg);

			//Ciclo infinito que estara escuchando por los movimientos de cada jugador
			//Cada que un jugador pone una X o O viene aca y le dice al otro jugador que es su turno
			while(true){
				//Leer los datos que se mandan cuando se selecciona un boton
				String recibidos = in.readLine();
				String[] recibido=recibidos.split(":");
				/*
                    recibido[0] : fila del tablero
                    recibido[1] : columna del tablero

				 */
				int f = Integer.parseInt(recibido[0]);
				int c = Integer.parseInt(recibido[1]);
				/*
                    Se guarda la jugada en la matriz
                    X : 1
                    O : 0

				 */
				tablero[f][c] = tipo;
				/*
                Se forma una cadena que se enviara a los jugadores, que lleva informacion del movimiento que se 
                acaba de hacer
				 */
				String cad = "";
				cad += tipo+":";
				cad += f+":";
				cad += c+":";
				/*
                Se comprueba si alguien de los jugadores gano
                y si el tablero ya se lleno... En los dos casos se notifica a los jugadores para empezar de nuevo la partida
				 */
				boolean ganador = gano(tipo);
				boolean completo = lleno();

				if(!ganador && !completo){
					cad += TriquiTraque.JUEGA;
				}
				else if(!ganador && completo){
					cad += TriquiTraque.EMPATE;
				}
				else if(ganador){
					vaciarMatriz();
					cad += tipo == 1 ? "X:":"O:";
				}
				
				//Busca el nombre del jugador o el oponente
				if(tipo==1){
					cad+="Cliente1 "+nombre[0];
				}else cad+="Cliente2 "+nombre[1];
				
				for(Socket usuario : usuarios) {
					out = new PrintWriter(usuario.getOutputStream(), true);
					out.println(cad);
				}
			}
		} catch (Exception e) {
			//Si ocurre un excepcion lo mas seguro es que sea por que algun jugador se desconecto asi que lo quitamos de la lista de conectados
			for (int i = 0; i < usuarios.size(); i++) {
				if(usuarios.get(i) == socket){
					usuarios.remove(i);
					break;
				} 
			}
			vaciarMatriz();
		}
	}
	//Funcion comprueba si algun jugador ha ganado el juego
	public boolean gano(int n){
		for (int i = 0; i < 3; i++) {
			boolean gano = true;
			for (int j = 0; j < 3; j++) {
				gano = gano && (tablero[i][j] == n); 
			}
			if(gano){
				return true;
			}
		}

		for (int i = 0; i < 3; i++) {
			boolean gano = true;
			for (int j = 0; j < 3; j++) {
				gano = gano && (tablero[j][i] == n); 
			}
			if(gano){
				return true;
			}
		}

		if(tablero[0][0] == n && tablero[1][1] == n && tablero[2][2] == n)return true;

		return false;
	}

	//Funcion comprueba si el tablero ya esta lleno
	public boolean lleno(){
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				if(tablero[i][j] == -1)return false;
			}
		}

		vaciarMatriz();
		return true;
	}

	//Funcion para reiniciar la matriz del juego
	public void vaciarMatriz(){
		for (int i = 0; i < 3; i++) {
			for (int j = 0; j < 3; j++) {
				tablero[i][j] = -1;
			}
		}
	}
}
