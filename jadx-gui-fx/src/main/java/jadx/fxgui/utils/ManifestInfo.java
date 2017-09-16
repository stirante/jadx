package jadx.fxgui.utils;

import org.w3c.dom.*;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.util.ArrayList;

public class ManifestInfo {

    private String packageName;
    private String versionName;
    private int versionCode;
    private ArrayList<Activity> activities = new ArrayList<>();

    public ManifestInfo(String manifestXML) {
        try {
            DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
            dbFactory.setValidating(false);
            dbFactory.setSchema(null);
            DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
            ByteArrayInputStream is = new ByteArrayInputStream(manifestXML.getBytes());
            Document doc = dBuilder.parse(is);
            is.close();
            doc.getDocumentElement().normalize();
            packageName = doc.getDocumentElement().getAttribute("package");
            versionName = doc.getDocumentElement().getAttribute("android:versionName");
            versionCode = Integer.parseInt(doc.getDocumentElement().getAttribute("android:versionCode"));
            NodeList a = doc.getElementsByTagName("activity");
            for (int i = 0; i < a.getLength(); i++) {
                Node activity = a.item(i);
                activities.add(new Activity((Element) activity));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public String toString() {
        return "ManifestInfo{" +
                "packageName='" + packageName + '\'' +
                ", versionName='" + versionName + '\'' +
                ", versionCode=" + versionCode +
                ", activities=" + activities +
                '}';
    }

    public String getPackageName() {
        return packageName;
    }

    public String getVersionName() {
        return versionName;
    }

    public int getVersionCode() {
        return versionCode;
    }

    public ArrayList<Activity> getActivities() {
        return activities;
    }

    public class Activity {

        private String name;
        private boolean isMain = false;

        private Activity(Element e) {
            name = e.getAttribute("android:name");
            NodeList filters = e.getElementsByTagName("intent-filter");
            for (int i = 0; i < filters.getLength(); i++) {
                Element item = (Element) filters.item(i);
                NodeList actions = item.getElementsByTagName("action");
                for (int j = 0; j < actions.getLength(); j++) {
                    Element action = (Element) actions.item(j);
                    if (action.getAttribute("android:name").equalsIgnoreCase("android.intent.action.MAIN"))
                        isMain = true;
                }
            }
        }

        @Override
        public String toString() {
            return "Activity{" +
                    "name='" + name + '\'' +
                    ", isMain=" + isMain +
                    '}';
        }

        public String getName() {
            return name;
        }

        public boolean isMain() {
            return isMain;
        }
    }

}
