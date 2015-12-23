package de.bht.bachelor.tasks;

import de.bht.bachelor.beans.OcrResult;

/**
 * Created by and on 23.12.15.
 */
public interface OcrTaskResultCallback {
    void onOcrTaskPreExecute();
    void onOcrTaskResultCallback(OcrResult ocrResult);
    void onTrainDataerror();
}
