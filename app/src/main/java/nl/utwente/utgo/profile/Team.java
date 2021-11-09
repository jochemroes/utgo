package nl.utwente.utgo.profile;

import java.util.List;

public class Team extends Group {

    private boolean discoverable;
    private int role;
    private List<String> members;
    private int memberCount;

    public Team(String name, int place, int score, String uid, String title, int memberCount, List<String> members, boolean discoverable, int role) {
        super(name, place, score, uid, title);
        this.memberCount = memberCount;
        this.members = members;
        this.discoverable = discoverable;
        this.role = role;
    }

    public boolean isDiscoverable() {
        return discoverable;
    }

    public void setDiscoverable(boolean discoverable) { this.discoverable = discoverable;}

    public int getRole() {
        return role;
    }

    @Override
    public int getMemberCount() { return memberCount; }

    public List<String> getMembers() { return members; }
}