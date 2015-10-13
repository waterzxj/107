/**
 * 
 */
package room107.web.session;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import room107.datamodel.IWritable;

/**
 * @author yanghao
 */
public class Session implements IWritable {
    
    private Map<String, Object> values = new HashMap<String, Object>();
    
    public void set(String key, Object obj) throws UnsupportedTypeException {
        if (obj instanceof String ||
            obj instanceof Integer ||
            obj instanceof Long ||
            obj instanceof Float ||
            obj instanceof Double ||
            obj instanceof Boolean ||
            obj instanceof IWritable) {
            values.put(key, obj);
        } else {
            throw new UnsupportedTypeException("unsupported value type");
        }
    }
    
    @SuppressWarnings("unchecked")
    public <T> T get(String key) {
        return (T)values.get(key);
    }
    
    public void remove(String key) {
        values.remove(key);
    }

    @Override
    public void writeFields(DataOutput out) throws IOException {
        out.writeInt(values.size());
        for (Entry<String, Object> entry : values.entrySet()) {
            out.writeUTF(entry.getKey());
            Object obj = entry.getValue();
            if (obj instanceof String) {
                out.writeByte(0);
                out.writeUTF((String)obj);
            } else if (obj instanceof Integer) {
                out.writeByte(1);
                out.writeInt((Integer)obj);
            } else if (obj instanceof Long) {
                out.writeByte(2);
                out.writeLong((Long)obj);
            } else if (obj instanceof Float) {
                out.writeByte(3);
                out.writeFloat((Float)obj);
            } else if (obj instanceof Double) {
                out.writeByte(4);
                out.writeDouble((Double)obj);
            } else if (obj instanceof Boolean) {
                out.writeByte(5);
                out.writeBoolean((Boolean)obj);
            } else if (obj instanceof IWritable) {
                out.writeByte(6);
                out.writeUTF(obj.getClass().getName());
                IWritable v = (IWritable)obj;
                v.writeFields(out);
            }
        }
    }

    @Override
    public void readFields(DataInput in) throws IOException {
        int n = in.readInt();
        values.clear();
        for (int i = 0; i < n; i++) {
            String key = in.readUTF();
            int type = in.readByte();
            if (type == 0) {
                values.put(key, in.readUTF());
            } else if (type == 1) {
                values.put(key, in.readInt());
            } else if (type == 2) {
                values.put(key, in.readLong());
            } else if (type == 3) {
                values.put(key, in.readFloat());
            } else if (type == 4) {
                values.put(key, in.readDouble());
            } else if (type == 5) {
                values.put(key, in.readBoolean());
            } else if (type == 6) {
                String clazz = in.readUTF();
                try {
                    IWritable v = (IWritable)(Class.forName(clazz).newInstance());
                    v.readFields(in);
                    values.put(key, v);
                } catch (InstantiationException e) {
                    throw new IOException("invalid default constructor for class " + clazz);
                } catch (IllegalAccessException e) {
                    throw new IOException("Illegal access default constructor for class " + clazz);
                } catch (ClassNotFoundException e) {
                    throw new IOException("No such class " + clazz);
                }
            }
        }
    }

}
