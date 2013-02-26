package models;

import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class Fiddle extends Model {

    private static String SHORTENER_CHARS = "123456789abcdefghijkmnopqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ";
    private static int ID_OFFSET = 656_356_768; // 100000 base 58, this ensures at leased 6 characters for the shortener

    @Id
    private long id;
    @Column(columnDefinition = "TEXT")
    private String xml;
    @Column(columnDefinition = "TEXT")
    private String xsl;


    public long getId() {
        return id;
    }

    public String getXsl() {
        return xsl;
    }

    public void setXsl(String xsl) {
        this.xsl = xsl;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public String getShortId(){
        return Fiddle.encodeShortenedID(getId());
    }

    public static int decodeShortenedID(String shortenedId) {
        int id = 0;
        int multiplier = 1;
        for(int i = shortenedId.length(); i > 0; i--) {
            id += SHORTENER_CHARS.indexOf(shortenedId.charAt(i-1)) * multiplier;
            multiplier *= SHORTENER_CHARS.length();
        }
        return id - ID_OFFSET;
    }

    public static String encodeShortenedID(long id) {
        StringBuilder shortenedId = new StringBuilder();
        long leftover = id + ID_OFFSET;
        while (leftover > 0) {
            int remainder = (int) (leftover % SHORTENER_CHARS.length());
            shortenedId.append(SHORTENER_CHARS.charAt(remainder));
            leftover = leftover / SHORTENER_CHARS.length();

        }
        return shortenedId.reverse().toString();
    }

    public static Model.Finder<String, Fiddle> find = new Model.Finder(String.class, Fiddle.class);

}