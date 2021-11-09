package nl.utwente.utgo.profile;

public abstract class Group {

    private String name;
    private int place;
    private int score;
    private String uid;
    private String title;

    public Group(String name, int place, int score, String uid, String title) {
        this.name = name;
        this.place = place;
        this.score = score;
        this.uid = uid;
        this.title = title;
    }

    public String getName() {
        return name;
    }

    public int getMemberCount() { return 0; }

    public int getPlace() {
        return place;
    }

    public int getScore() {
        return score;
    }

    public String getUid() {
        return uid;
    }

    public String getTitle() { return title; }
}
