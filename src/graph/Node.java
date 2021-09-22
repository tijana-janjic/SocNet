package graph;

public class Node<V> {

    private String id;
    private V value;
    protected static int n = 0;

    public Node() {
        this.id = "v"+ ++n;
    }

    public Node(V value) {
        this.value = value;
        this.id = "v" + value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public V getValue() {
        return value;
    }

    public void setValue(V value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return ""+id;
    }
}
