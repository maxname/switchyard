package org.switchyard.transform.pool;

import java.io.Writer;

/**
 * @author Zinoviev Oleg
 * @since 07.12.2016.
 */
public class PooledStringWriter extends Writer {
    private StringBuffer buf;

    public PooledStringWriter() {
        try {
            this.buf = StringBuffersPool.INSTANCE.borrowObject();
            this.lock = this.buf;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void write(int b) {
        this.buf.append((char)b);
    }

    @Override
    public void write(char[] chars, int from, int count) {
        if(from >= 0 && from <= chars.length && count >= 0 && from + count <= chars.length && from + count >= 0) {
            if(count != 0) {
                this.buf.append(chars, from, count);
            }
        } else {
            throw new IndexOutOfBoundsException();
        }
    }

    @Override
    public void write(String str) {
        this.buf.append(str);
    }

    @Override
    public void write(String str, int from, int count) {
        this.buf.append(str.substring(from, from + count));
    }

    @Override
    public PooledStringWriter append(CharSequence str) {
        if(str == null) {
            this.write("null");
        } else {
            this.write(str.toString());
        }

        return this;
    }

    @Override
    public PooledStringWriter append(CharSequence str, int from, int count) {
        CharSequence val = str == null ? "null" : str;
        this.write(val.subSequence(from, count).toString());
        return this;
    }

    @Override
    public PooledStringWriter append(char c) {
        this.write(c);
        return this;
    }

    @Override
    public String toString() {
        return this.buf.toString();
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() {
        // Ничего не делаем, так как некоторые маршаллеры вызывают close()
    }

    public void dispose() {
        try {
            StringBuffersPool.INSTANCE.returnObject(buf);
            buf = null;
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
