package org.switchyard.transform.pool;

import org.apache.commons.pool.PoolableObjectFactory;

/**
 * @author Zinoviev Oleg
 * @since 07.12.2016.
 */
public class StringBufferFactory implements PoolableObjectFactory<StringBuffer> {
    private final static int INITIAL_SIZE = 1024 * 1024; //1Мб

    @Override
    public StringBuffer makeObject() throws Exception {
        return new StringBuffer(INITIAL_SIZE);
    }

    @Override
    public void destroyObject(StringBuffer stringBuffer) throws Exception {

    }

    @Override
    public boolean validateObject(StringBuffer stringBuffer) {
        return true;
    }

    @Override
    public void activateObject(StringBuffer stringBuffer) throws Exception {
        stringBuffer.setLength(0);
    }

    @Override
    public void passivateObject(StringBuffer stringBuffer) throws Exception {

    }
}
