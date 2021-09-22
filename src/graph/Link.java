package graph;

public class Link<E> {
    private static int n = 0;
    private String id;
    private E value;

    public Link(E value) {
        this.id = "e"+ ++n;
        this.value = value;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public E getValue() {
        return value;
    }

    public void setValue(E value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return id + '(' + value + ')';
    }

}
