package models;

import jakarta.persistence.*;

@Entity
@Table(name = "fiddle_revision")
public class FiddleRevision {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "fiddle_id", nullable = false)
    private Fiddle fiddle;

    @Column(nullable = false)
    private Integer revision;

    @Column(length = 50)
    private String engine;

    @Column(columnDefinition = "TEXT")
    private String xml;

    @Column(columnDefinition = "TEXT")
    private String xsl;

    public FiddleRevision() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Fiddle getFiddle() {
        return fiddle;
    }

    public void setFiddle(Fiddle fiddle) {
        this.fiddle = fiddle;
    }

    public Integer getRevision() {
        return revision;
    }

    public void setRevision(Integer revision) {
        this.revision = revision;
    }

    public String getEngine() {
        return engine;
    }

    public void setEngine(String engine) {
        this.engine = engine;
    }

    public String getXml() {
        return xml;
    }

    public void setXml(String xml) {
        this.xml = xml;
    }

    public String getXsl() {
        return xsl;
    }

    public void setXsl(String xsl) {
        this.xsl = xsl;
    }
}
