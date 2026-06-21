package com.helirfid;

import android.content.Context;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

public class TaskProfile {
    public String uid;
    public String tagName;
    public boolean enabled;
    public final List<TaskAction> actions = new ArrayList<>();

    public static class TaskAction {
        public int type;
        public String param1;
        public String param2;
        public int intParam;
        public boolean boolParam;

        public TaskAction() {}

        public TaskAction(int type, String param1, String param2, int intParam, boolean boolParam) {
            this.type = type;
            this.param1 = param1;
            this.param2 = param2;
            this.intParam = intParam;
            this.boolParam = boolParam;
        }

        JSONObject toJson() throws Exception {
            JSONObject o = new JSONObject();
            o.put("type", type);
            o.put("param1", param1 != null ? param1 : "");
            o.put("param2", param2 != null ? param2 : "");
            o.put("intParam", intParam);
            o.put("boolParam", boolParam);
            return o;
        }

        static TaskAction fromJson(JSONObject o) throws Exception {
            TaskAction a = new TaskAction();
            a.type = o.getInt("type");
            a.param1 = o.optString("param1");
            a.param2 = o.optString("param2");
            a.intParam = o.optInt("intParam");
            a.boolParam = o.optBoolean("boolParam");
            return a;
        }
    }

    public String getActionSummary() {
        if (actions.isEmpty()) return "無動作";
        StringBuilder sb = new StringBuilder();
        for (TaskAction a : actions) {
            if (a.type >= 0 && a.type < TaskExecutor.ACTION_NAMES.length)
                sb.append(TaskExecutor.ACTION_NAMES[a.type]).append(", ");
        }
        if (sb.length() > 2) sb.setLength(sb.length() - 2);
        return sb.toString();
    }

    public static File getProfilesDir(Context context) {
        File dir = new File(context.getFilesDir(), "task_profiles");
        if (!dir.exists()) dir.mkdirs();
        return dir;
    }

    public void save(Context context) throws Exception {
        JSONObject o = new JSONObject();
        o.put("uid", uid);
        o.put("tagName", tagName != null ? tagName : "");
        o.put("enabled", enabled);
        JSONArray arr = new JSONArray();
        for (TaskAction a : actions) arr.put(a.toJson());
        o.put("actions", arr);
        File f = new File(getProfilesDir(context), uid + ".json");
        try (FileWriter w = new FileWriter(f)) {
            w.write(o.toString(2));
        }
    }

    public static TaskProfile load(Context context, String uid) throws Exception {
        File f = new File(getProfilesDir(context), uid + ".json");
        if (!f.exists()) return null;
        try (FileReader r = new FileReader(f)) {
            StringBuilder sb = new StringBuilder();
            int c;
            while ((c = r.read()) != -1) sb.append((char) c);
            JSONObject o = new JSONObject(sb.toString());
            TaskProfile p = new TaskProfile();
            p.uid = o.getString("uid");
            p.tagName = o.optString("tagName");
            p.enabled = o.optBoolean("enabled", true);
            JSONArray arr = o.getJSONArray("actions");
            for (int i = 0; i < arr.length(); i++)
                p.actions.add(TaskAction.fromJson(arr.getJSONObject(i)));
            return p;
        }
    }

    public static void delete(Context context, String uid) {
        new File(getProfilesDir(context), uid + ".json").delete();
    }

    public static List<TaskProfile> listAll(Context context) {
        List<TaskProfile> list = new ArrayList<>();
        File[] files = getProfilesDir(context).listFiles((d, n) -> n.endsWith(".json"));
        if (files == null) return list;
        for (File f : files) {
            try {
                String uid = f.getName().replace(".json", "");
                TaskProfile p = load(context, uid);
                if (p != null) list.add(p);
            } catch (Exception ignored) {}
        }
        return list;
    }
}
