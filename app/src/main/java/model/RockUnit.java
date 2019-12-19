package model;


public class RockUnit {
    private static int m_nextId;
    private String m_colour;
    private int m_id;
    private boolean m_isVisible;
    private String m_name;

    public RockUnit(int n, String string2, String string3) {
        this.m_isVisible = true;
        this.m_id = n;
        if (this.m_id >= m_nextId) {
            m_nextId = n + 1;
        }
        this.m_name = string2;
        this.m_colour = string3;
    }


    private String verifyColour(String string2) {
        if (!string2.startsWith("#")) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("#");
            stringBuilder.append(string2);
            return stringBuilder.toString();
        }
        return string2;
    }

    public String getColour() {
        return this.m_colour;
    }

    public String getColourDb() {
        if (this.m_colour.startsWith("#")) {
            return this.m_colour.substring(1);
        }
        return this.m_colour;
    }

    public String getName() {
        return this.m_name;
    }

    public int id() {
        return this.m_id;
    }

    public boolean isVisible() {
        return this.m_isVisible;
    }

    public void setColour(String string2) {
        this.m_colour = string2;
    }

    public void setName(String string2) {
        this.m_name = string2;
    }

    public void setVisible(boolean bl) {
        this.m_isVisible = bl;
    }

    public String toString() {
        return this.m_name;
    }
}
