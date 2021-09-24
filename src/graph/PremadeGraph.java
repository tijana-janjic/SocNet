package graph;

import edu.uci.ics.jung.graph.UndirectedSparseGraph;

public class PremadeGraph {
    public static UndirectedSparseGraph<Integer, Link<Integer>> getPremadeNet(){

        UndirectedSparseGraph<Integer, Link<Integer>> graph = new UndirectedSparseGraph<>();

        int n = 30;

        for (int i = 1; i <= n; i++) {
            graph.addVertex(i);
        }

        //pozitivne grane
        for (int v1 = 1; v1 <= n; v1++) {
            for (int v2 = v1+4; v2 <= n; v2+=4) {
                graph.addEdge(new Link<>(1),v1,v2);
            }
        }

        //negativne grane
        for (int v1 = 1; v1 <= n; v1++) {
            for (int v2 = v1+1; v2 <= n; v2++) {
                if(v1%2 != v2%2){
                    graph.addEdge(new Link<>(-1),v1,v2);
                }
            }
        }

        return graph;
    }
}
