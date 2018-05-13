package app;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

public class Simulacion {


    /////////////GLOBAL VAR////////////////

    private static Boolean bc_available = true;
    private static Integer N = 0;
    private static Integer M = 0;
    private static Integer I = 0;
    private static Integer HV = -1;
    private static Double T = 0D;
    private static Integer TF;
    private static Double TPT = 0D;
    private static Double[] NTLineMax;
    private static Double[] NTLineMin;
    private static Integer[] NTLine;
    private static Double[] TPS;
    private static Integer[] queues;
    private static Double[] PERCENTAGE;
    private static Double[] STA;
    private static Double[] STS;
    private static Double[] STLL;
    private static Double[] WAITINGTIME;
    private static Double[] TIMEFREE;
    private static Double[] ITF;
    private static Integer NA = 864000/Integer.valueOf(String.valueOf(dailyArrival(random())).substring(0,6));
    private static final Logger LOGGER = LoggerFactory.getLogger(Simulacion.class);
    private static BufferedWriter writer;

    public static void main (String[] args) throws IOException {

        System.out.println("Enter number of simulations to run: ");

        writer = new BufferedWriter(new FileWriter("BitcoinStats.xls"));

        Scanner amountOfSimulations = new Scanner(System.in);
        M = Integer.valueOf(amountOfSimulations.nextLine());

        while (I < M) {
            T = 0D;
            System.out.println("Enter number of queues: ");
            Scanner inputLines = new Scanner(System.in);

            N = Integer.valueOf(inputLines.nextLine());

            TPS = new Double[N];
            queues = new Integer[N];
            STA = new Double[N];
            STS = new Double[N];
            STLL = new Double[N];
            WAITINGTIME = new Double[N];
            PERCENTAGE = new Double[N];
            NTLine = new Integer[N];
            NTLineMin = new Double[N];
            NTLineMax = new Double[N];
            TIMEFREE  = new Double[N];
            ITF       = new Double[N];


            initializeDouble(TPS, -1D);
            initialize(queues, 0);
            initialize(NTLine, 0);
            initializeDouble(PERCENTAGE, 0D);
            initializeDouble(STA, 0D);
            initializeDouble(STS, 0D);
            initializeDouble(STLL, 0D);
            initializeDouble(WAITINGTIME, 0D);
            initializeDouble(TIMEFREE,0D);
            initializeDouble(ITF,0D);
            initializeNTLineMax();


            System.out.println("Enter final time (minutes): ");
            Scanner inputTF = new Scanner(System.in);

            TF = Integer.valueOf(inputTF.nextLine());
            TF = TF * 600;

            while (T < TF) {
                simulation();
            }

            //Getting the system empty

            for (int i = 0; i < queues.length; i++) {
                if (queues[i] != 0) {
                    TPT = -1D;
                    empty();
                }
            }

            printAnswer();
            I++;
        }

        writer.close();

    }

    ///////////////////////////////////////////// AUXILIAR METHODS ////////////////////////////////////////////////////////////


    private static void simulation() {
        Double minTps = minTPS();
        Integer minTpsIndex = minTPSIndex(minTps);

        if ((TPT >= minTps) && (TPT == -1D) && (minTps >= 0)) {
            processExit(minTps, minTpsIndex);
        } else {
            processArrival();
        }
    }



    private static void empty() {

        Double minTps = minTPS();

        T = T + minTps;

        for(int x = 0; x< queues.length; x++){
            while(queues[x]>0){
                queues[x] = queues[x] - 1;
                processEmptyingExit(x);
                NTLine[x] = NTLine[x] + 1;
                STS[x] = STS[x] + T;
            }
        }
    }

    ////////////////////////////////////////// ARRIVAL AND EXITS /////////////////////////////////////////////////////////////

    private static void processArrival() {

        T = TPT;
        TPT = T+NA ;

        Integer queuePosition = queuePosition();

        if(queuePosition >= N) {
            queuePosition = 0;
        }

        queues[queuePosition] = queues[queuePosition] + 1;

        Boolean previousEmpty = arePreviousEmpty(queuePosition);

        if (queues[queuePosition] == 1 && bc_available) {
            if (previousEmpty) {
                bc_available = false;
                makeNextHV(queuePosition);
                Double TA = attentionTime(queuePosition);
                TPS[queuePosition] = T + TA;
                STA[queuePosition] = STA[queuePosition] + TA;
                STLL[queuePosition] = STLL[queuePosition] + TPT;
            } else {
                STLL[queuePosition] = STLL[queuePosition] + TPT;
            }
        } else {
            STLL[queuePosition] = STLL[queuePosition] + T;
        }

        if (queues[queuePosition]== 1){
            TIMEFREE[queuePosition]+= (T-ITF[queuePosition]);
        }

    }

    private static void processExit(Double minTps, Integer minTpsIndex) {
        T = T + minTps;
        queues[minTpsIndex] = queues[minTpsIndex] - 1;

        if(queues[minTpsIndex] >= 1) {
            Double TA =attentionTime(minTpsIndex);
            TPS[minTpsIndex] = T + TA;
            STA[minTpsIndex] = STA[minTpsIndex] + TA;

        } else {
            if (queues[minTpsIndex]>0){
                ITF[minTpsIndex]=T;
            }
            nextExitToProcess(minTpsIndex);
        }

        NTLine[minTpsIndex] = NTLine[minTpsIndex] + 1;
        STS[minTpsIndex] = STS[minTpsIndex] + T;
        bc_available = true;
    }


    private static void processEmptyingExit(Integer index) {

        if(queues[index] >= 1) {
            Double TA =attentionTime(index);
            T = T+TA;
            TPS[index] = T + TA;
            STA[index] = STA[index] + TA;
        } else {
            nextExitToProcess(index);
        }
    }

    private static void nextExitToProcess(Integer minTpsIndex) {

        boolean queueNotEmpty = true;
        TPS[minTpsIndex] = -1D;

        for (int x = 0; x < queues.length && queueNotEmpty; x++){
            if (queues[x]>=0){
                Double TA = attentionTime(x);
                TPS[x] = T + TA;
                STA[x] = STA[x] + TA;
                queueNotEmpty = false;

            }
        }
    }

    //////////////////////////////////////////// INIT METHODS ///////////////////////////////////////////////////////////////

    private static void initialize(Integer[] lista,int valorInicial){
        for (int i = 0; i < lista.length; i++){
            lista[i] = valorInicial;
        }
    }

    private static void initializeDouble(Double[] lista, Double valorInicial){
        for (int i = 0; i< lista .length; i++){
            lista[i] = valorInicial;
        }
    }

    private static boolean arePreviousEmpty(Integer index) {
        for(Integer i = index-1 ; i >= 0 ; i--) {
            if(!queues[i].equals(0)) return false;
        }
        return true;
    }

    private static void makeNextHV(Integer index) {
        for(Integer i = index + 1; i < queues.length; i++) {
            queues[i] = HV;
        }
    }

    private static Double minTPS() {

        List<Double> filterList = Arrays.asList(TPS).stream().filter(n -> n != -1).collect(Collectors.toList());
        if(!(filterList).isEmpty()) {
            return Collections.min(filterList);
        }
        return -1D;
    }

    public static Integer minTPSIndex (Double minimoValor) {

        int index = -1;

        List<Double> filterList = Arrays.asList(TPS).stream().filter(n -> n != -1).collect(Collectors.toList());

        if(!(filterList).isEmpty()) {
            for (int i = 0; (i < TPS.length) && (index == -1); i++) {
                if (TPS[i] == minimoValor) {
                    index = i;
                }
            }
        } else {
            index = 0;
        }

        return index ;
    }

    public static void printAnswer() throws IOException {

        Integer NTLineTotal = Arrays.asList(NTLine).stream().mapToInt(Integer::intValue).sum();

        // Calculate the results, average of waiting team for each line
        for (int i = 0; i < queues.length; i++)
            if(NTLine[i]!=0) {
                WAITINGTIME[i] = (STS[i] - STLL[i] - STA[i]) / (NTLine[i]*600);
            }else{
                WAITINGTIME[i] = 0D;
            }

        writer.write("i "+"\t"+"WAITINGTIME[i]"+"\t"+"PERCENTAGE OF TRANSACTIONS[i]"+"\t"+"PERCENTAGE OF TIME FREE[i]"+"\n");
        // Calculate the results, percentage of processed transactions in that line on total transactions.
        for (int i = 0; i < queues.length; i++) {
            if (NTLineTotal != 0){
                PERCENTAGE[i] = (Double.valueOf(NTLine[i])*100) / NTLineTotal;
                PERCENTAGE[i] = PERCENTAGE[i] * 100 / 100;
            }else{
                PERCENTAGE[i] = null;
            }
            writer.write((i+1)+"\t"+String.valueOf(WAITINGTIME[i])+"\t"+PERCENTAGE[i]+"\t"+TIMEFREE[i]/T +"\n");
        }
            LOGGER.info("SIMULATION NUMBER: " + I + " | LINES: "+ N + " | FINAL TIME: "+ TF );
        for (int i = 0; i < queues.length; i++) {
            LOGGER.info("Waiting time in the line:" + (i + 1) + " = " + WAITINGTIME[i] + "\n" + "Percentage of transactions in " +
                    "the line " + (i + 1) + " of the total: " +PERCENTAGE[i] + "Percentaje of time Free [i]" + TIMEFREE[i]/T);
        }

    }


    ///////////////////////////// TIME TO PROCESS TRANSACTION SIMULATION ////////////////////////////////////////////////////

    /*
    private static Double attentionTime(int line) {
        Double rangeMax = ;
        Double rangeMin = 0D;
        if (line != 0) {
            rangeMax = 1200D - 600D/line;
        }else{
            rangeMax = 300D;
        }
        return ThreadLocalRandom.current().nextDouble(rangeMin, rangeMax);
    }*/

    private static Double attentionTime(int line){
        Double rangeMax = 600D;
        Double rangeMin = 0D;

        return ThreadLocalRandom.current().nextDouble(rangeMin, rangeMax);
    }


    ///////////////////////////// ARRIVALS TO QUEUES SIMULATION /////////////////////////////////////////////////////////////

    private static double dailyArrival(double r) {
        double sigma = 106540;
        double mu = 128800;

        if(r > 0.9968724239) {
            dailyArrival(random());
        }

        return sigma * Math.sqrt(-2 * Math.log(1-r)) + mu;
    }

    private static double random() {

        int rangeMin = 0;
        int rangeMax = 1;

        return ThreadLocalRandom.current().nextDouble(rangeMin, rangeMax);

    }

    ///////////////////////////// FEES AND QUEUES DISTRIBUTION SIMULATION ///////////////////////////////////////////////////


    private static Integer queuePosition(){
        Double probability =  ThreadLocalRandom.current().nextDouble(0, 1);

        return (queueDistribution(bitcoinDistribution(probability*100)));

    }

    private static Double bitcoinDistribution(Double random){

        if (random <= 49.01126709){
            return ThreadLocalRandom.current().nextDouble(0, 1);
        }else if(random <= (49.01126709 + 22.85238066)){
            return ThreadLocalRandom.current().nextDouble(0.5, 2);
        }else if (random <=  (49.01126709 + 22.85238066 + 17.23819381)){
            return ThreadLocalRandom.current().nextDouble(1, 3);
        }else if (random <= (49.01126709 + 22.85238066 + 17.23819381 + 7.686242931 )){
            return ThreadLocalRandom.current().nextDouble(1.5, 5);
        }else if (random <=( 49.01126709 + 22.85238066 + 17.23819381 + 7.686242931 + 2.535713917)){
            return ThreadLocalRandom.current().nextDouble(2, 5);
        }else if(random <=( 49.01126709 + 22.85238066 + 17.23819381 + 7.686242931 + 2.535713917+ 0.5973369618)){
            return ThreadLocalRandom.current().nextDouble(2.5, 5);
        }else if(random <= ( 49.01126709 + 22.85238066 + 17.23819381 + 7.686242931 + 2.535713917+ 0.5973369618 + 0.07137256447)){
            return ThreadLocalRandom.current().nextDouble(3, 5);
        }else if(random <= (49.01126709 + 22.85238066 + 17.23819381 + 7.686242931 + 2.535713917+ 0.5973369618 + 0.07137256447+0.006991690987)){
            return ThreadLocalRandom.current().nextDouble(3.5, 5);
        }else if(random <= (49.01126709 + 22.85238066 + 17.23819381 + 7.686242931 + 2.535713917+ 0.5973369618 + 0.07137256447+0.006991690987+ 0.0004867345059)) {
            return ThreadLocalRandom.current().nextDouble(4, 5);
        }else{
            return ( ThreadLocalRandom.current().nextDouble(4.5, 5));
        }

    }

    private static int queueDistribution(Double value){

        int j=0;
        for (int i = 0;i<N;i++){
            if(NTLineMin[i] < value && value < NTLineMax[i]) {
                j = i;
            }

        }
        return j;
    }

    private static void initializeNTLineMax(){


        Double x = 5D /N;

        for (int i = 0; i < NTLineMin.length; i++){
            NTLineMax[i] = 5D - x*i;
        }
        initializeNTLineMin();

    }

    private static void initializeNTLineMin(){
        int i;
        Double x = 5D /N;
        for (i = 0; i < NTLineMin.length; i++) {
            NTLineMin[i] = 5D -  x * (i+1);
        }
    }
}
