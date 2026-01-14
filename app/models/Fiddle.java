package models;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "fiddle")
public class Fiddle {

    private static final String SHORTENER_CHARS = "123456789abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ";
    private static final int ID_OFFSET = 656_356_768; // 100000 base 58, ensures at least 6 characters
    private static final int[] ENCODE_ARR = {7, 4, 1, 8, 5, 2, 9, 6, 3, 10, 11, 0};
    private static final int[] DECODE_ARR = {11, 2, 5, 8, 1, 4, 7, 0, 3, 6, 9, 10};

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToMany(mappedBy = "fiddle", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    @OrderBy("revision ASC")
    private List<FiddleRevision> revisions = new ArrayList<>();

    public Fiddle() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public List<FiddleRevision> getRevisions() {
        return revisions;
    }

    public void setRevisions(List<FiddleRevision> revisions) {
        this.revisions = revisions;
    }

    public void addRevision(String xml, String xsl, String engine) {
        FiddleRevision revision = new FiddleRevision();
        revision.setRevision(revisions.size() + 1);
        revision.setXml(xml);
        revision.setXsl(xsl);
        revision.setEngine(engine);
        revision.setFiddle(this);
        this.revisions.add(revision);
    }

    public String getShortId() {
        return encodeShortenedID(this.id);
    }

    public FiddleRevision getRevision(int revisionNumber) {
        return revisions.stream()
                .filter(r -> r.getRevision() == revisionNumber)
                .findFirst()
                .orElse(null);
    }

    public FiddleRevision getLatestRevision() {
        if (revisions.isEmpty()) {
            return null;
        }
        return revisions.get(revisions.size() - 1);
    }

    public static long decodeShortenedID(String shortenedId) {
        long id = 0;
        long multiplier = 1;
        for (int i = shortenedId.length(); i > 0; i--) {
            id += SHORTENER_CHARS.indexOf(shortenedId.charAt(i - 1)) * multiplier;
            multiplier *= SHORTENER_CHARS.length();
        }
        String hustle = String.format("%012d", id);
        id = hustle(hustle, DECODE_ARR);
        return id - ID_OFFSET;
    }

    public static String encodeShortenedID(long id) {
        StringBuilder shortenedId = new StringBuilder();
        long leftover = id + ID_OFFSET;

        String hustle = String.format("%012d", leftover);
        leftover = hustle(hustle, ENCODE_ARR);

        while (leftover > 0) {
            int remainder = (int) (leftover % SHORTENER_CHARS.length());
            shortenedId.append(SHORTENER_CHARS.charAt(remainder));
            leftover = leftover / SHORTENER_CHARS.length();
        }
        return shortenedId.reverse().toString();
    }

    private static long hustle(String hustle, int[] order) {
        char[] input = hustle.toCharArray();
        char[] output = new char[order.length];
        for (int i = 0; i < order.length; i++) {
            output[order[i]] = input[i];
        }
        return Long.parseLong(new String(output));
    }
}
