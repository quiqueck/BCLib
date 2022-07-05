package org.betterx.bclib.api.v2.dataexchange.handler.autosync;

import org.betterx.bclib.BCLib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

public class FileContentWrapper {
    private byte[] rawContent;
    private ByteArrayOutputStream outputStream;

    FileContentWrapper(byte[] content) {
        this.rawContent = content;
        this.outputStream = null;
    }

    public byte[] getOriginalContent() {
        return rawContent;
    }

    public byte[] getRawContent() {
        if (outputStream != null) {
            return outputStream.toByteArray();
        }
        return rawContent;
    }

    private void invalidateOutputStream() {
        if (this.outputStream != null) {
            try {
                this.outputStream.close();
            } catch (IOException e) {
                BCLib.LOGGER.debug(e);
            }
        }
        this.outputStream = null;
    }

    public void setRawContent(byte[] rawContent) {
        this.rawContent = rawContent;
        invalidateOutputStream();
    }

    public void syncWithOutputStream() {
        if (outputStream != null) {
            try {
                outputStream.flush();
            } catch (IOException e) {
                BCLib.LOGGER.error(e.getMessage());
                e.printStackTrace();
            }
            setRawContent(getRawContent());
            invalidateOutputStream();
        }
    }

    public ByteArrayInputStream getInputStream() {
        if (rawContent == null) return new ByteArrayInputStream(new byte[0]);
        return new ByteArrayInputStream(rawContent);
    }

    public ByteArrayOutputStream getOrCreateOutputStream() {
        if (this.outputStream == null) {
            return this.getEmptyOutputStream();
        }
        return this.outputStream;
    }

    public ByteArrayOutputStream getEmptyOutputStream() {
        invalidateOutputStream();
        this.outputStream = new ByteArrayOutputStream(this.rawContent.length);
        return this.outputStream;
    }
}
