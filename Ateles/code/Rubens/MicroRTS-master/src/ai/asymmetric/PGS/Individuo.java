/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.asymmetric.PGS;

import ai.asymmetric.common.UnitScriptData;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author Ateles Junior
 */
public class Individuo {
    UnitScriptData gen = null;
    double fitness = 0;
    
    public Individuo(){
    }
    
    public Individuo(UnitScriptData g, double fit){
        gen = g.clone();
        fitness = fit;
    }
    
    public double getFitness(){
        return fitness;
    }
    
    public UnitScriptData getGen(){
        return gen.clone();
    }
    
    public void setFitness(double fit){
        this.fitness = fit;
    }
}
