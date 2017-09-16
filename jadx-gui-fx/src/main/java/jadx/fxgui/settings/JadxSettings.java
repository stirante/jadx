package jadx.fxgui.settings;

import jadx.cli.JadxCLIArgs;

import java.awt.*;
import java.util.*;
import java.util.List;

public class JadxSettings extends JadxCLIArgs {

    static final Set<String> SKIP_FIELDS = new HashSet<>(Arrays.asList(
            "files", "input", "outputDir", "verbose", "printHelp"
    ));
    private static final String USER_HOME = System.getProperty("user.home");
    private static final int RECENT_FILES_COUNT = 15;
    private String lastOpenFilePath = USER_HOME;
    private String lastSaveFilePath = USER_HOME;
    private boolean flattenPackage = false;
    private boolean checkForUpdates = true;
    private boolean moreResults = false;
    private List<String> recentFiles = new ArrayList<>();
    private String fontStr = "";
    private boolean autoStartJobs = true;

    private Map<String, WindowLocation> windowPos = new HashMap<>();

    public JadxSettings() {
        setSkipResources(true);
    }

    private static String addStyleName(int style) {
        switch (style) {
            case Font.BOLD:
                return "-BOLD";
            case Font.PLAIN:
                return "-PLAIN";
            case Font.ITALIC:
                return "-ITALIC";
            default:
                return "";
        }
    }

    public void sync() {
        JadxSettingsAdapter.store(this);
    }

    public String getLastOpenFilePath() {
        return lastOpenFilePath;
    }

    public void setLastOpenFilePath(String lastOpenFilePath) {
        this.lastOpenFilePath = lastOpenFilePath;
        sync();
    }

    public String getLastSaveFilePath() {
        return lastSaveFilePath;
    }

    public void setLastSaveFilePath(String lastSaveFilePath) {
        this.lastSaveFilePath = lastSaveFilePath;
        sync();
    }

    public boolean isFlattenPackage() {
        return flattenPackage;
    }

    public void setFlattenPackage(boolean flattenPackage) {
        this.flattenPackage = flattenPackage;
        sync();
    }

    public boolean isMoreResults() {
        return moreResults;
    }

    public void setMoreResults(boolean moreResults) {
        this.moreResults = moreResults;
        sync();
    }

    public boolean isCheckForUpdates() {
        return checkForUpdates;
    }

    public void setCheckForUpdates(boolean checkForUpdates) {
        this.checkForUpdates = checkForUpdates;
        sync();
    }

    public Iterable<String> getRecentFiles() {
        return recentFiles;
    }

    public void addRecentFile(String filePath) {
        recentFiles.remove(filePath);
        recentFiles.add(0, filePath);
        int count = recentFiles.size();
        if (count > RECENT_FILES_COUNT) {
            recentFiles.subList(0, count - RECENT_FILES_COUNT).clear();
        }
        sync();
    }

    public void saveWindowPos(Window window) {
        WindowLocation pos = new WindowLocation(window.getClass().getSimpleName(),
                window.getX(), window.getY(),
                window.getWidth(), window.getHeight()
        );
        windowPos.put(pos.getWindowId(), pos);
        sync();
    }

    public boolean loadWindowPos(Window window) {
        WindowLocation pos = windowPos.get(window.getClass().getSimpleName());
        if (pos == null) {
            return false;
        }
        window.setLocation(pos.getX(), pos.getY());
        window.setSize(pos.getWidth(), pos.getHeight());
        return true;
    }

    public void setThreadsCount(int threadsCount) {
        this.threadsCount = threadsCount;
    }

    public void setFallbackMode(boolean fallbackMode) {
        this.fallbackMode = fallbackMode;
    }

    public void setSkipResources(boolean skipResources) {
        this.skipResources = skipResources;
    }

    public void setSkipSources(boolean skipSources) {
        this.skipSources = skipSources;
    }

    public void setShowInconsistentCode(boolean showInconsistentCode) {
        this.showInconsistentCode = showInconsistentCode;
    }

    public void setCfgOutput(boolean cfgOutput) {
        this.cfgOutput = cfgOutput;
    }

    public void setRawCfgOutput(boolean rawCfgOutput) {
        this.rawCfgOutput = rawCfgOutput;
    }

    public void setVerbose(boolean verbose) {
        this.verbose = verbose;
    }

    public void setDeobfuscationOn(boolean deobfuscationOn) {
        this.deobfuscationOn = deobfuscationOn;
    }

    public void setDeobfuscationMinLength(int deobfuscationMinLength) {
        this.deobfuscationMinLength = deobfuscationMinLength;
    }

    public void setDeobfuscationMaxLength(int deobfuscationMaxLength) {
        this.deobfuscationMaxLength = deobfuscationMaxLength;
    }

    public void setDeobfuscationForceSave(boolean deobfuscationForceSave) {
        this.deobfuscationForceSave = deobfuscationForceSave;
    }

    public void setUseSourceNameAsClassAlias(boolean useSourceNameAsAlias) {
        this.deobfuscationUseSourceNameAsAlias = useSourceNameAsAlias;
    }

    public void setEscapeUnicode(boolean escapeUnicode) {
        this.escapeUnicode = escapeUnicode;
    }

    public void setReplaceConsts(boolean replaceConsts) {
        this.replaceConsts = replaceConsts;
    }

    public boolean isAutoStartJobs() {
        return autoStartJobs;
    }

    public void setAutoStartJobs(boolean autoStartJobs) {
        this.autoStartJobs = autoStartJobs;
    }

    public void setExportAsGradleProject(boolean exportAsGradleProject) {
        this.exportAsGradleProject = exportAsGradleProject;
    }

    public Font getFont() {
        if (fontStr.isEmpty()) {
            return null;//TODO: !!!
        }
        return Font.decode(fontStr);
    }

    public void setFont(Font font) {
        this.fontStr = font.getFontName() + addStyleName(font.getStyle()) + "-" + font.getSize();
    }
}
