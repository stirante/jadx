package jadx.fxgui.jobs;

import jadx.fxgui.utils.AsyncTask;
import jadx.fxgui.utils.CacheObject;
import jadx.fxgui.utils.Utils;
import jadx.fxgui.utils.search.TextSearchIndex;
import javafx.application.Platform;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.Future;

public class BackgroundWorker extends AsyncTask<Void, Integer, Void> {
    private static final Logger LOG = LoggerFactory.getLogger(BackgroundWorker.class);

    private final CacheObject cache;

    public BackgroundWorker(CacheObject cacheObject) {
        this.cache = cacheObject;
    }

    public void exec() {
        if (isDone()) {
            return;
        }
        Platform.runLater(() -> {
//				progressPane.setVisible(true);
        });
//		addPropertyChangeListener(progressPane);
        execute();
    }

    @Override
    public void onProgress(Integer progress) {
        //TODO: set progress
    }

    public void stop() {
        if (isDone()) {
            return;
        }
        LOG.debug("Canceling background jobs ...");
        cancel();
    }

    @Override
    public Void doInBackground(Void[] params) {
        try {
            System.gc();
            LOG.debug("Memory usage: Before decompile: {}", Utils.memoryInfo());
            runJob(cache.getDecompileJob());

            LOG.debug("Memory usage: Before index: {}", Utils.memoryInfo());
            runJob(cache.getIndexJob());
            LOG.debug("Memory usage: After index: {}", Utils.memoryInfo());

            System.gc();
            LOG.debug("Memory usage: After gc: {}", Utils.memoryInfo());

            TextSearchIndex searchIndex = cache.getTextIndex();
            if (searchIndex != null && searchIndex.getSkippedCount() > 0) {
                LOG.warn("Indexing of some classes skipped, count: {}, low memory: {}",
                        searchIndex.getSkippedCount(), Utils.memoryInfo());
            }
        } catch (Exception e) {
            LOG.error("Exception in background worker", e);
        }
        return null;
    }

    private void runJob(BackgroundJob job) {
        if (isCancelled()) {
            return;
        }
//		progressPane.changeLabel(this, job.getInfoString());
        Future<Boolean> future = job.process();
        while (!future.isDone()) {
            try {
                publishProgress(job.getProgress());
                if (isCancelled()) {
                    future.cancel(false);
                }
                Thread.sleep(500);
            } catch (Exception e) {
                LOG.error("Background worker error", e);
            }
        }
    }

    @Override
    public void onPostExecute(Void result) {
//		progressPane.setVisible(false);
    }
}
