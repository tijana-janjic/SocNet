package graph;

import edu.uci.ics.jung.graph.UndirectedSparseGraph;

import java.util.*;

public class RandomNetGenerator {

    private static final Random random = new Random();

    public static UndirectedSparseGraph<Integer, Link<Integer>> generateRandomClusterableSmallNet() {
        return generateRandomNet(random.nextInt(50)+6, random.nextInt(5)+1, true);
    }

    public static UndirectedSparseGraph<Integer, Link<Integer>> generateRandomUnclusterableSmallNet() {
        return generateRandomNet(random.nextInt(50)+6, random.nextInt(5)+1, false);
    }

    public static UndirectedSparseGraph<Integer, Link<Integer>> generateRandomSmallNet() {
        return generateRandomNet(random.nextInt(50)+6, random.nextInt(5)+1, random.nextBoolean());
    }

    public static UndirectedSparseGraph<Integer, Link<Integer>> generateRandomClusterableNet() {
        return generateRandomNet(random.nextInt(30000) + 10000, random.nextInt(5000)+1, true);
    }

    public static UndirectedSparseGraph<Integer, Link<Integer>> generateRandomUnclusterableNet() {
        return generateRandomNet(random.nextInt(30000) + 10000, random.nextInt(5000) + 1, false);
    }

    public static UndirectedSparseGraph<Integer, Link<Integer>> generateRandomNet() {
            return generateRandomNet(random.nextInt(30000) + 10000, random.nextInt(5000)+1, random.nextBoolean());
    }

    private static UndirectedSparseGraph<Integer, Link<Integer>> generateRandomNet(int vertexCount, int clusterCount, boolean clusterable){

        System.out.println("Generisanje random mreže... ");
        UndirectedSparseGraph<Integer, Link<Integer>> graph = new UndirectedSparseGraph<>();


        System.out.println("Dodavanje cvorova u graf...");

        // Dodavanje cvorova u graf
        for (int i = 0; i < vertexCount; i++) {
            graph.addVertex(i);
        }

        System.out.println("Kreiranje klastera...");

        // Distribucija cvorova po klasterima
        // randomiziramo redoslijed ubacivanja
        Stack<Integer> unvisited = new Stack<>();
        unvisited.addAll(graph.getVertices());
        Collections.shuffle(unvisited);

        HashMap<Integer, ArrayList<Integer>> map = new HashMap<>();
        ArrayList<ArrayList<Integer>> clusters = new ArrayList<>(clusterCount);

        for (int i = 0; i < clusterCount; i++) {
            ArrayList<Integer> cluster = new ArrayList<>();
            clusters.add(cluster);
            // svaki klaster ima bar jedan cvor
            int v = unvisited.pop();
            cluster.add(v);
            // mapiramo v na cluster
            map.put(v, cluster);
        }

        System.out.println("Raspodjela cvorova po klasterima...");

        // raspodjela ostalih cvorova po klasterima
        for (int i = 0; i < vertexCount; i++) {
            if(!map.containsKey(i)){
                ArrayList<Integer> c = clusters.get(random.nextInt(clusterCount));
                c.add(i);
                map.put(i, c);
            }
        }

        HashMap<ArrayList<Integer>, Integer> mapEdgesCount = new HashMap<>();
        // dodavanje grana
        System.out.println("Konstrukcija minimalnih povezanih komponenti...");
        for (ArrayList<Integer> cluster : clusters) {
            int count = createMinimalTree(graph, cluster);
            mapEdgesCount.put(cluster, count);
        }

        System.out.println("Dodavanje grana medju klasterima..." );
        addClusterEdges(graph, map, vertexCount, clusterCount);


        System.out.println("Dodavanje grana unutar klastera..." );
        for (ArrayList<Integer> cluster : clusters)
            addOtherEdges(graph, cluster, clusterable, mapEdgesCount.get(cluster));

        System.out.println("\nGenerisanje random mreže završeno!\n");

        return graph;
    }

    /**
     *
     * Dodajemo negativne grane izmedju cvorova koji se nalaze u razlicitim klasterima
     *
     */
    private static void addClusterEdges(UndirectedSparseGraph<Integer, Link<Integer>> graph, HashMap<Integer, ArrayList<Integer>> map, int vertexCount, int clusterCount) {
        if (clusterCount == 1)
            return;
        long max = (long) clusterCount * (clusterCount-1) / 2;
        long edgeCount = (long) (random.nextDouble() * max); // maksimalan broj grana u neusmjerenom grafu
        while (edgeCount > 0) {
            int v1 = random.nextInt(vertexCount);
            int v2 = random.nextInt(vertexCount);
            if(v1 != v2 && map.get(v1) != map.get(v2) && graph.findEdge(v1,v2) == null){
                graph.addEdge(new Link<>(-1), v1, v2);
                edgeCount--;
            }
        }
    }


    /**
     *
     * Dodaje pozitivne grane u klaster c, tako da se konstruise minimalno pokrivajuce stablo,
     * takvo da je za pocetak svaki klaster potencijalno i koalicija
     *
     * @param graph     : Mreza koju konstruisemo
     * @param clusterVertices         : Klaster u koji dosajemo grane
     * @return          : Broj dodatih grana u klasteru
     */
    private static int createMinimalTree(UndirectedSparseGraph<Integer, Link<Integer>> graph, ArrayList<Integer> clusterVertices) {

        if (clusterVertices.size() == 1)
            return 0;

        ArrayList<Integer> visited = new ArrayList<>();
        ArrayList<Integer> unvisited = new ArrayList<>(clusterVertices);
        visited.add(unvisited.remove(0)); // dodamo korijen stabla
        int edges = 0;
        while(unvisited.size() > 0){
            int index1 = random.nextInt(visited.size());
            int index2 = random.nextInt(unvisited.size());

            int v1 = visited.get(index1);
            int v2 = unvisited.get(index2);

            graph.addEdge(new Link<>(1), v1, v2);
            edges++;
            visited.add(unvisited.remove(index2));
            visited.remove(index1);
        }
        return edges;
    }

    /**
     *
     * Dodaje preostale grane u klastere:
     *  - ako se zahtijeva klasterabilan graf : samo pozitivne
     *  - inace: dodaje granu izmedju dva nasumicno odabrana cvora;
     *           grana je pozitivna ili negativna u zavisnosti od nekog koeficijenta (vjerovatnoce)
     *
     * @param graph             : Mreza koju konstruisemo
     * @param cluster           : Klaster u koji dosajemo grane
     * @param clusterable       : Da li se zahtijeva klasterabilna mreza?
     */
    private static void addOtherEdges(UndirectedSparseGraph<Integer, Link<Integer>> graph, ArrayList<Integer> cluster, boolean clusterable, int added) {
        if (cluster.size() == 1)
            return;

        long max = (long) cluster.size() * (cluster.size()-1) / 2  - added;

        if (max == 0)
            return;

        long edgeCount = (long) (random.nextDouble() * max);

        int v1, v2;

        double coefFriendly = 0.5;
        while (edgeCount > 0) {

            v1 = cluster.get(random.nextInt(cluster.size()));
            v2 = cluster.get(random.nextInt(cluster.size()));
            Link<Integer> link = graph.findEdge(v1, v2);
            if (v1 != v2 && link == null){
                if (!clusterable && random.nextDouble() > coefFriendly)
                    graph.addEdge(new Link<>(-1), v1, v2);
                else
                    graph.addEdge(new Link<>(1), v1, v2);
                edgeCount--;
            }
        }

    }

}