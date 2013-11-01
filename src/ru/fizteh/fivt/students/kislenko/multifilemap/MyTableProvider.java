package ru.fizteh.fivt.students.kislenko.multifilemap;

import ru.fizteh.fivt.storage.strings.TableProvider;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MyTableProvider implements TableProvider {
    private Map<String, MyTable> tables = new HashMap<String, MyTable>();

    @Override
    public MyTable getTable(String name) {
        if (name == null || name.equals("")) {
            throw new IllegalArgumentException("Incorrect table name.");
        }
        return tables.get(name);
    }

    @Override
    public MyTable createTable(String name) {
        if (name == null || name.equals("")) {
            throw new IllegalArgumentException("Incorrect table name.");
        }
        MyTable table = new MyTable(name);
        if (table.getSize() == -1) {
            return null;
        }
        tables.put(name, table);
        return table;
    }

    @Override
    public void removeTable(String name) {
        if (!tables.containsKey(name)) {
            throw new IllegalStateException("Have no table to remove.");
        }
        if (name == null || name.equals("")) {
            throw new IllegalArgumentException("Incorrect table name.");
        }
        tables.remove(name);
    }
}