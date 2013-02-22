package models;

import play.db.ebean.Model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.util.UUID;

@Entity
public class Fiddle extends Model {



    @Id
    private UUID id;
    @Column(columnDefinition = "TEXT")
    private String xml;
    @Column(columnDefinition = "TEXT")
    private String xsl;


    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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
    public static Model.Finder<String, Fiddle> find = new Model.Finder(String.class, Fiddle.class);

}