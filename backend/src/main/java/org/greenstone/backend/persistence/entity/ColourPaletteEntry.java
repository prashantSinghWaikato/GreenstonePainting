package org.greenstone.backend.persistence.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.Version;

@Entity
@Table(name = "colour_palette_entries")
public class ColourPaletteEntry extends BaseEntity {

    @Version
    @Column(nullable = false)
    private long version;

    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @Column(name = "hex_value", nullable = false, length = 7)
    private String hex;

    @Column(name = "resene_url", length = 1000)
    private String reseneUrl;

    @Column(nullable = false)
    private boolean active = true;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "default_interior", nullable = false)
    private boolean defaultInterior;

    @Column(name = "default_exterior", nullable = false)
    private boolean defaultExterior;

    protected ColourPaletteEntry() {
    }

    public ColourPaletteEntry(String name, String hex, int displayOrder) {
        this.name = name;
        this.hex = hex;
        this.displayOrder = displayOrder;
    }

    public long getVersion() { return version; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getHex() { return hex; }
    public void setHex(String hex) { this.hex = hex; }
    public String getReseneUrl() { return reseneUrl; }
    public void setReseneUrl(String reseneUrl) { this.reseneUrl = reseneUrl; }
    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }
    public int getDisplayOrder() { return displayOrder; }
    public void setDisplayOrder(int displayOrder) { this.displayOrder = displayOrder; }
    public boolean isDefaultInterior() { return defaultInterior; }
    public void setDefaultInterior(boolean defaultInterior) { this.defaultInterior = defaultInterior; }
    public boolean isDefaultExterior() { return defaultExterior; }
    public void setDefaultExterior(boolean defaultExterior) { this.defaultExterior = defaultExterior; }
}
