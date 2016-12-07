package org.switchyard.transform.pool;

import org.apache.commons.pool.impl.GenericObjectPool;

/**
 * @author Zinoviev Oleg
 * @since 07.12.2016.
 */
public class StringBuffersPool extends GenericObjectPool<StringBuffer> {

    private StringBuffersPool() {
        super(new StringBufferFactory());
        setMaxIdle(64);
        setMinIdle(32);
        setWhenExhaustedAction(WHEN_EXHAUSTED_GROW);
    }

    public final static StringBuffersPool INSTANCE = new StringBuffersPool();

}
