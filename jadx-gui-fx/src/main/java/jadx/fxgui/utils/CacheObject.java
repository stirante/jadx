package jadx.fxgui.utils;

import jadx.fxgui.utils.search.TextSearchIndex;
import jadx.fxgui.jobs.DecompileJob;
import jadx.fxgui.jobs.IndexJob;

import org.jetbrains.annotations.Nullable;

public class CacheObject {

	private DecompileJob decompileJob;
	private IndexJob indexJob;

	private TextSearchIndex textIndex;
	private CodeUsageInfo usageInfo;
	private String lastSearch;
	private JNodeCache jNodeCache = new JNodeCache();

	public void reset() {
		textIndex = null;
		lastSearch = null;
		jNodeCache = new JNodeCache();
		usageInfo = null;
	}

	public DecompileJob getDecompileJob() {
		return decompileJob;
	}

	public void setDecompileJob(DecompileJob decompileJob) {
		this.decompileJob = decompileJob;
	}

	@Nullable
	public TextSearchIndex getTextIndex() {
		return textIndex;
	}

	public void setTextIndex(TextSearchIndex textIndex) {
		this.textIndex = textIndex;
	}

	@Nullable
	public String getLastSearch() {
		return lastSearch;
	}

	public void setLastSearch(String lastSearch) {
		this.lastSearch = lastSearch;
	}

	@Nullable
	public CodeUsageInfo getUsageInfo() {
		return usageInfo;
	}

	public void setUsageInfo(@Nullable CodeUsageInfo usageInfo) {
		this.usageInfo = usageInfo;
	}

	public IndexJob getIndexJob() {
		return indexJob;
	}

	public void setIndexJob(IndexJob indexJob) {
		this.indexJob = indexJob;
	}

	public JNodeCache getNodeCache() {
		return jNodeCache;
	}
}
