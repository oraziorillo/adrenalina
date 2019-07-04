package server.controller.socket;

import client.controller.socket.AbstractSocketProxy;
import com.google.gson.Gson;
import common.dto_model.GameDTO;
import common.events.ModelEventListener;
import common.events.game_board_events.GameBoardEvent;
import common.events.kill_shot_track_events.KillShotTrackEvent;
import common.events.lobby_events.LobbyEvent;
import common.events.pc_board_events.PcBoardEvent;
import common.events.pc_events.PcEvent;
import common.events.requests.Request;
import common.events.square_events.SquareEvent;
import common.remote_interfaces.RemoteView;

import java.io.IOException;
import java.net.Socket;
import java.rmi.RemoteException;

import static common.Constants.REGEX;
import static common.enums.ViewMethodsEnum.*;

public class ViewSocketProxy extends AbstractSocketProxy implements RemoteView, ModelEventListener {

   private Gson gson = new Gson();


   ViewSocketProxy(Socket socket) throws IOException {
      super(socket);
   }


   @Override
   public void ack(String msg) throws RemoteException{
      out.println(ACK + REGEX + msg.replaceAll(System.lineSeparator(), REGEX));
      out.flush();
   }


   @Override
   public void notifyEvent(LobbyEvent event) throws RemoteException {
      out.println(NOTIFY_EVENT + REGEX + gson.toJson(event, LobbyEvent.class));
      out.flush();
   }

   @Override
   public void request(Request request) throws RemoteException {
      out.println(REQUEST + REGEX + gson.toJson(request));
      out.flush();
   }


   @Override
   public void chatMessage(String message) throws RemoteException {
      out.println(CHAT_MESSAGE + REGEX + message.replaceAll(System.lineSeparator(), REGEX));
      out.flush();
   }
   
   
   @Override
   public void onGameBoardUpdate(GameBoardEvent event) throws RemoteException{
      out.println(ON_GAME_BOARD_UPDATE + REGEX + gson.toJson(event));
      out.flush();
   }


   @Override
   public void onKillShotTrackUpdate(KillShotTrackEvent event) throws RemoteException {
      out.println(ON_KILL_SHOT_TRACK_UPDATE + REGEX + gson.toJson(event));
      out.flush();
   }


   @Override
   public void onPcBoardUpdate(PcBoardEvent event) throws RemoteException {
      out.println(ON_PC_BOARD_UPDATE + REGEX + gson.toJson(event));
      out.flush();
   }


   @Override
   public void onPcUpdate(PcEvent event) throws RemoteException {
      out.println(ON_PC_UPDATE + REGEX + gson.toJson(event));
      out.flush();
   }


   @Override
   public void onSquareUpdate(SquareEvent event) throws RemoteException {
      out.println(ON_SQUARE_UPDATE + REGEX + gson.toJson(event));
      out.flush();
   }


   @Override
   public ModelEventListener getListener() throws RemoteException {
      return this;
   }

   @Override
   public void resumeGame(GameDTO game) throws RemoteException {

   }


   @Override
   public boolean isReachable() throws RemoteException {
      return true;
   }
}
