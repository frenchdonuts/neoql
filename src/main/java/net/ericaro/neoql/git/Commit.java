package net.ericaro.neoql.git;

public class Commit {
    static long idCount = 0L;

    long id = idCount++;
    String comment;

    public Commit(String comment) {
        super();
        this.comment = comment;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public String toString() {
        return comment + "[" + id + "]";
    }
}
