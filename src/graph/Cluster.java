package graph;

import edu.uci.ics.jung.graph.UndirectedSparseGraph;

import java.util.Objects;

public class Cluster<V,E>{

    private String name = "c" + ++n;
    private UndirectedSparseGraph<V,E> graph;

    private static int n = 0;

    public Cluster() {
        graph = new UndirectedSparseGraph<>();
    }

    public Cluster(UndirectedSparseGraph<V, E> graph) {
        this.graph = graph;
    }

    public void addEdge(E e, V v1, V v2){
        graph.addEdge(e, v1, v2);
    }

    public void addVertex(V v){
        graph.addVertex(v);
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UndirectedSparseGraph<V, E> getGraph() {
        return graph;
    }

    public void setGraph(UndirectedSparseGraph<V, E> graph) {
        this.graph = graph;
    }

    public static int getN() {
        return n;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String[] strings = graph.toString().split("\n");

        sb.append(name)
                .append(" :\t").append(strings[0])
                .append("\n\t\t\t").append(strings[1]);

        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cluster<?, ?> cluster = (Cluster<?, ?>) o;
        return Objects.equals(name, cluster.name) && Objects.equals(graph, cluster.graph);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, graph);
    }
}
