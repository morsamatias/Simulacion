import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Simulacion {

    private static Integer N = 0;
    private static Integer HV = -1;
    private static Integer T = 0;
    private static Integer TF;
    private static Integer TPT = 0;
    private static Integer[] NTLine;
    private static Integer[] TPS;
    private static Integer[] lines;
    private static Integer[] STA;
    private static Integer[] STS;
    private static Integer[] STLL;
    private static Integer[] WAITINGTIME;
    private static Integer[] PERCENTAGE;



    public static void main (String[] args) {
        System.out.println("Ingrese cantidad de colas: ");
        Scanner inputColas = new Scanner( System.in );

        N = Integer.valueOf(inputColas.nextLine());

        TPS          = new Integer[N];
        lines        = new Integer[N];
        STA          = new Integer[N];
        STS          = new Integer[N];
        STLL         = new Integer[N];
        WAITINGTIME  = new Integer[N];
        PERCENTAGE   = new Integer[N];
        NTLine       = new Integer[N];

        inicializar(TPS,-1);
        inicializar(lines,0);
        inicializar(STA,0);
        inicializar(STS,0);
        inicializar(STLL,0);
        inicializar(WAITINGTIME,0);
        inicializar(NTLine,0);
        inicializar(PERCENTAGE, 0);



        System.out.println("Ingrese tiempo final (en decimas de segundo): ");
        Scanner inputTF = new Scanner( System.in );

        TF = Integer.valueOf(inputTF.nextLine());


        while(T<TF) {
            simulacion();
        }


        for(int i = 0; i < lines.length; i++) {
            if(!(lines[i] == 0)) {
                TPT = HV;
                empty();
            }
        }

        printAnswer();

    }

    private static void simulacion() {
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
            Integer linePosition = Integer.valueOf(String.valueOf(linesPosition()).substring(0, 1));

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

    private static double linesPosition() {

        Integer rangeMin = 0;
        Integer rangeMax = N;

        if((ThreadLocalRandom.current().nextDouble(rangeMin, rangeMax-1)) > N){
            return 0;
        }


        return ThreadLocalRandom.current().nextDouble(rangeMin, rangeMax);
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
            WAITINGTIME[i] = (STS[i]-STLL[i]-STA[i])/NTLine[i];

        // Calculate the results, percentage of processed transactions in that line on total transactions.
        for (int i = 0; i < lines.length; i++)
            PERCENTAGE[i] = (NTLine[i]*100)/NTLineTotal;

        for (int i = 0; i < lines.length; i++) {
            System.out.println("Tiempo de espera en la cola:" + i +" = "  + WAITINGTIME[i] + "\n" + "porcentaje de transacciones en " +
                    "la linea " + i + " respecto al total: " + PERCENTAGE[i] );

        }
    }


    public static void inicializar(Integer[] lista,int valorInicial){
        for (int i = 0; i < lista.length; i++){
            lista[i] = valorInicial;
        }
    }

}
