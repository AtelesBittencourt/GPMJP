 /*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tests;

import ai.core.AI;
import ai.*;
import ai.abstraction.WorkerRush;
import ai.abstraction.pathfinding.BFSPathFinding;
import ai.asymmetric.PGS.PGSSCriptChoiceRandom;
import ai.asymmetric.PGS.PGSmRTS;
import ai.asymmetric.PGS.PGSmRTS_v2;
import ai.asymmetric.PGS.PGSmRTS_v3;
import ai.asymmetric.PGS.POEmRTS;
import ai.asymmetric.PGS.POEmRTS_v2;
import ai.asymmetric.PGS.POEmRTS_v3;
import ai.asymmetric.SSS.SSSmRTS;
import ai.mcts.naivemcts.NaiveMCTS;
import ai.scv.SCV;
import gui.PhysicalGameStatePanel;
import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import javax.swing.JFrame;
import rts.GameState;
import rts.PhysicalGameState;
import rts.PlayerAction;
import rts.units.UnitTypeTable;
import util.XMLWriter;

/**
 *
 * @author santi
 */
public class GameVisualSimulationTestLote {
    public static void main(String args[]) throws Exception {
        String caminho = "C:\\Users\\Ateles Junior\\Documents\\2018-2\\TCC\\MicroRTS\\";
        int version = 0;
        String arq;
        int nCiclos = 100;
/*---------------------------------------------------------------------------------------------------------------------------------*/
        version = 0;
        arq = caminho + "TesteLongo16_basesWorkers16x16_v1.txt";
        //16 x 16 simples
        for(int vez = 0; vez < nCiclos; vez++){
            UnitTypeTable utt = new UnitTypeTable();
            PhysicalGameState pgs16 = PhysicalGameState.load("maps/16x16/basesWorkers16x16.xml", utt);
            
            //PhysicalGameState pgs = PhysicalGameState.load("maps/BroodWar/(4)Fortress.scxA.xml", utt);
    //        PhysicalGameState pgs = MapGenerator.basesWorkers8x8Obstacle();

            GameState gs = new GameState(pgs16, utt);
            int MAXCYCLES = 5000;
            int PERIOD = 20;
            boolean gameover = false;
            
            AI ai1;
            AI ai2;
            switch (version) {
                case 0:
                    ai1 = new POEmRTS(utt);
                    ai2 = new PGSmRTS(utt);
                    break;
                case 1:
                    ai1 = new POEmRTS_v2(utt);
                    ai2 = new PGSmRTS_v2(utt);
                    break;
                default:
                    ai1 = new POEmRTS_v3(utt);
                    ai2 = new PGSmRTS_v3(utt);
                    break;
            }

            //JFrame w = PhysicalGameStatePanel.newVisualizer(gs,640,640,false,PhysicalGameStatePanel.COLORSCHEME_BLACK);
    //        JFrame w = PhysicalGameStatePanel.newVisualizer(gs,640,640,false,PhysicalGameStatePanel.COLORSCHEME_WHITE);

            long nextTimeToUpdate = System.currentTimeMillis() + PERIOD;
            do{
                if (System.currentTimeMillis()>=nextTimeToUpdate) {

                    PlayerAction pa1 = ai1.getAction(0, gs);
                    PlayerAction pa2 = ai2.getAction(1, gs);
                    gs.issueSafe(pa1);
                    gs.issueSafe(pa2);

                    // simulate:
                    gameover = gs.cycle();
                    //w.repaint();
                    nextTimeToUpdate+=PERIOD;
                } else {
                    try {
                        Thread.sleep(1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }while(!gameover && gs.getTime()<MAXCYCLES);
            ai1.gameOver(gs.winner());
            ai2.gameOver(gs.winner());
            //System.out.println("Ganhador: " + gs.winner());
            //System.out.println("Game Over");
            try{
                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(arq, true)));
                out.println("Ganhador: " + gs.winner());
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            //muda para a segunda versão do PGS e do POE
            if(vez == 99 && version == 0){
                version = 1;
                vez = 0;
                arq = caminho + "TesteLongo16_basesWorkers16x16_v2.txt";
            } else if(vez == 99 && version == 1){
                version = 2;
                vez = 0;
                arq = caminho + "TesteLongo16_basesWorkers16x16_v3.txt";
            }
        }

/*---------------------------------------------------------------------------------------------------------------------------------*/
        //16x16 sem recurso
        version = 0;
        arq = caminho + "TesteLongo16_melee16x16Mixed8_v1.txt";
        for(int vez = 0; vez < nCiclos; vez++){
            UnitTypeTable utt = new UnitTypeTable();
            PhysicalGameState pgs16 = PhysicalGameState.load("maps/16x16/melee16x16Mixed8.xml", utt);

            //PhysicalGameState pgs = PhysicalGameState.load("maps/BroodWar/(4)Fortress.scxA.xml", utt);
    //        PhysicalGameState pgs = MapGenerator.basesWorkers8x8Obstacle();

            GameState gs = new GameState(pgs16, utt);
            int MAXCYCLES = 5000;
            int PERIOD = 20;
            boolean gameover = false;

            AI ai1;
            AI ai2;
            switch (version) {
                case 0:
                    ai1 = new POEmRTS(utt);
                    ai2 = new PGSmRTS(utt);
                    break;
                case 1:
                    ai1 = new POEmRTS_v2(utt);
                    ai2 = new PGSmRTS_v2(utt);
                    break;
                default:
                    ai1 = new POEmRTS_v3(utt);
                    ai2 = new PGSmRTS_v3(utt);
                    break;
            }

            //JFrame w = PhysicalGameStatePanel.newVisualizer(gs,640,640,false,PhysicalGameStatePanel.COLORSCHEME_BLACK);
    //        JFrame w = PhysicalGameStatePanel.newVisualizer(gs,640,640,false,PhysicalGameStatePanel.COLORSCHEME_WHITE);

            long nextTimeToUpdate = System.currentTimeMillis() + PERIOD;
            do{
                if (System.currentTimeMillis()>=nextTimeToUpdate) {

                    PlayerAction pa1 = ai1.getAction(0, gs);
                    PlayerAction pa2 = ai2.getAction(1, gs);
                    gs.issueSafe(pa1);
                    gs.issueSafe(pa2);

                    // simulate:
                    gameover = gs.cycle();
                    //w.repaint();
                    nextTimeToUpdate+=PERIOD;
                } else {
                    try {
                        Thread.sleep(1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }while(!gameover && gs.getTime()<MAXCYCLES);
            ai1.gameOver(gs.winner());
            ai2.gameOver(gs.winner());
            //System.out.println("Ganhador: " + gs.winner());
            //System.out.println("Game Over");
            try{
                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(arq, true)));
                out.println("Ganhador: " + gs.winner());
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            //muda para a segunda versão do PGS e do POE
            if(vez == 99 && version == 0){
                version = 1;
                vez = 0;
                arq = caminho + "TesteLongo16_melee16x16Mixed8_v2.txt";
            } else if(vez == 99 && version == 1){
                version = 2;
                vez = 0;
                arq = caminho + "TesteLongo16_melee16x16Mixed8_v3.txt";
            }
        }

/*---------------------------------------------------------------------------------------------------------------------------------*/
        //16x16 BasesTwoBarracksWithWalls16x16
        version = 0;
        arq = caminho + "TesteLongo16_BasesTwoBarracksWithWalls16x16_v1.txt";
        for(int vez = 0; vez < nCiclos; vez++){
            UnitTypeTable utt = new UnitTypeTable();
            PhysicalGameState pgs16 = PhysicalGameState.load("maps/16x16/BasesTwoBarracksWithWalls16x16.xml", utt);
            


            //PhysicalGameState pgs = PhysicalGameState.load("maps/BroodWar/(4)Fortress.scxA.xml", utt);
    //        PhysicalGameState pgs = MapGenerator.basesWorkers8x8Obstacle();

            GameState gs = new GameState(pgs16, utt);
            int MAXCYCLES = 5000;
            int PERIOD = 20;
            boolean gameover = false;

            AI ai1;
            AI ai2;
            switch (version) {
                case 0:
                    ai1 = new POEmRTS(utt);
                    ai2 = new PGSmRTS(utt);
                    break;
                case 1:
                    ai1 = new POEmRTS_v2(utt);
                    ai2 = new PGSmRTS_v2(utt);
                    break;
                default:
                    ai1 = new POEmRTS_v3(utt);
                    ai2 = new PGSmRTS_v3(utt);
                    break;
            }

            //JFrame w = PhysicalGameStatePanel.newVisualizer(gs,640,640,false,PhysicalGameStatePanel.COLORSCHEME_BLACK);
    //        JFrame w = PhysicalGameStatePanel.newVisualizer(gs,640,640,false,PhysicalGameStatePanel.COLORSCHEME_WHITE);

            long nextTimeToUpdate = System.currentTimeMillis() + PERIOD;
            do{
                if (System.currentTimeMillis()>=nextTimeToUpdate) {

                    PlayerAction pa1 = ai1.getAction(0, gs);
                    PlayerAction pa2 = ai2.getAction(1, gs);
                    gs.issueSafe(pa1);
                    gs.issueSafe(pa2);

                    // simulate:
                    gameover = gs.cycle();
                    //w.repaint();
                    nextTimeToUpdate+=PERIOD;
                } else {
                    try {
                        Thread.sleep(1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }while(!gameover && gs.getTime()<MAXCYCLES);
            ai1.gameOver(gs.winner());
            ai2.gameOver(gs.winner());
            //System.out.println("Ganhador: " + gs.winner());
            //System.out.println("Game Over");
            try{
                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(arq, true)));
                out.println("Ganhador: " + gs.winner());
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            //muda para a segunda versão do PGS e do POE
            if(vez == 99 && version == 0){
                version = 1;
                vez = 0;
                arq = caminho + "TesteLongo16_BasesTwoBarracksWithWalls16x16_v2.txt";
            } else if(vez == 99 && version == 1){
                version = 2;
                vez = 0;
                arq = caminho + "TesteLongo16_BasesTwoBarracksWithWalls16x16_v3.txt";
            }
        }

/*---------------------------------------------------------------------------------------------------------------------------------*/
        //32x32 só base
        version = 0;
        arq = caminho + "TesteLongo32_basesWorkers32x32A_v1.txt";
        for(int vez = 0; vez < nCiclos; vez++){
            UnitTypeTable utt = new UnitTypeTable();
            PhysicalGameState pgs16 = PhysicalGameState.load("maps/32x32/basesWorkers32x32A.xml", utt);
            


            //PhysicalGameState pgs = PhysicalGameState.load("maps/BroodWar/(4)Fortress.scxA.xml", utt);
    //        PhysicalGameState pgs = MapGenerator.basesWorkers8x8Obstacle();

            GameState gs = new GameState(pgs16, utt);
            int MAXCYCLES = 5000;
            int PERIOD = 20;
            boolean gameover = false;

            AI ai1;
            AI ai2;
            switch (version) {
                case 0:
                    ai1 = new POEmRTS(utt);
                    ai2 = new PGSmRTS(utt);
                    break;
                case 1:
                    ai1 = new POEmRTS_v2(utt);
                    ai2 = new PGSmRTS_v2(utt);
                    break;
                default:
                    ai1 = new POEmRTS_v3(utt);
                    ai2 = new PGSmRTS_v3(utt);
                    break;
            }

            //JFrame w = PhysicalGameStatePanel.newVisualizer(gs,640,640,false,PhysicalGameStatePanel.COLORSCHEME_BLACK);
    //        JFrame w = PhysicalGameStatePanel.newVisualizer(gs,640,640,false,PhysicalGameStatePanel.COLORSCHEME_WHITE);

            long nextTimeToUpdate = System.currentTimeMillis() + PERIOD;
            do{
                if (System.currentTimeMillis()>=nextTimeToUpdate) {

                    PlayerAction pa1 = ai1.getAction(0, gs);
                    PlayerAction pa2 = ai2.getAction(1, gs);
                    gs.issueSafe(pa1);
                    gs.issueSafe(pa2);

                    // simulate:
                    gameover = gs.cycle();
                    //w.repaint();
                    nextTimeToUpdate+=PERIOD;
                } else {
                    try {
                        Thread.sleep(1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }while(!gameover && gs.getTime()<MAXCYCLES);
            ai1.gameOver(gs.winner());
            ai2.gameOver(gs.winner());
            //System.out.println("Ganhador: " + gs.winner());
            //System.out.println("Game Over");
            try{
                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(arq, true)));
                out.println("Ganhador: " + gs.winner());
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            //muda para a segunda versão do PGS e do POE
            if(vez == 99 && version == 0){
                version = 1;
                vez = 0;
                arq = caminho + "TesteLongo32_basesWorkers32x32A_v2.txt";
            } else if(vez == 99 && version == 1){
                version = 2;
                vez = 0;
                arq = caminho + "TesteLongo32_basesWorkers32x32A_v3.txt";
            }
        }

/*---------------------------------------------------------------------------------------------------------------------------------*/
        //32x32 mesmo mapa anterior, mas já começa com uma barrack
        version = 0;
        arq = caminho + "TesteLongo32_basesWorkersBarracks32x32_v1.txt";
        for(int vez = 0; vez < nCiclos; vez++){
            UnitTypeTable utt = new UnitTypeTable();
            PhysicalGameState pgs16 = PhysicalGameState.load("maps/32x32/basesWorkersBarracks32x32.xml", utt);
            


            //PhysicalGameState pgs = PhysicalGameState.load("maps/BroodWar/(4)Fortress.scxA.xml", utt);
    //        PhysicalGameState pgs = MapGenerator.basesWorkers8x8Obstacle();

            GameState gs = new GameState(pgs16, utt);
            int MAXCYCLES = 5000;
            int PERIOD = 20;
            boolean gameover = false;

            AI ai1;
            AI ai2;
            switch (version) {
                case 0:
                    ai1 = new POEmRTS(utt);
                    ai2 = new PGSmRTS(utt);
                    break;
                case 1:
                    ai1 = new POEmRTS_v2(utt);
                    ai2 = new PGSmRTS_v2(utt);
                    break;
                default:
                    ai1 = new POEmRTS_v3(utt);
                    ai2 = new PGSmRTS_v3(utt);
                    break;
            }

            //JFrame w = PhysicalGameStatePanel.newVisualizer(gs,640,640,false,PhysicalGameStatePanel.COLORSCHEME_BLACK);
    //        JFrame w = PhysicalGameStatePanel.newVisualizer(gs,640,640,false,PhysicalGameStatePanel.COLORSCHEME_WHITE);

            long nextTimeToUpdate = System.currentTimeMillis() + PERIOD;
            do{
                if (System.currentTimeMillis()>=nextTimeToUpdate) {

                    PlayerAction pa1 = ai1.getAction(0, gs);
                    PlayerAction pa2 = ai2.getAction(1, gs);
                    gs.issueSafe(pa1);
                    gs.issueSafe(pa2);

                    // simulate:
                    gameover = gs.cycle();
                    //w.repaint();
                    nextTimeToUpdate+=PERIOD;
                } else {
                    try {
                        Thread.sleep(1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }while(!gameover && gs.getTime()<MAXCYCLES);
            ai1.gameOver(gs.winner());
            ai2.gameOver(gs.winner());
            //System.out.println("Ganhador: " + gs.winner());
            //System.out.println("Game Over");
            try{
                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(arq, true)));
                out.println("Ganhador: " + gs.winner());
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            //muda para a segunda versão do PGS e do POE
            if(vez == 99 && version == 0){
                version = 1;
                vez = 0;
                arq = caminho + "TesteLongo32_basesWorkersBarracks32x32_v2.txt";
            } else if(vez == 99 && version == 1){
                version = 2;
                vez = 0;
                arq = caminho + "TesteLongo32_basesWorkersBarracks32x32_v3.txt";
            }
        }

/*---------------------------------------------------------------------------------------------------------------------------------*/
        //32x32 mapa com barreiras
        version = 0;
        arq = caminho + "TesteLongo32_RunToFight32x32A_v1.txt";
        for(int vez = 0; vez < nCiclos; vez++){
            UnitTypeTable utt = new UnitTypeTable();
            PhysicalGameState pgs16 = PhysicalGameState.load("maps/32x32/RunToFight32x32A.xml", utt);
            


            //PhysicalGameState pgs = PhysicalGameState.load("maps/BroodWar/(4)Fortress.scxA.xml", utt);
    //        PhysicalGameState pgs = MapGenerator.basesWorkers8x8Obstacle();

            GameState gs = new GameState(pgs16, utt);
            int MAXCYCLES = 5000;
            int PERIOD = 20;
            boolean gameover = false;

            AI ai1;
            AI ai2;
            switch (version) {
                case 0:
                    ai1 = new POEmRTS(utt);
                    ai2 = new PGSmRTS(utt);
                    break;
                case 1:
                    ai1 = new POEmRTS_v2(utt);
                    ai2 = new PGSmRTS_v2(utt);
                    break;
                default:
                    ai1 = new POEmRTS_v3(utt);
                    ai2 = new PGSmRTS_v3(utt);
                    break;
            }

            //JFrame w = PhysicalGameStatePanel.newVisualizer(gs,640,640,false,PhysicalGameStatePanel.COLORSCHEME_BLACK);
    //        JFrame w = PhysicalGameStatePanel.newVisualizer(gs,640,640,false,PhysicalGameStatePanel.COLORSCHEME_WHITE);

            long nextTimeToUpdate = System.currentTimeMillis() + PERIOD;
            do{
                if (System.currentTimeMillis()>=nextTimeToUpdate) {

                    PlayerAction pa1 = ai1.getAction(0, gs);
                    PlayerAction pa2 = ai2.getAction(1, gs);
                    gs.issueSafe(pa1);
                    gs.issueSafe(pa2);

                    // simulate:
                    gameover = gs.cycle();
                    //w.repaint();
                    nextTimeToUpdate+=PERIOD;
                } else {
                    try {
                        Thread.sleep(1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }while(!gameover && gs.getTime()<MAXCYCLES);
            ai1.gameOver(gs.winner());
            ai2.gameOver(gs.winner());
            //System.out.println("Ganhador: " + gs.winner());
            //System.out.println("Game Over");
            try{
                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(arq, true)));
                out.println("Ganhador: " + gs.winner());
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            //muda para a segunda versão do PGS e do POE
            if(vez == 99 && version == 0){
                version = 1;
                vez = 0;
                arq = caminho + "TesteLongo32_RunToFight32x32A_v2.txt";
            } else if(vez == 99 && version == 1){
                version = 2;
                vez = 0;
                arq = caminho + "TesteLongo32_RunToFight32x32A_v3.txt";
            }
        }

/*---------------------------------------------------------------------------------------------------------------------------------*/
        //64x64
        version = 0;
        arq = caminho + "TesteLongo64_SimplePathToFight64x64_v1.txt";
        for(int vez = 0; vez < nCiclos; vez++){
            UnitTypeTable utt = new UnitTypeTable();
            PhysicalGameState pgs16 = PhysicalGameState.load("maps/64x64/SimplePathToFight64x64.xml", utt);
            


            //PhysicalGameState pgs = PhysicalGameState.load("maps/BroodWar/(4)Fortress.scxA.xml", utt);
    //        PhysicalGameState pgs = MapGenerator.basesWorkers8x8Obstacle();

            GameState gs = new GameState(pgs16, utt);
            int MAXCYCLES = 5000;
            int PERIOD = 20;
            boolean gameover = false;

            AI ai1;
            AI ai2;
            switch (version) {
                case 0:
                    ai1 = new POEmRTS(utt);
                    ai2 = new PGSmRTS(utt);
                    break;
                case 1:
                    ai1 = new POEmRTS_v2(utt);
                    ai2 = new PGSmRTS_v2(utt);
                    break;
                default:
                    ai1 = new POEmRTS_v3(utt);
                    ai2 = new PGSmRTS_v3(utt);
                    break;
            }

            //JFrame w = PhysicalGameStatePanel.newVisualizer(gs,640,640,false,PhysicalGameStatePanel.COLORSCHEME_BLACK);
    //        JFrame w = PhysicalGameStatePanel.newVisualizer(gs,640,640,false,PhysicalGameStatePanel.COLORSCHEME_WHITE);

            long nextTimeToUpdate = System.currentTimeMillis() + PERIOD;
            do{
                if (System.currentTimeMillis()>=nextTimeToUpdate) {

                    PlayerAction pa1 = ai1.getAction(0, gs);
                    PlayerAction pa2 = ai2.getAction(1, gs);
                    gs.issueSafe(pa1);
                    gs.issueSafe(pa2);

                    // simulate:
                    gameover = gs.cycle();
                    //w.repaint();
                    nextTimeToUpdate+=PERIOD;
                } else {
                    try {
                        Thread.sleep(1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }while(!gameover && gs.getTime()<MAXCYCLES);
            ai1.gameOver(gs.winner());
            ai2.gameOver(gs.winner());
            //System.out.println("Ganhador: " + gs.winner());
            //System.out.println("Game Over");
            try{
                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(arq, true)));
                out.println("Ganhador: " + gs.winner());
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            //muda para a segunda versão do PGS e do POE
            if(vez == 99 && version == 0){
                version = 1;
                vez = 0;
                arq = caminho + "TesteLongo64_SimplePathToFight64x64_v2.txt";
            } else if(vez == 99 && version == 1){
                version = 2;
                vez = 0;
                arq = caminho + "TesteLongo64_SimplePathToFight64x64_v3.txt";
            }
        }
/*---------------------------------------------------------------------------------------------------------------------------------*/
        //Tamanho real
        version = 0;
        arq = caminho + "TesteLongo_(3)TauCross.scxA_v1.txt";
        for(int vez = 0; vez < nCiclos; vez++){
            UnitTypeTable utt = new UnitTypeTable();
            PhysicalGameState pgs16 = PhysicalGameState.load("maps/BroodWar/(3)TauCross.scxA.xml", utt);
            


            //PhysicalGameState pgs = PhysicalGameState.load("maps/BroodWar/(4)Fortress.scxA.xml", utt);
    //        PhysicalGameState pgs = MapGenerator.basesWorkers8x8Obstacle();

            GameState gs = new GameState(pgs16, utt);
            int MAXCYCLES = 5000;
            int PERIOD = 20;
            boolean gameover = false;

            AI ai1;
            AI ai2;
            switch (version) {
                case 0:
                    ai1 = new POEmRTS(utt);
                    ai2 = new PGSmRTS(utt);
                    break;
                case 1:
                    ai1 = new POEmRTS_v2(utt);
                    ai2 = new PGSmRTS_v2(utt);
                    break;
                default:
                    ai1 = new POEmRTS_v3(utt);
                    ai2 = new PGSmRTS_v3(utt);
                    break;
            }

            //JFrame w = PhysicalGameStatePanel.newVisualizer(gs,640,640,false,PhysicalGameStatePanel.COLORSCHEME_BLACK);
    //        JFrame w = PhysicalGameStatePanel.newVisualizer(gs,640,640,false,PhysicalGameStatePanel.COLORSCHEME_WHITE);

            long nextTimeToUpdate = System.currentTimeMillis() + PERIOD;
            do{
                if (System.currentTimeMillis()>=nextTimeToUpdate) {

                    PlayerAction pa1 = ai1.getAction(0, gs);
                    PlayerAction pa2 = ai2.getAction(1, gs);
                    gs.issueSafe(pa1);
                    gs.issueSafe(pa2);

                    // simulate:
                    gameover = gs.cycle();
                    //w.repaint();
                    nextTimeToUpdate+=PERIOD;
                } else {
                    try {
                        Thread.sleep(1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }while(!gameover && gs.getTime()<MAXCYCLES);
            ai1.gameOver(gs.winner());
            ai2.gameOver(gs.winner());
            //System.out.println("Ganhador: " + gs.winner());
            //System.out.println("Game Over");
            try{
                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(arq, true)));
                out.println("Ganhador: " + gs.winner());
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            //muda para a segunda versão do PGS e do POE
            if(vez == 99 && version == 0){
                version = 1;
                vez = 0;
                arq = caminho + "TesteLongo_(3)TauCross.scxA_v2.txt";
            } else if(vez == 99 && version == 1){
                version = 2;
                vez = 0;
                arq = caminho + "TesteLongo_(3)TauCross.scxA_v3.txt";
            }
        }

/*---------------------------------------------------------------------------------------------------------------------------------*/
        //Tamanho real
        version = 0;
        arq = caminho + "TesteLongo_(4)CircuitBreaker.scxA_v1.txt";
        for(int vez = 0; vez < nCiclos; vez++){
            UnitTypeTable utt = new UnitTypeTable();
            PhysicalGameState pgs16 = PhysicalGameState.load("maps/BroodWar/(4)CircuitBreaker.scxA.xml", utt);
            


            //PhysicalGameState pgs = PhysicalGameState.load("maps/BroodWar/(4)Fortress.scxA.xml", utt);
    //        PhysicalGameState pgs = MapGenerator.basesWorkers8x8Obstacle();

            GameState gs = new GameState(pgs16, utt);
            int MAXCYCLES = 5000;
            int PERIOD = 20;
            boolean gameover = false;

            AI ai1;
            AI ai2;
            switch (version) {
                case 0:
                    ai1 = new POEmRTS(utt);
                    ai2 = new PGSmRTS(utt);
                    break;
                case 1:
                    ai1 = new POEmRTS_v2(utt);
                    ai2 = new PGSmRTS_v2(utt);
                    break;
                default:
                    ai1 = new POEmRTS_v3(utt);
                    ai2 = new PGSmRTS_v3(utt);
                    break;
            }

            //JFrame w = PhysicalGameStatePanel.newVisualizer(gs,640,640,false,PhysicalGameStatePanel.COLORSCHEME_BLACK);
    //        JFrame w = PhysicalGameStatePanel.newVisualizer(gs,640,640,false,PhysicalGameStatePanel.COLORSCHEME_WHITE);

            long nextTimeToUpdate = System.currentTimeMillis() + PERIOD;
            do{
                if (System.currentTimeMillis()>=nextTimeToUpdate) {

                    PlayerAction pa1 = ai1.getAction(0, gs);
                    PlayerAction pa2 = ai2.getAction(1, gs);
                    gs.issueSafe(pa1);
                    gs.issueSafe(pa2);

                    // simulate:
                    gameover = gs.cycle();
                    //w.repaint();
                    nextTimeToUpdate+=PERIOD;
                } else {
                    try {
                        Thread.sleep(1);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }while(!gameover && gs.getTime()<MAXCYCLES);
            ai1.gameOver(gs.winner());
            ai2.gameOver(gs.winner());
            //System.out.println("Ganhador: " + gs.winner());
            //System.out.println("Game Over");
            try{
                PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(arq, true)));
                out.println("Ganhador: " + gs.winner());
                out.flush();
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            
            //muda para a segunda versão do PGS e do POE
            if(vez == 99 && version == 0){
                version = 1;
                vez = 0;
                arq = caminho + "TesteLongo64_(4)CircuitBreaker.scxA_v2.txt";
            } else if(vez == 99 && version == 1){
                version = 2;
                vez = 0;
                arq = caminho + "TesteLongo64_(4)CircuitBreaker.scxA_v3.txt";
            }
        }

    }    
}
