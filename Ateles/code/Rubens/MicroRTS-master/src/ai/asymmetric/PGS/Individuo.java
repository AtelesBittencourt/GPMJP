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
    Double fitness = 0.0;
    
    public Individuo(){
    }
    
    public Individuo(UnitScriptData g, Double fit){
        this.gen = g.clone();
        this.fitness = fit;
    }
    
    public Double getFitness(){
        return this.fitness;
    }
    
    public UnitScriptData getGen(){
        return this.gen.clone();
    }
    
    public void setFitness(double fit){
        this.fitness = fit;
    }
    
    public void setGen(UnitScriptData g){
        this.gen = null;
        this.gen = g.clone();
    }
}
