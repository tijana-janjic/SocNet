package main;

import edu.uci.ics.jung.graph.UndirectedSparseGraph;
import edu.uci.ics.jung.graph.util.Pair;
import graph.Cluster;
import graph.FriendLink;
import org.apache.commons.collections15.Transformer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ClusterabilityCheck<V, E> {

    //input
    private final UndirectedSparseGraph<V, E> net;
    private final Transformer<E, Boolean> transformer;

    //output
    private boolean clusterable;

    private final Collection<Cluster<V,E>> clusters;
    private final Collection<Cluster<V,E>> coalitions;
    private final Collection<Cluster<V,E>> anticoalitions;

    private final Collection<E> badEdges;
    private final UndirectedSparseGraph<Cluster<V,E>, FriendLink> clusterNet
            = new UndirectedSparseGraph<>();

    private final double density;
    private double degree;

    private int coalitionsVertices;
    private int anticoalitionsVertices;

    private final HashMap<V, Cluster<V, E>> map = new HashMap<>();


    /**
     * Konstruktor koji inicijalizuje mrezu i transformer i pokrece analizu
     *
     * @param net           Mreza koju je potrebno analizirati
     * @param transformer   Transformer za datu mrezu
     */
    public ClusterabilityCheck(UndirectedSparseGraph<V, E> net, Transformer<E, Boolean> transformer) {
        this.net = net;
        this.transformer = transformer;
        this.density = graphDensity();
        clusters = new HashSet<>();
        coalitions = new HashSet<>();
        anticoalitions = new HashSet<>();
        badEdges = new HashSet<>();
        init();
    }
    public double graphDensity() {
        double vertexCount = net.getVertexCount();
        double maxEdges = (vertexCount-1) * vertexCount / 2;
        return (double)(net.getEdgeCount())/maxEdges;
    }
    /**
     * Pokretanje provjere klasterabilnosti
     */
    private void init(){

        findComponents();
        createClusterNet();
        clusterable = anticoalitions.isEmpty();
        initAverageVertexDegree();
    }

    private void initAverageVertexDegree(){
        degree = getAverageVertexDegree(net);
    }

    private double getAverageVertexDegree(UndirectedSparseGraph<V,E> graph) {
        if (net.getVertexCount() == 0)
            return 0;
        int total = 0;
        for (V v : graph.getVertices()) {
            total += graph.degree(v);
        }
        return total / (double) net.getVertexCount();
    }

    /**
     * Kreira mrezu klastera,
     * particionise skup klastera na koalicije i antikoalicije,
     * i broji koliko je cvorova zastupljeno u skupu koalicija a koliko u skupu antikoalicija
     */

    private void createClusterNet() {

        for (Cluster<V,E> cluster : new HashSet<>(map.values()))
            clusterNet.addVertex(cluster);

        anticoalitionsVertices = 0;
        for (E e : net.getEdges() ) {
            if (!isFriendly(e)){
                Pair<V> vs = net.getEndpoints(e);
                V v1 = vs.getFirst();
                V v2 = vs.getSecond();
                Cluster<V, E> g1 = map.get(v1);
                Cluster<V, E> g2 = map.get(v2);

                if(g1 == g2){ // detektovana antikoalicija
                    if (!anticoalitions.contains(g1)){
                        anticoalitions.add(g1);
                        anticoalitionsVertices += g1.getGraph().getVertexCount();
                    }
                    g1.addEdge(e, v1, v2);
                    badEdges.add(e);
                } else if(clusterNet.findEdge(g1, g2) == null) // detektovana grana između klastera
                    clusterNet.addEdge(new FriendLink(false), g1, g2);
            }
        }
        coalitionsVertices = net.getVertexCount() - anticoalitionsVertices;
        coalitions.addAll(clusters);
        coalitions.removeAll(anticoalitions);
    }

    /**
     * Pronalazi sve povezane komponente u posmatranoj mrezi
     * pomocu modifikovanog BFS algoritma
     */
    private void findComponents() {
        HashSet<V> visited = new HashSet<>();
        for (V root : net.getVertices() ) {
            if(!visited.contains(root)){
                Cluster<V,E> cluster = bfs(visited, root);
                clusters.add(cluster);
            }
        }
    }

    private Cluster<V,E> bfs(HashSet<V> visited, V root) {

        Cluster<V,E> cluster = new Cluster<>();
        LinkedList<V> queue = new LinkedList<>();

        visited.add(root);
        cluster.addVertex(root);
        map.put(root, cluster);
        queue.add(root);

        while (!queue.isEmpty()) {
            V current = queue.removeFirst();
            for (V v : net.getNeighbors(current)) {
                E e = net.findEdge(v, current);
                if (isFriendly(e)) {
                    if (!visited.contains(v)) {
                        visited.add(v);
                        queue.addLast(v);
                        cluster.addVertex(v);
                        map.put(v, cluster);
                    }
                    if (!cluster.getGraph().containsEdge(e))
                        cluster.addEdge(e, current, v);
                }
            }
        }
        return cluster;
    }


    private boolean isFriendly(E e){
        return transformer.transform(e);
    }

    public UndirectedSparseGraph<V, E> getNet() {
        return net;
    }

    public Transformer<E, Boolean> getTransformer() {
        return transformer;
    }

    public boolean isClusterable() {
        return clusterable;
    }

    public Collection<Cluster<V, E>> getClusters() {
        return clusters;
    }

    public Collection<Cluster<V, E>> getCoalitions() {
        return coalitions;
    }

    public Collection<Cluster<V, E>> getAnticoalitions() {
        return anticoalitions;
    }

    public Collection<E> getBadEdges() {
        return badEdges;
    }

    public UndirectedSparseGraph<Cluster<V, E>, FriendLink> getClusterNet() {
        return clusterNet;
    }

    public double getDensity() {
        return density;
    }

    public double getDegree() {
        return degree;
    }

    public int getCoalitionsVertices() {
        return coalitionsVertices;
    }

    public int getAnticoalitionsVertices() {
        return anticoalitionsVertices;
    }

    public HashMap<V, Cluster<V, E>> getMap() {
        return map;
    }

    public String getShortStatistics(){
        StringBuilder sb = new StringBuilder();
        sb.append("Input mreza:")
                .append("\n\tBroj cvorova: ").append(net.getVertexCount())
                .append("\n\tBroj grana: ").append(net.getEdgeCount())
                .append("\n\tGustina:\t").append(String.format("%,.6f", density))
                .append("\n\tProsječan stepen čvora u mreži: ").append(String.format("%,.2f", degree))
                .append("\n\nRezultati klasterisanja:")
                .append("\n\tBroj klastera:\t").append(clusters.size());

        if (clusterable)
            sb.append("\n\tSve komponente su i koalicije!");
        else {
            String percentage = String.format("%,.2f", (100.0 * badEdges.size()) / net.getEdgeCount());
            sb.append("\n\tMreza nije klasterabilna!")
                    .append("\n\tBroj koalicija:\t").append(coalitions.size())
                    .append(" (broj cvorova: ").append(coalitionsVertices).append(")")
                    .append("\n\tBroj antikoalicija:\t").append(anticoalitions.size())
                    .append(" (broj cvorova: ").append(anticoalitionsVertices).append(")")
                    .append("\n\tGrane koje je potrebno izbaciti da bi mreza postala klaserabilna: \t\t").append(badEdges.size())
                    .append(" (").append(percentage).append("%).");
        }
        sb.append("\n\nMreza klastera: ")
                .append("\n\tBroj cvorova: ").append(clusterNet.getVertexCount())
                .append("\n\tBroj grana: ").append(clusterNet.getEdgeCount());
        return sb.toString();
    }

    public void exportResults(){

        BufferedWriter out = null;

        try {
            System.out.println("Eksportovanje fajla info.txt...");
            out = new BufferedWriter(new FileWriter("src/files/out/info.txt"));
            out.append("Short statistics:\n\n").append(getShortStatistics());
            out.close();

            System.out.println("Eksportovanje fajla net.txt...");
            out = new BufferedWriter(new FileWriter("src/files/out/net.txt"));
            out.append("Mreža:\n\n").append(prettyStringNet());
            out.close();

            System.out.println("Eksportovanje fajla clusters.txt...");
            out = new BufferedWriter(new FileWriter("src/files/out/clusters.txt"));
            out.append("Klasteri (").append(String.valueOf(clusters.size())).append("):\n")
                    .append(prettyStringCollection(clusters));
            out.close();

            System.out.println("Eksportovanje fajla coalitions.txt...");
            out = new BufferedWriter(new FileWriter("src/files/out/coalitions.txt"));
            out.append("Koalicije (").append(String.valueOf(coalitions.size())).append("):\n")
                    .append(prettyStringCollection(coalitions));
            out.close();

            System.out.println("Eksportovanje fajla anticoalitions.txt...");
            out = new BufferedWriter(new FileWriter("src/files/out/anticoalitions.txt"));
            out.append("Antikoalicije (").append(String.valueOf(anticoalitions.size())).append("):\n")
                    .append(prettyStringCollection(anticoalitions));
            out.close();

            System.out.println("Eksportovanje fajla bad-edges.txt...");
            out = new BufferedWriter(new FileWriter("src/files/out/bad-edges.txt"));
            out.append("Grane koje narusavaju fajla klasterabilnost (").append(String.valueOf(badEdges.size())).append("):\n")
                    .append(prettyStringCollectionOfEdges(badEdges));
            out.close();

            System.out.println("Eksportovanje fajla net-of-clusters.txt...");
            out = new BufferedWriter(new FileWriter("src/files/out/net-of-clusters.txt"));
            out.append("Mreza klastera (")
                    .append(String.valueOf(clusterNet.getVertexCount())).append(",")
                    .append(String.valueOf(clusterNet.getEdgeCount())).append("):\n")
                    .append(prettyStringNetOfClusters());
            out.close();

            System.out.println("\nEksportovanje svih fajlova uspješno završeno!");
        } catch (IOException e) {
            System.err.println("Greska pri pisanju u fajl!");
        } finally {
            try {
                if(out != null)
                    out.close();
            } catch (IOException e) {
                System.err.println("Greska pri zatvaranju BufferedWriter-a!");
            }
        }
    }

    private String prettyStringCollectionOfEdges(Collection<E> collection){
        if (collection.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (E e : collection ){
            Pair<V> vp = net.getEndpoints(e);
            sb.append("\n\t").append(e).append("[").append(vp.getFirst()).append(",").append(vp.getSecond()).append("]");
        }
        return sb.toString();
    }

    private String prettyStringCollection(Collection<Cluster<V,E>> collection){
        if (collection.isEmpty()) return "";
        StringBuilder sb = new StringBuilder();
        for (Cluster<V,E> cluster : collection ) {
            sb.append("\t").append(cluster.toString()).append("\n\n");
        }
        return sb.toString();
    }

    private String prettyStringNet(){
        StringBuilder sb = new StringBuilder();
        sb.append("\tVertices:\n");
        int i = 0;
        for ( V v : net.getVertices()) {
            sb.append(v).append(",");
            i++;
            if (i == 20) {
                sb.append('\n');
                i = 0;
            }
        }
        if (!net.getVertices().isEmpty())
            sb.deleteCharAt(sb.length()-1);

        sb.append("\n\tEdges:\n");
        i = 0;
        for (E link : net.getEdges()) {
            Pair<V> vertices = net.getEndpoints(link);
            sb.append(link.toString())
                    .append("[").append(vertices.getFirst())
                    .append(",").append(vertices.getSecond()).append("]").append(",");
            i++;
            if (i == 5) {
                sb.append('\n');
                i = 0;
            }
        }
        if (!clusterNet.getEdges().isEmpty())
            sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }

    private String prettyStringNetOfClusters(){
        StringBuilder sb = new StringBuilder();

        sb.append("\tVertices:\n");
        int i = 0;
        for ( Cluster<V,E> cluster : clusterNet.getVertices()) {
            sb.append(cluster.getName()).append(",");
            i++;
            if (i == 20) {
                sb.append('\n');
                i = 0;
            }
        }
        if (!clusterNet.getVertices().isEmpty())
            sb.deleteCharAt(sb.length()-1);

        sb.append("\n\tEdges:\n");
        i = 0;
        for ( FriendLink link : clusterNet.getEdges()) {
            Pair<Cluster<V,E>> vertices = clusterNet.getEndpoints(link);
            sb.append(link.toString())
                    .append("[").append(vertices.getFirst().getName())
                    .append(",").append(vertices.getSecond().getName()).append("]").append(",");
            i++;
            if (i == 5) {
                sb.append('\n');
                i = 0;
            }
        }
        if (!clusterNet.getEdges().isEmpty())
            sb.deleteCharAt(sb.length()-1);
        return sb.toString();
    }


}