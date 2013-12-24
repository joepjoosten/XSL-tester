package models;

import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.ManyToOne;

@Entity
public class FiddleRevision extends Model {

    @Id
    private long id;

    private int revision;

    private String engine;

    @ManyToOne
    private Fiddle fiddle;

    @Column(columnDefinition = "TEXT")
    private String xml;
    @Column(columnDefinition = "TEXT")
    private String xsl;

    public FiddleRevision() {
    }

    public long getId() {
        return id;
    }

    public int getRevision() {
        return revision;
    }

    public void setRevision(int revision) {
        this.revision = revision;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
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

    public static Finder<String, FiddleRevision> find = new Finder(String.class, FiddleRevision.class);

}
