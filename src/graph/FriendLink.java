package graph;

public class FriendLink extends Link<Boolean>{
    private static int n = 0;
    private final String id;
    private boolean friend;

    public FriendLink(boolean friend) {
        super(friend);
        this.id = "e"+ ++n;
    }

    public boolean isFriend() {
        return friend;
    }

    public void setFriend(boolean friend) {
        this.friend = friend;
    }

    @Override
    public String toString() {
        char sign = friend ? '+' : '-';
        return id + '(' + sign + ')';
    }

}
