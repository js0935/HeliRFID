package com.helirfid;

public class HceCardProfile {
    private final String id;
    private final String name;
    private final String uid;
    private final String atqa;
    private final String sak;
    private final String techTypes;
    private final long timestamp;

    public HceCardProfile(String id, String name, String uid, String atqa, String sak, String techTypes, long timestamp) {
        this.id = id;
        this.name = name;
        this.uid = uid;
        this.atqa = atqa;
        this.sak = sak;
        this.techTypes = techTypes;
        this.timestamp = timestamp;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getUid() { return uid; }
    public String getAtqa() { return atqa; }
    public String getSak() { return sak; }
    public String getTechTypes() { return techTypes; }
    public long getTimestamp() { return timestamp; }

    public String toStorageString() {
        return id + "|" + name + "|" + uid + "|" + atqa + "|" + sak + "|" + techTypes + "|" + timestamp;
    }

    public static HceCardProfile fromStorageString(String s) {
        if (s == null || s.isEmpty()) return null;
        String[] p = s.split("\\|", 7);
        if (p.length < 7) return null;
        try {
            return new HceCardProfile(p[0], p[1], p[2], p[3], p[4], p[5], Long.parseLong(p[6]));
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
