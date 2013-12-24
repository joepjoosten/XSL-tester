package models;

import play.db.ebean.Model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Entity
public class Fiddle extends Model {

    private static String SHORTENER_CHARS = "123456789abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ";
    private static int ID_OFFSET = 656_356_768; // 100000 base 58, this ensures at leased 6 characters for the shortener
    private static int[] ENCODE_ARR = {7,4,1,8,5,2,9,6,3,10,11,0};
    private static int[] DECODE_ARR = {11,2,5,8,1,4,7,0,3,6,9,10};

    @Id
    private long id;

    @OneToMany(cascade = CascadeType.PERSIST)
    private List<FiddleRevision> fiddleRevisionList = new ArrayList<FiddleRevision>();

    public Fiddle() {
    }

    public long getId() {
        return id;
    }

    public List<FiddleRevision> getFiddleRevisionList() {
        return fiddleRevisionList;
    }

    public void setFiddleRevisionList(List<FiddleRevision> fiddleRevisionList) {
        this.fiddleRevisionList = fiddleRevisionList;
    }

    public void addFiddleRevision(FiddleRevision fiddleRevision){
        this.fiddleRevisionList.add(fiddleRevision);
    }

    public void addRevision(String xml, String xsl, String engine){
        FiddleRevision fiddleRevision = new FiddleRevision();
        fiddleRevision.setRevision(fiddleRevisionList.size());
        fiddleRevision.setXml(xml);
        fiddleRevision.setXsl(xsl);
        fiddleRevision.setEngine(engine);
        this.addFiddleRevision(fiddleRevision);
    }

    public String getShortId(){
        return Fiddle.encodeShortenedID(this.getId());
    }

    public static long decodeShortenedID(String shortenedId) {
        long id = 0;
        long multiplier = 1;
        for(int i = shortenedId.length(); i > 0; i--) {
            id += SHORTENER_CHARS.indexOf(shortenedId.charAt(i-1)) * multiplier;
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

    private static long hustle(String hustle, int[] order){
        char[] input = hustle.toCharArray();
        char[] output = new char[order.length];
        for(int i = 0; i < order.length; i++){
            output[order[i]] = input[i];
        }
        return Long.parseLong(new String(output));
    }

    public static Model.Finder<String, Fiddle> find = new Model.Finder(String.class, Fiddle.class);

    public static Fiddle getByShortId(String shortId){
        return find.byId(String.valueOf(Fiddle.decodeShortenedID(shortId)));
    }

    public FiddleRevision getFiddleRevision(int revision) {
        return FiddleRevision.find.where().eq("fiddle_id", this.id).eq("revision",revision).findUnique();

    }
}
