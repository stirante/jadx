package jadx.fxgui.treemodel;

import jadx.api.JavaPackage;
import jadx.fxgui.JadxWrapper;
import jadx.fxgui.utils.Utils;
import javafx.scene.Node;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;

import java.util.*;

public class JSources extends JNode {
    private static final long serialVersionUID = 8962924556824862801L;

    private static final Image ROOT_ICON = Utils.openIcon("packagefolder_obj");

    private final JadxWrapper wrapper;
    private final boolean flatPackages;

    public JSources(JRoot jRoot, JadxWrapper wrapper) {
        this.flatPackages = jRoot.isFlatPackages();
        this.wrapper = wrapper;
        init();
        update();
    }

    public final void update() {
        removeAllChildren();
        if (flatPackages) {
            for (JavaPackage pkg : wrapper.getPackages()) {
                JPackage node = new JPackage(pkg);
                add(node);
            }
        } else {
            // build packages hierarchy
            List<JPackage> rootPkgs = getHierarchyPackages(wrapper.getPackages());
            for (JPackage jPackage : rootPkgs) {
                jPackage.update();
                add(jPackage);
            }
        }
    }

    /**
     * Convert packages list to hierarchical packages representation
     *
     * @param packages input packages list
     * @return root packages
     */
    List<JPackage> getHierarchyPackages(List<JavaPackage> packages) {
        Map<String, JPackage> pkgMap = new HashMap<>();
        for (JavaPackage pkg : packages) {
            addPackage(pkgMap, new JPackage(pkg));
        }
        // merge packages without classes
        boolean repeat;
        do {
            repeat = false;
            for (JPackage pkg : pkgMap.values()) {
                if (pkg.getInnerPackages().size() == 1 && pkg.getClasses().isEmpty()) {
                    JPackage innerPkg = pkg.getInnerPackages().get(0);
                    pkg.getInnerPackages().clear();
                    pkg.getInnerPackages().addAll(innerPkg.getInnerPackages());
                    pkg.getClasses().addAll(innerPkg.getClasses());
                    pkg.setName(pkg.getName() + "." + innerPkg.getName());

                    innerPkg.getInnerPackages().clear();
                    innerPkg.getClasses().clear();

                    repeat = true;
                    break;
                }
            }
        } while (repeat);

        // remove empty packages
        for (Iterator<Map.Entry<String, JPackage>> it = pkgMap.entrySet().iterator(); it.hasNext(); ) {
            JPackage pkg = it.next().getValue();
            if (pkg.getInnerPackages().isEmpty() && pkg.getClasses().isEmpty()) {
                it.remove();
            }
        }
        // use identity set for collect inner packages
        Set<JPackage> innerPackages = Collections.newSetFromMap(new IdentityHashMap<JPackage, Boolean>());
        for (JPackage pkg : pkgMap.values()) {
            innerPackages.addAll(pkg.getInnerPackages());
        }
        // find root packages
        List<JPackage> rootPkgs = new ArrayList<>();
        for (JPackage pkg : pkgMap.values()) {
            if (!innerPackages.contains(pkg)) {
                rootPkgs.add(pkg);
            }
        }
        Collections.sort(rootPkgs);
        return rootPkgs;
    }

    private void addPackage(Map<String, JPackage> pkgs, JPackage pkg) {
        String pkgName = pkg.getName();
        JPackage replaced = pkgs.put(pkgName, pkg);
        if (replaced != null) {
            pkg.getInnerPackages().addAll(replaced.getInnerPackages());
            pkg.getClasses().addAll(replaced.getClasses());
        }
        int dot = pkgName.lastIndexOf('.');
        if (dot > 0) {
            String prevPart = pkgName.substring(0, dot);
            String shortName = pkgName.substring(dot + 1);
            pkg.setName(shortName);
            JPackage prevPkg = pkgs.get(prevPart);
            if (prevPkg == null) {
                prevPkg = new JPackage(prevPart);
                addPackage(pkgs, prevPkg);
            }
            prevPkg.getInnerPackages().add(pkg);
        }
    }

    @Override
    public Node getIcon() {
        return new ImageView(ROOT_ICON);
    }

    @Override
    public JClass getJParent() {
        return null;
    }

    @Override
    public String makeString() {
        return "Source code";
    }
}
