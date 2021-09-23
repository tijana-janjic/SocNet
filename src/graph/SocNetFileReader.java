package graph;

import edu.uci.ics.jung.graph.UndirectedSparseGraph;

import java.io.*;

public class SocNetFileReader {

    public UndirectedSparseGraph<Integer, Link<Integer>> readFromSimpleTxtFile(String path){

        UndirectedSparseGraph<Integer, Link<Integer>> graph = null;
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(path));
            graph = new UndirectedSparseGraph<>();

            for (int i = 0; i < 4; i++) {
                br.readLine();
            }

            String line = br.readLine();

            while (line != null){

                String[] data = line.split("\\s+");
                int vertex1 = Integer.parseInt(data[0].trim());
                int vertex2 = Integer.parseInt(data[1].trim());
                int edgeVal = Integer.parseInt(data[2].trim());

                addEdge(graph, vertex1, vertex2, edgeVal);

                line = br.readLine();
            } 

        } catch (Exception e) {
            System.err.println("Greska pri citanju fajla!");
            e.printStackTrace();
        } finally {
            if (br != null)
                try {
                    br.close();
                } catch (IOException e) {
                    System.err.println("Greska pri zatvaranju Buffered Reader-a!");
                    e.printStackTrace();
                }
        }
        return graph;
    }

    public UndirectedSparseGraph<Integer, Link<Integer>> readWiki(String path){

        UndirectedSparseGraph<Integer, Link<Integer>> graph = null;
        BufferedReader br = null;

        try {
            br = new BufferedReader(new FileReader(path));
            graph = new UndirectedSparseGraph<>();
            String line = br.readLine();
            int vertex1 = 0;

            while (line != null) {
                String[] data = line.split("\\s+");
                line = br.readLine();

                String token = data[0].trim();

                if (!token.equals("N") && !token.contains("V"))
                    continue;

                if (token.equals("N")) {
                    vertex1 = Integer.parseInt(data[1].trim());
                    continue;
                }

                int vertex2 = Integer.parseInt(data[2].trim());
                int edgeVal = Integer.parseInt(data[1].trim());

                if (edgeVal != 0) {
                    addEdge(graph, vertex1, vertex2, edgeVal);
                }
            }

        } catch (Exception e) {
            System.err.println("Greska pri citanju fajla!");
            e.printStackTrace();
        } finally {
            if (br != null)
                try {
                    br.close();
                } catch (IOException e) {
                    System.err.println("Greska pri zatvaranju Buffered Reader-a!");
                    e.printStackTrace();
                }
        }
        System.out.println("Graf je ucitan!");
        return graph;
    }

    private void addEdge(UndirectedSparseGraph<Integer, Link<Integer>> graph, int vertex1, int vertex2, int edgeVal) {
        if (!graph.containsVertex(vertex1))
            graph.addVertex(vertex1);
        if (!graph.containsVertex(vertex2))
            graph.addVertex(vertex2);
        if (vertex1 != vertex2) {
            Link<Integer> edge = graph.findEdge(vertex1, vertex2);
            if (edge == null)
                graph.addEdge(new Link<>(edgeVal), vertex1, vertex2);
            else {
                if (edgeVal < 0)
                    edge.setValue(edgeVal);
            }
        }
    }

    public UndirectedSparseGraph<Integer, Link<Integer>> readSlashDot() {
        return readFromSimpleTxtFile("src/files/in/soc-sign-Slashdot.txt");
    }

    public UndirectedSparseGraph<Integer, Link<Integer>> readEpions() {
        return readFromSimpleTxtFile("src/files/in/soc-sign-epinions.txt");
    }

    public UndirectedSparseGraph<Integer, Link<Integer>> readWiki(){
        return readWiki("src/files/in/wikiElec.ElecBs3.txt");
    }



}
