/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package ai.asymmetric.PGS;

import ai.RandomBiasedAI;
import ai.abstraction.combat.Cluster;
import ai.abstraction.combat.KitterDPS;
import ai.abstraction.combat.NOKDPS;
import ai.abstraction.partialobservability.POHeavyRush;
import ai.abstraction.partialobservability.POLightRush;
import ai.abstraction.partialobservability.PORangedRush;
import ai.abstraction.partialobservability.POWorkerRush;
import ai.abstraction.pathfinding.AStarPathFinding;
import ai.abstraction.pathfinding.PathFinding;
import ai.asymmetric.common.UnitScriptData;
import ai.configurablescript.BasicExpandedConfigurableScript;
import ai.configurablescript.ScriptsCreator;
import ai.core.AI;
import ai.core.AIWithComputationBudget;
import ai.core.InterruptibleAI;
import ai.core.ParameterSpecification;
import ai.evaluation.EvaluationFunction;
import ai.evaluation.LTD2;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.SortedSet;
import java.util.TreeSet;
import rts.GameState;
import rts.PlayerAction;
import rts.UnitAction;
import rts.units.Unit;
import rts.units.UnitTypeTable;
import util.Pair;

/**
 *
 * @author rubens
 */
public class POEmRTS extends AIWithComputationBudget implements InterruptibleAI {

    int LOOKAHEAD = 200;
    int I = 1;  // number of iterations for improving a given player
    int R = 0;  // number of times to improve with respect to the response fo the other player
    EvaluationFunction evaluation = null;
    List<AI> scripts = null;
    UnitTypeTable utt;
    PathFinding pf;
    int _startTime;

    AI defaultScript = null;

    long start_time = 0;
    int nplayouts = 0;

    GameState gs_to_start_from = null;
    int playerForThisComputation;
    
    ArrayList<Individuo> pop;
    int tamPop = 0;
    double mutate = 0;
    double crossover = 0;
    int elite = 2;
    int rodada = 3;
    
    AI randAI = null;
    HashMap<String, PlayerAction> cache;
    
    int qtdSumPlayout = 2;

    public POEmRTS(UnitTypeTable utt) {
        this(100, -1, 200, 1, 2,
                new LTD2(),
                //new SimpleSqrtEvaluationFunction2(),
                //new LanchesterEvaluationFunction(),
                utt,
                new AStarPathFinding());
    }

    public POEmRTS(int time, int max_playouts, int la, int a_I, int a_R, EvaluationFunction e, UnitTypeTable a_utt, PathFinding a_pf) {
        super(time, max_playouts);

        LOOKAHEAD = la;
        I = a_I;
        R = a_R;
        evaluation = e;
        utt = a_utt;
        pf = a_pf;
        defaultScript = new POLightRush(a_utt);
        scripts = new ArrayList<>();
        
        pop = new ArrayList();
        tamPop = 10;
        mutate = 0.1;
        crossover = 0.2;
        
        randAI = new RandomBiasedAI(a_utt);
        
        buildPortfolio();
    }

    protected void buildPortfolio() {
        this.scripts.add(new POWorkerRush(utt));
        this.scripts.add(new POLightRush(utt));
        this.scripts.add(new POHeavyRush(utt));
        this.scripts.add(new PORangedRush(utt));
        //this.scripts.add(new NOKDPS(utt));
        //this.scripts.add(new KitterDPS(utt));
        //this.scripts.add(new Cluster(utt));
        
        //this.scripts.add(new EconomyMilitaryRush(utt));
        
        //this.scripts.add(new POHeavyRush(utt, new FloodFillPathFinding()));
        //this.scripts.add(new POLightRush(utt, new FloodFillPathFinding()));
        //this.scripts.add(new PORangedRush(utt, new FloodFillPathFinding()));
    }

    @Override
    public void reset() {

    }
    
    protected void evalPortfolio(int heightMap){
        if(heightMap <= 16 && !portfolioHasWorkerRush()){
            //this.scripts.add(new POWorkerRush(utt));
        }
    }

    @Override
    public PlayerAction getAction(int player, GameState gs) throws Exception {
        if (gs.canExecuteAnyAction(player)) {
            
            if(rodada < 3){
                Individuo ret = (Individuo) pop.get(rodada);
                rodada++;
                return getFinalAction(ret.getGen());
            } else{
                rodada = 1;
                pop = new ArrayList();
                startNewComputation(player, gs);
                return getBestActionSoFar();
            }
            //System.out.println("issooooooooooooo");
            
            
        } else {
            //System.out.println("?????????????");
            return new PlayerAction();
        }

    }

    @Override
    public PlayerAction getBestActionSoFar() throws Exception {
        getCache();
        UnitScriptData InitBest = new UnitScriptData(playerForThisComputation);
        InitBest.setSeedUnits(defaultScript);
        setAllScripts(playerForThisComputation, InitBest, defaultScript);
        initialize(playerForThisComputation, InitBest);
        if( (System.currentTimeMillis()-start_time ) < TIME_BUDGET){
            InitBest = evolution(playerForThisComputation);
        }
        writeUsingFileWriter("");
        return getFinalAction(InitBest);
    }
    
    private void getCache() throws Exception {
        for (AI script : scripts) {
            cache.put(script.toString(), script.getAction(playerForThisComputation, gs_to_start_from));
        }
    }
    
    protected void initialize(int player, UnitScriptData currentScriptData) throws Exception{
        //Random rand = new Random();
        AI enemyAI = new POLightRush(utt);
        //double fitness = eval(player, gs_to_start_from, currentScriptData, enemyAI);
        //Individuo init = new Individuo(currentScriptData, fitness);
        for(int i = pop.size(); i<tamPop; i++){
            //int script = rand.nextInt(scripts.size());
            //currentScriptData.setSeedUnits(scripts.get(script));
            setAllScripts(playerForThisComputation, currentScriptData, defaultScript);
            double fitness = eval(player, gs_to_start_from, currentScriptData, enemyAI);
            /*double sum = 0.0;
            for (int j = 0; j < qtdSumPlayout; j++) {
                sum += eval(player, gs_to_start_from, currentScriptData, new POLightRush(utt));
            }
            double fitness = sum / qtdSumPlayout;*/
            Individuo init = new Individuo(currentScriptData, fitness);
            pop.add(init);
        }
        pop.sort((o1, o2) -> o1.getFitness().compareTo(o2.getFitness()));
     }
    
    protected ArrayList<Individuo> mutateSemCross(int player) throws Exception{
        ArrayList<Unit> unitsPlayer = getUnitsPlayer(player);
        Random rand = new Random();
        ArrayList<Individuo> filhos = new ArrayList();
        //System.out.println("Rand: " + rand.nextDouble());
        
        for(int i = 0; i<tamPop; i++){
            double vai = rand.nextDouble();
            //System.out.println("vai: " + vai);
            if(vai < mutate){
                //System.out.println("foi");
                AI enemyAI = new POLightRush(utt);
                int unidade = rand.nextInt(unitsPlayer.size());
                int script = rand.nextInt(scripts.size());
                //System.out.println("script: " + script);
                UnitScriptData u = pop.get(i).getGen();
                u.setUnitScript(unitsPlayer.get(unidade), scripts.get(script));
                
                double fitness = eval(player, gs_to_start_from, u, enemyAI);//eval PGS
                //double fitness = eval(player, gs_to_start_from, u, scripts.get(rand.nextInt(scripts.size())));//eval PGS

                /*double sum = 0.0;
                for (int j = 0; j < qtdSumPlayout; j++) {
                    sum += eval(player, gs_to_start_from, u, new POLightRush(utt));
                    //sum += eval(player, gs_to_start_from, u, scripts.get(rand.nextInt(scripts.size())));
                }
                double fitness = sum / qtdSumPlayout;*/
                Individuo init = new Individuo(u, fitness);
                filhos.add(init);
                //pop.get(i).setGen(u);
            }
        }
        return filhos;
    }
    
    protected ArrayList<Individuo> crossover(int player) throws Exception{
        Random rand = new Random();
        ArrayList<Individuo> filhos = new ArrayList();
        //ArrayList<Unit> unitsPlayer = getUnitsPlayer(player);
        int tam = pop.size();
        for(int i = 0; i<tam; i++){
            double vai = rand.nextDouble();
            if(vai < crossover){
                int pos = rand.nextInt(tam);
                if(pos != i){
                    ArrayList<Unit> unidades1 = new ArrayList(pop.get(i).getGen().getUnits());
                    ArrayList<Unit> unidades2 = new ArrayList(pop.get(pos).getGen().getUnits());
                    int corte = rand.nextInt(unidades1.size());
                    UnitScriptData aux = new UnitScriptData(playerForThisComputation);
                    UnitScriptData aux2 = new UnitScriptData(playerForThisComputation);
                    aux.setSeedUnits(defaultScript);
                    aux2.setSeedUnits(defaultScript);
                    aux.reset();
                    //primeiro filho
                    for(int uni1 = 0; uni1 < corte; uni1++){
                        aux.setUnitScript(unidades1.get(uni1), pop.get(i).getGen().getAIUnit(unidades1.get(uni1)));
                    }
                    for(int uni2 = corte; uni2 < unidades2.size(); uni2++){
                        aux.setUnitScript(unidades2.get(uni2), pop.get(i).getGen().getAIUnit(unidades2.get(uni2)));
                    }
                    //segundo filho
                    for(int uni1 = corte; uni1 < unidades1.size(); uni1++){
                        aux2.setUnitScript(unidades1.get(uni1), pop.get(i).getGen().getAIUnit(unidades1.get(uni1)));
                    }
                    for(int uni2 = 0; uni2 < corte; uni2++){
                        aux2.setUnitScript(unidades1.get(uni2), pop.get(i).getGen().getAIUnit(unidades1.get(uni2)));
                    }
                    //System.out.println("unidades1.size():" + unidades1.size());
                    
                    //double fitness = eval(player, gs_to_start_from, aux, scripts.get(rand.nextInt(scripts.size())));//eval PGS
                    //double fitness2 = eval(player, gs_to_start_from, aux2, scripts.get(rand.nextInt(scripts.size())));//eval PGS
                    
                    double fitness = eval(player, gs_to_start_from, aux, new POLightRush(utt));//eval PGS
                    double fitness2 = eval(player, gs_to_start_from, aux2, new POLightRush(utt));//eval PGS
                    /*
                    double sum = 0.0;
                    double sum2 = 0.0;
                    for (int j = 0; j < qtdSumPlayout; j++) {
                        sum += eval(player, gs_to_start_from, aux, new POLightRush(utt));
                        sum2 += eval(player, gs_to_start_from, aux2, new POLightRush(utt));
                    }
                    double fitness = sum / qtdSumPlayout;
                    double fitness2 = sum2 / qtdSumPlayout;*/
                    
                    filhos.add(new Individuo(aux, fitness));
                    filhos.add(new Individuo(aux2, fitness2));
                }
            }
        }
        return filhos;
    }
    
    private void writeUsingFileWriter(String data) {
        String arq = "C:\\Users\\Ateles Junior\\Documents\\2018-2\\TCC\\MicroRTS\\logTeste.txt";
        //File file = new File(arq);
        //FileWriter fr = null;
        
        try {
            //fr = new FileWriter(file);
            //fr.write(data);
            
            //Files.write(Paths.get("C:\\Users\\Ateles Junior\\Documents\\2018-2\\TCC\\MicroRTS\\logTeste.txt"), (data + "\n").getBytes(UTF_8),StandardOpenOption.CREATE,StandardOpenOption.APPEND);
            //Files.write(Paths.get("C:\\Users\\Ateles Junior\\Documents\\2018-2\\TCC\\MicroRTS\\logTeste.txt"), (data + System.lineSeparator()).getBytes(UTF_8),StandardOpenOption.CREATE,StandardOpenOption.APPEND);
            
            PrintWriter out = new PrintWriter(new BufferedWriter(new FileWriter(arq, true)));
            if(data == "aaaaaaaaa"){
                for (Individuo pop1 : pop) {
                    for(Unit u : pop1.getGen().getUnits()){
                        out.write(u.toString() + ": " + pop1.getGen().getAIUnit(u).toString() + " / ");
                    }
                    out.println();
                }
                out.println("aaaa");
            } else {
                out.println();
                out.println("fim da geração");
                out.println();
            }
            
            out.flush();
            out.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    
    protected void selectSemElit(ArrayList<Individuo> filhos) throws Exception{
        pop.addAll(filhos);
        pop.sort((o1, o2) -> o1.getFitness().compareTo(o2.getFitness()));
        int tam = pop.size();
        if(tam > tamPop){
            tam--;
            while(tam >= tamPop){
                pop.remove(tam);
                tam--;
            }
        }
        //writeUsingFileWriter("aaaaaaaaa");
    }
    
    protected void selectComElit(ArrayList<Individuo> filhos) throws Exception{
        pop.sort((o1, o2) -> o1.getFitness().compareTo(o2.getFitness()));
        filhos.sort((o1, o2) -> o1.getFitness().compareTo(o2.getFitness()));
        int i = elite;
        int j = 0;
        int tamFilhos = filhos.size();
        while(i < tamPop && j < tamFilhos){
            pop.set(i, filhos.get(j));
            i++; j++;
        }
        pop.sort((o1, o2) -> o1.getFitness().compareTo(o2.getFitness()));
        //writeUsingFileWriter("aaaaaaaaa");
    }
    
    private UnitScriptData evolution(int player) throws Exception{
        Random rand = new Random();
        
        while(System.currentTimeMillis() < (start_time + (TIME_BUDGET - 10))){
            ArrayList<Individuo> filhos = new ArrayList();
            //selectSemElit(mutateSemCross(player));//Mutacao sem elitismo
            if (System.currentTimeMillis() >= (start_time + (TIME_BUDGET - 10))) {
                    Individuo ret = (Individuo) pop.get(0);
                    return ret.getGen();
                }
            filhos.addAll(mutateSemCross(player));
            if (System.currentTimeMillis() >= (start_time + (TIME_BUDGET - 10))) {
                    Individuo ret = (Individuo) pop.get(0);
                    return ret.getGen();
                }
            filhos.addAll(crossover(player));
            if (System.currentTimeMillis() >= (start_time + (TIME_BUDGET - 10))) {
                    Individuo ret = (Individuo) pop.get(0);
                    return ret.getGen();
                }
            //selectComElit(filhos);//Mutacao com elitismo
            selectSemElit(mutateSemCross(player));//Mutacao sem elitismo

        }
        
        Individuo ret = (Individuo) pop.get(0);
        return ret.getGen();
    }
    
    private void setAllScripts(int playerForThisComputation, UnitScriptData currentScriptData, AI seedPlayer) {
        Random rand = new Random();
        currentScriptData.reset();
        for (Unit u : gs_to_start_from.getUnits()) {
            if (u.getPlayer() == playerForThisComputation) {
                //currentScriptData.setUnitScript(u, seedPlayer);
                currentScriptData.setUnitScript(u, scripts.get(rand.nextInt(scripts.size())));
            }
        }
    }

    /*protected AI getSeedPlayer(int player) throws Exception {
        AI seed = null;
        double bestEval = -9999;
        AI enemyAI = new POLightRush(utt);
        //vou iterar para todos os scripts do portfolio
        for (AI script : scripts) {
            double tEval = eval(player, gs_to_start_from, script, enemyAI);
            if (tEval > bestEval) {
                bestEval = tEval;
                seed = script;
            }
        }

        return seed;
    }*/

    /*
    * Executa um playout de tamanho igual ao @LOOKAHEAD e retorna o valor
     */
    public double eval(int player, GameState gs, AI aiPlayer, AI aiEnemy) throws Exception {
        AI ai1 = aiPlayer.clone();
        AI ai2 = aiEnemy.clone();

        GameState gs2 = gs.clone();
        ai1.reset();
        ai2.reset();
        int timeLimit = gs2.getTime() + LOOKAHEAD;
        boolean gameover = false;
        while (!gameover && gs2.getTime() < timeLimit) {
            if (gs2.isComplete()) {
                gameover = gs2.cycle();
            } else {
                gs2.issue(ai1.getAction(player, gs2));
                gs2.issue(ai2.getAction(1 - player, gs2));
            }
        }
        double e = evaluation.evaluate(player, 1 - player, gs2);

        return e;
    }

    /**
     * Realiza um playout (Dave playout) para calcular o improve baseado nos
     * scripts existentes.
     *
     * @param player
     * @param gs
     * @param uScriptPlayer
     * @param aiEnemy
     * @return a avaliação para ser utilizada como base.
     * @throws Exception
     */
    public double eval(int player, GameState gs, UnitScriptData uScriptPlayer, AI aiEnemy) throws Exception {
        //AI ai1 = defaultScript.clone();
        AI ai2 = aiEnemy.clone();

        GameState gs2 = gs.clone();
        //ai1.reset();
        ai2.reset();
        int timeLimit = gs2.getTime() + LOOKAHEAD/10;
        boolean gameover = false;
        while (!gameover && gs2.getTime() < timeLimit) {
            if (gs2.isComplete()) {
                gameover = gs2.cycle();
            } else {
                //gs2.issue(ai1.getAction(player, gs2));
                gs2.issue(uScriptPlayer.getAction(player, gs2));
                //
                gs2.issue(ai2.getAction(1 - player, gs2));
            }
        }

        return evaluation.evaluate(player, 1 - player, gs2);
    }
    //Usando aleatoriedade
    public double eval1(int player, GameState gs, UnitScriptData uScriptPlayer, AI aiEnemy) throws Exception {
        //AI ai1 = defaultScript.clone();
        AI ai2 = aiEnemy.clone();
        ai2.reset();
        GameState gs2 = gs.clone();
        //actions default
        //gs2.issue(uScriptPlayer.getAction(player, gs2));
        //gs2.issue(ai2.getAction(1 - player, gs2));
        gs2.issue(getActionsUScript(player, uScriptPlayer, gs2));
        gs2.issue(ai2.getAction(1 - player, gs2));
        int timeLimit = gs2.getTime() + LOOKAHEAD / 10;
        boolean gameover = false;
        while (!gameover && gs2.getTime() < timeLimit) {
            if (gs2.isComplete()) {
                gameover = gs2.cycle();
            } else {
                gs2.issue(randAI.getAction(player, gs2));
                gs2.issue(randAI.getAction(1 - player, gs2));
            }
        }

        return evaluation.evaluate(player, 1 - player, gs2);
    }
    
    private PlayerAction getActionsUScript(int player, UnitScriptData uScriptPlayer, GameState gs2) {
        PlayerAction temp = new PlayerAction();
        for (Unit u : gs2.getUnits()) {
            if (u.getPlayer() == player) {
                String sAI = uScriptPlayer.getAIUnit(u).toString();

                UnitAction uAt = getUnitAction(u, cache.get(sAI));
                if(uAt != null){
                    temp.addUnitAction(u, uAt);
                }
            }
        }

        return temp;
    }
    
    private UnitAction getUnitAction(Unit u, PlayerAction get) {
        for (Pair<Unit, UnitAction> tmp : get.getActions()) {
            if (tmp.m_a.getID() == u.getID()) {
                return tmp.m_b;
            }
        }
        return null;
    }

    @Override
    public AI clone() {
        return new PGSmRTS(TIME_BUDGET, ITERATIONS_BUDGET, LOOKAHEAD, I, R, evaluation, utt, pf);
    }

    @Override
    public List<ParameterSpecification> getParameters() {
        List<ParameterSpecification> parameters = new ArrayList<>();

        parameters.add(new ParameterSpecification("TimeBudget", int.class, 100));
        parameters.add(new ParameterSpecification("IterationsBudget", int.class, -1));
        parameters.add(new ParameterSpecification("PlayoutLookahead", int.class, 100));
        parameters.add(new ParameterSpecification("I", int.class, 1));
        parameters.add(new ParameterSpecification("R", int.class, 1));
        parameters.add(new ParameterSpecification("EvaluationFunction", EvaluationFunction.class, new LTD2()));
        parameters.add(new ParameterSpecification("PathFinding", PathFinding.class, new AStarPathFinding()));

        return parameters;
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "(" + TIME_BUDGET + ", " + ITERATIONS_BUDGET + ", " + LOOKAHEAD + ", " + I + ", " + R + ", " + evaluation + ", " + pf + ")";
    }

    public int getPlayoutLookahead() {
        return LOOKAHEAD;
    }

    public void setPlayoutLookahead(int a_pola) {
        LOOKAHEAD = a_pola;
    }

    public int getI() {
        return I;
    }

    public void setI(int a) {
        I = a;
    }

    public int getR() {
        return R;
    }

    public void setR(int a) {
        R = a;
    }

    public EvaluationFunction getEvaluationFunction() {
        return evaluation;
    }

    public void setEvaluationFunction(EvaluationFunction a_ef) {
        evaluation = a_ef;
    }

    public PathFinding getPathFinding() {
        return pf;
    }

    public void setPathFinding(PathFinding a_pf) {
        pf = a_pf;
    }

    @Override
    public void startNewComputation(int player, GameState gs) throws Exception {
        playerForThisComputation = player;
        gs_to_start_from = gs;
        nplayouts = 0;
        _startTime = gs.getTime();
        start_time = System.currentTimeMillis();
        this.cache = new HashMap<>();
    }

    @Override
    public void computeDuringOneGameFrame() throws Exception {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }

    private UnitScriptData doPortfolioSearch(int player, UnitScriptData currentScriptData, AI seedEnemy) throws Exception {
        int enemy = 1 - player;

        UnitScriptData bestScriptData = currentScriptData.clone();
        double bestScore = eval(player, gs_to_start_from, bestScriptData, seedEnemy);
        ArrayList<Unit> unitsPlayer = getUnitsPlayer(player);
        //controle pelo número de iterações
        //for (int i = 0; i < I; i++) {
        while(System.currentTimeMillis() < (start_time + (TIME_BUDGET - 10))){
            //fazer o improve de cada unidade
            for (Unit unit : unitsPlayer) {
                //inserir controle de tempo
                if (System.currentTimeMillis() >= (start_time + (TIME_BUDGET - 10))) {
                    return currentScriptData;
                }
                //iterar sobre cada script do portfolio
                for (AI ai : scripts) {
                    currentScriptData.setUnitScript(unit, ai);
                    double scoreTemp = eval(player, gs_to_start_from, currentScriptData, seedEnemy);

                    if (scoreTemp > bestScore) {
                        bestScriptData = currentScriptData.clone();
                        bestScore = scoreTemp;
                    }
                    if( (System.currentTimeMillis()-start_time ) > (TIME_BUDGET-5)){
                        return bestScriptData.clone();
                    }
                }
                //seto o melhor vetor para ser usado em futuras simulações
                currentScriptData = bestScriptData.clone();
            }
        }
        return currentScriptData;
    }

    private ArrayList<Unit> getUnitsPlayer(int player) {
        ArrayList<Unit> unitsPlayer = new ArrayList<>();
        for (Unit u : gs_to_start_from.getUnits()) {
            if (u.getPlayer() == player) {
                unitsPlayer.add(u);
            }
        }

        return unitsPlayer;
    }

    private PlayerAction getFinalAction(UnitScriptData currentScriptData) throws Exception {
        PlayerAction pAction = new PlayerAction();
        HashMap<String, PlayerAction> actions = new HashMap<>();
        for (AI script : scripts) {
            actions.put(script.toString(), script.getAction(playerForThisComputation, gs_to_start_from));
        }
        for (Unit u : currentScriptData.getUnits()) {
            AI ai = currentScriptData.getAIUnit(u);

            UnitAction unt = actions.get(ai.toString()).getAction(u);
            if (unt != null) {
                pAction.addUnitAction(u, unt);
            }
        }
        
        
        //writeUsingFileWriter("aaaaaaaaa");
        return pAction;
    }

    private boolean portfolioHasWorkerRush() {
        for (AI script : scripts) {
            if(script.toString().contains("POWorkerRush")){
                return true;
            }
        }
        return false;
    }

}
