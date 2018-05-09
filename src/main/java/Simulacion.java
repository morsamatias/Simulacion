import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Simulacion {


    /////////////GLOBAL VAR////////////////

    private static Integer N = 0;
    private static Integer HV = -1;
    private static Integer T = 0;
    private static Integer TF;
    private static Integer TPT = 0;
    private static Double[] NTLineMax;
    private static Double[] NTLineMin;
    private static Integer[] NTLine;
    private static Integer[] TPS;
    private static Integer[] lines;
    private static Long[] PERCENTAGE;
    private static Long[] STA;
    private static Long[] STS;
    private static Long[] STLL;
    private static Long[] WAITINGTIME;






    public static void main (String[] args) {
        System.out.println("Ingrese cantidad de colas: ");
        Scanner inputColas = new Scanner( System.in );

        N = Integer.valueOf(inputColas.nextLine());

        TPS          = new Integer[N];
        lines        = new Integer[N];
        STA          = new Long[N];
        STS          = new Long[N];
        STLL         = new Long[N];
        WAITINGTIME  = new Long[N];
        PERCENTAGE   = new Long[N];
        NTLine       = new Integer[N];
        NTLineMin    = new Double[N];
        NTLineMax    = new Double[N];

        initialize(TPS,-1);
        initialize(lines,0);
        initialize(NTLine,0);
        initializeLong(PERCENTAGE, 0);
        initializeLong(STA,0);
        initializeLong(STS,0);
        initializeLong(STLL,0);
        initializeLong(WAITINGTIME,0);
        initializeNTLineMax();



        System.out.println("Ingrese tiempo final (en decimas de segundo): ");
        Scanner inputTF = new Scanner( System.in );

        TF = Integer.valueOf(inputTF.nextLine());


        while(T<TF) {
            simulation();
        }


        for(int i = 0; i < lines.length; i++) {
            if(!(lines[i] == 0)) {
                TPT = HV;
                empty();
            }
        }

        printAnswer();

    }




    ///////////////////////////////////////////// FUNCTIONS ////////////////////////////////////////////////////////////




    private static void simulation() {
        Integer minTps = minTPS();
        Integer minTpsIndex = minTPSIndex(minTps);

        if (((TPT >= minTps) && (TPT == HV)) && (minTps >= 0)) {
            T = T + minTps;
            lines[minTpsIndex] = lines[minTpsIndex] - 1;
            processExit(minTpsIndex);
            NTLine[minTpsIndex] = NTLine[minTpsIndex] + 1;
            STS[minTpsIndex] = STS[minTpsIndex] + T;

        } else {
            T = TPT;
            Integer NA = 864000/Integer.valueOf(String.valueOf(nextArrival(random())).substring(0,6));
            TPT = T + NA;
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
                    Integer TA = Integer.valueOf(String.valueOf(attentionTime()).substring(0,1));
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

        Integer minTps = minTPS();
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


    private static double attentionTime() {

        Integer rangeMin = 1;
        Integer rangeMax = 10;

        return ThreadLocalRandom.current().nextDouble(rangeMin, rangeMax);
    }

    private static void processExit(Integer index) {

        if(lines[index] >= 1) {
            Integer TA = Integer.valueOf(String.valueOf(attentionTime()).substring(0,1));
            TPS[index] = T + TA;
            STA[index] = STA[index] + TA;

        } else {
            TPS[index] = HV;
            for (int x = 0; x<lines.length; x++){
                if (lines[x]>=0){
                    Integer TA = Integer.valueOf(String.valueOf(attentionTime()).substring(0,1));
                    TPS[x] = T + TA;
                    STA[x] = STA[x] + TA;

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

    private static double nextArrival(double r) {
        double sigma = 106540;
        double mu = 128800;

        if(r > 0.9968724239) {
            nextArrival(random());
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
       Double probability =  ThreadLocalRandom.current().nextDouble(0, 5);


       return (lineDistribution(probability));

    }


    private static Integer minTPS() {

        List<Integer> filterList = Arrays.asList(TPS).stream().filter(n -> n != -1).collect(Collectors.toList());
        if(!(filterList).isEmpty()) {
            return Collections.min(filterList);
        }
        return -1;
    }

    public static int minTPSIndex (int minimoValor) {

        int index = -1;

        List<Integer> filterList = Arrays.asList(TPS).stream().filter(n -> n != -1).collect(Collectors.toList());

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

    public static void printAnswer(){

        Integer NTLineTotal = Arrays.asList(NTLine).stream().mapToInt(Integer::intValue).sum();

        // Calculate the results, average of waiting team for each line
        for (int i = 0; i < lines.length; i++)
            if(NTLine[i]!=0) {
                WAITINGTIME[i] = (STS[i] - STLL[i] - STA[i]) / NTLine[i];
            }else{
                WAITINGTIME[i] = Long.valueOf(0);
            }

        // Calculate the results, percentage of processed transactions in that line on total transactions.
        for (int i = 0; i < lines.length; i++) {
            PERCENTAGE[i] = (Long.valueOf(NTLine[i] * 100)) / NTLineTotal;
        }

        for (int i = 0; i < lines.length; i++) {
            System.out.println("Waiting time in the line:" + (i+1) +" = "  + WAITINGTIME[i] + "\n" + "percentage of transactions in " +
                    "the line " + (i+1) + " of the total: " + PERCENTAGE[i] );

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

    //IN BITS PER BYTE

    public static void initializeNTLineMax(){


        Double x = 5.00 /N;

        for (int i = 0; i < NTLineMin.length; i++){
            NTLineMax[i] = x*(i+1);
        }
        initializeNTLineMin();

    }

    public static void initializeNTLineMin(){
        int i;
        Double x = 5.00 /N;
        for (i = 0; i < NTLineMin.length; i++) {
            NTLineMin[i] = x * i;
            System.out.println("NTLineMin = " + NTLineMin[i] + " NTLineMax[" + i + "] =  " + NTLineMax[i]);
        }
    }

    public static int lineDistribution(Double value){
        int j=0;
        for (int i = 0;i<N;i++){
            if(NTLineMin[i] < value && value < NTLineMax[i]) {
                j = i;
            }

        }
        System.out.println("value =" + value + " j=" + j);
        return j;
    }
}
