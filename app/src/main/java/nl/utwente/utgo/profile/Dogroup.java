package nl.utwente.utgo.profile;

public class Dogroup extends Group {

    private int role;
    private int memberCount;

    public Dogroup(String name, int place, int score, String uid, String title, int memberCount, int role) {
        super(name, place, score, uid, title);
        this.memberCount = memberCount;
        this.role = role;
    }

    @Override
    public int getMemberCount() { return memberCount; }
}
