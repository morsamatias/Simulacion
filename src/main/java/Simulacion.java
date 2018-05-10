import javax.xml.crypto.dom.DOMCryptoContext;
import java.io.BufferedWriter;
import java.io.CharArrayReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.Period;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Simulacion {


    /////////////GLOBAL VAR////////////////

    private static Integer N = 0;
    private static Integer M = 0;
    private static Integer I = 0;
    private static Integer HV = -1;
    private static Double T = 0.00;
    private static Integer TF;
    private static Double TPT = 0.00;
    private static Double[] NTLineMax;
    private static Double[] NTLineMin;
    private static Integer[] NTLine;
    private static Double[] TPS;
    private static Integer[] lines;
    private static Double[] PERCENTAGE;
    private static Double[] STA;
    private static Double[] STS;
    private static Double[] STLL;
    private static Double[] WAITINGTIME;
    private static BufferedWriter writer;
    private static Integer NA = 864000/Integer.valueOf(String.valueOf(dailyArrival(random())).substring(0,6));






    public static void main (String[] args) throws IOException {

        writer = new BufferedWriter(new FileWriter("/home/matias/Escritorio/BitcoinLinesTest.xls"));
        System.out.println("Ingrese cantidad de simulaciones que va a realizar: ");
        Scanner amountOfSimulations = new Scanner(System.in);
        M = Integer.valueOf(amountOfSimulations.nextLine());

        while (I <= M) {
            T = 0.00;
            System.out.println("Ingrese cantidad de colas: ");
            Scanner inputLines = new Scanner(System.in);

            N = Integer.valueOf(inputLines.nextLine());

            TPS = new Double[N];
            lines = new Integer[N];
            STA = new Double[N];
            STS = new Double[N];
            STLL = new Double[N];
            WAITINGTIME = new Double[N];
            PERCENTAGE = new Double[N];
            NTLine = new Integer[N];
            NTLineMin = new Double[N];
            NTLineMax = new Double[N];

            initializeDouble(TPS, -1.00);
            initialize(lines, 0);
            initialize(NTLine, 0);
            initializeDouble(PERCENTAGE, 0.00);
            initializeDouble(STA, 0.00);
            initializeDouble(STS, 0.00);
            initializeDouble(STLL, 0.00);
            initializeDouble(WAITINGTIME, 0.00);
            initializeNTLineMax();


            System.out.println("Ingrese tiempo final (en decimas de segundo): ");
            Scanner inputTF = new Scanner(System.in);

            TF = Integer.valueOf(inputTF.nextLine());


            while (T < TF) {
                simulation();
            }


            for (int i = 0; i < lines.length; i++) {
                if (!(lines[i] == 0)) {
                    TPT = -1.00;
                    empty();
                }
            }

            printAnswer();
            I++;
        }


        writer.close();

    }




    ///////////////////////////////////////////// FUNCTIONS ////////////////////////////////////////////////////////////




    private static void simulation() {
        Double minTps = minTPS();
        Integer minTpsIndex = minTPSIndex(minTps);

        if (((TPT >= minTps) && (TPT == -1.00)) && (minTps >= 0)) {
            T = T + minTps;
            lines[minTpsIndex] = lines[minTpsIndex] - 1;
            processExit(minTpsIndex);
            NTLine[minTpsIndex] = NTLine[minTpsIndex] + 1;
            STS[minTpsIndex] = STS[minTpsIndex] + T;

        } else {
            T = TPT;
            //Integer NA = 864000/Integer.valueOf(String.valueOf(nextArrival(random())).substring(0,6));

            TPT = T+NA ;
            //Integer linePosition = Integer.valueOf(String.valueOf(linesPosition()).substring(0, 1));
            Integer linePosition = linesPosition();

            if(linePosition >= N) {
                linePosition = 0;
            }

            lines[linePosition] = lines[linePosition] + 1;

            Boolean previousEmpty = arePreviousEmpty(linePosition);

            if (!(lines[linePosition] > 1)) {
                if (previousEmpty) {
                    makeNextHV(linePosition);
                    Double TA =  attentionTime(linePosition);
                    TPS[linePosition] = T + TA;
                    STA[linePosition] = STA[linePosition] + TA;
                    STLL[linePosition] = STLL[linePosition] + TPT;
                } else {
                    STLL[linePosition] = STLL[linePosition] + TPT;
                }
            } else {
                STLL[linePosition] = STLL[linePosition] + T;
            }
        }
    }



    private static void empty() {

        Double minTps = minTPS();
        //Integer minTpsIndex = minTPSIndex(minTps);

        T = T + minTps;

        for(int x=0; x<lines.length; x++){
            while(lines[x]>0){
                lines[x] = lines[x] - 1;
                processExit(x);
                NTLine[x] = NTLine[x] + 1;
                STS[x] = STS[x] + T;
            }
        }



    }

    ////// ATTENTION TIME, WITH THIS METHOD THE LOWER NUMBER HAVE PRIORITY

    private static Double attentionTime(int line) {
        Double rangeMax;
        Double rangeMin = 0.00;
        if (line != 0) {
            rangeMax = 1200.00 - 600.00/line;
        }else{
            rangeMax = 150.00 ;
        }
        return ThreadLocalRandom.current().nextDouble(rangeMin, rangeMax);
    }

    private static void processExit(Integer index) {

        if(lines[index] >= 1) {
            Double TA =attentionTime(index);
            TPS[index] = T + TA;
            STA[index] = STA[index] + TA;

        } else {
            boolean flag = true;
            TPS[index] = -1.00;
            for (int x = 0; x<lines.length && flag; x++){
                if (lines[x]>=0){
                    Double TA = attentionTime(index);
                    TPS[x] = T + TA;
                    STA[x] = STA[x] + TA;
                    flag = false;

                }
            }
        }

    }

    private static boolean arePreviousEmpty(Integer index) {
        for(Integer i = index-1 ; i >= 0 ; i--) {
            if(!lines[i].equals(0)) return false;
        }
        return true;
    }

    private static void makeNextHV(Integer index) {
        for(Integer i = index + 1; i < lines.length; i++) {
            lines[i] = HV;
        }
    }

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

    /*
    private static double linesPosition() {

        Integer rangeMin = 0;
        Integer rangeMax = N;

        if((ThreadLocalRandom.current().nextDouble(rangeMin, rangeMax-1)) > N){
            return 0;
        }


        return ThreadLocalRandom.current().nextDouble(rangeMin, rangeMax);
    }

    */

    private static Integer linesPosition(){
       Double probability =  ThreadLocalRandom.current().nextDouble(0, 1);


       return (lineDistribution(bitcoinDistribution(probability*100)));

    }


    private static Double minTPS() {

        List<Double> filterList = Arrays.asList(TPS).stream().filter(n -> n != -1).collect(Collectors.toList());
        if(!(filterList).isEmpty()) {
            return Collections.min(filterList);
        }
        return -1.00;
    }

    public static int minTPSIndex (Double minimoValor) {

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
        for (int i = 0; i < lines.length; i++)
            if(NTLine[i]!=0) {
                WAITINGTIME[i] = (STS[i] - STLL[i] - STA[i])/ NTLine[i];
            }else{
                WAITINGTIME[i] = 0.00;
            }

        // Calculate the results, percentage of processed transactions in that line on total transactions.
        for (int i = 0; i < lines.length; i++) {
            if (NTLineTotal != 0){
                PERCENTAGE[i] = (Double.valueOf(NTLine[i])*100) / NTLineTotal;
                PERCENTAGE[i] = Math.floor(PERCENTAGE[i] * 100) / 100;
            }else{
                PERCENTAGE[i] = null;
            }
        }
            writer.write("SIMULATION NUMBER" + I + "LINES: "+ N + "TIME: "+ TF );
            writer.write("i "+"\t"+"WAITINGTIME[i]"+"\t"+"PERCENTAGE[i]"+"\n");
        for (int i = 0; i < lines.length; i++) {
            System.out.println("Waiting time in the line:" + (i + 1) + " = " + WAITINGTIME[i] + "\n" + "percentage of transactions in " +
                    "the line " + (i + 1) + " of the total: " +PERCENTAGE[i]);
            writer.write((i+1)+"\t"+String.valueOf(WAITINGTIME[i])+"\t"+PERCENTAGE[i]+"\n");
        }

    }


    public static void initialize(Integer[] lista,int valorInicial){
        for (int i = 0; i < lista.length; i++){
            lista[i] = valorInicial;
        }
    }

    public static void initializeLong(Long[] lista, Integer valorInicial){
        for (int i = 0; i< lista .length; i++){
            lista[i] = Long.valueOf(valorInicial);
        }
    }

    public static void initializeDouble(Double[] lista, Double valorInicial){
        for (int i = 0; i< lista .length; i++){
            lista[i] = Double.valueOf(valorInicial);
        }
    }


    //IN BITS PER BYTE

    public static void initializeNTLineMax(){


        Double x = 5.00 /N;

        for (int i = 0; i < NTLineMin.length; i++){
            NTLineMax[i] = 5.00 - x*i;
        }
        initializeNTLineMin();

    }

    public static void initializeNTLineMin(){
        int i;
        Double x = 5.00 /N;
        for (i = 0; i < NTLineMin.length; i++) {
            NTLineMin[i] = 5.00 -  x * (i+1);
            System.out.println("NTLineMin = " + NTLineMin[i] + " NTLineMax[" + i + "] =  " + NTLineMax[i]);
        }
    }

    public static Double bitcoinDistribution(Double random){

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





    public static int lineDistribution(Double value){
        int j=0;
        for (int i = 0;i<N;i++){
            if(NTLineMin[i] < value && value < NTLineMax[i]) {
                j = i;
            }

        }
        //System.out.println("value =" + value + " j=" + j);
        return j;
    }
}
