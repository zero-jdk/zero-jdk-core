package dev.zerojdk.domain.port.out.download;

public interface ProgressListener {
    void onProgress(long bytesRead, long totalBytes);
}
