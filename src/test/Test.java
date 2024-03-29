package test;

import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import graph.Link;
import graph.PremadeGraph;
import graph.RandomNetGenerator;
import graph.SocNetFileReader;
import main.ClusterabilityCheck;

import java.util.Scanner;

public class Test {
    public static void main(String[] args) {

        Scanner scanner = new Scanner(System.in);
        SocNetFileReader in = new SocNetFileReader();
        UndirectedSparseGraph<Integer, Link<Integer>> graph;

        System.out.println("Odaberite graf za analizu:");
        System.out.println("\t1: Mali pripremljeni graf");
        System.out.println("\t2: Wiki");
        System.out.println("\t3: Slashdot");
        System.out.println("\t4: Epinions");
        System.out.println("\t5: Random mali graf");
        System.out.println("\t6: Random mali klasterabilan graf");
        System.out.println("\t7: Random veliki graf");
        System.out.println("\t8: Random veliki klasterabilan graf");

        String str;
        int op = -1;
        do {
            str = scanner.nextLine();
            if (str.matches("[1-8]"))
                op = Integer.parseInt(str);
            else
                System.out.println("Pokusajte ponovo");

        } while (op < 1 || op > 8);

        switch (op) {
            case 1 -> graph = PremadeGraph.getPremadeNet();
            case 2 -> graph = in.readWiki();
            case 3 -> graph = in.readSlashDot();
            case 4 -> graph = in.readEpions();
            case 5 -> graph = RandomNetGenerator.generateRandomUnclusterableSmallNet();
            case 6 -> graph = RandomNetGenerator.generateRandomClusterableSmallNet();
            case 7 -> graph = RandomNetGenerator.generateRandomUnclusterableNet();
            default -> graph = RandomNetGenerator.generateRandomClusterableNet();
        }

        ClusterabilityCheck<Integer, Link<Integer>> cc
                = new ClusterabilityCheck<>(graph, x -> x.getValue() > 0);
        System.out.println(cc.getShortStatistics());
        boolean ok;
        do {
            ok = true;
            System.out.println("\nDa li želite da eksportujete rezultate? (y/n)");
            str = scanner.nextLine();
            switch (str) {
                case "y" -> cc.exportResults();
                case "n" -> System.out.println("Ok.");
                default -> ok = false;
            }
        } while(!ok);

    }
}
