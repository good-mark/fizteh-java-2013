package ru.fizteh.fivt.students.irinapodorozhnaya.utils;

import java.util.HashMap;
import java.util.Map;

public enum Types {
    INTEGER("int", Integer.class) {
        public Integer parseValue(String value) {
            return Integer.parseInt(value);
        }
    },
    LONG("long", Long.class) {
        public Long parseValue(String value) {
            return Long.parseLong(value);
        }
    },
    BYTE("byte", Byte.class) {
        public Byte parseValue(String value) {
            return Byte.parseByte(value);
        }
    },
    FLOAT("float", Float.class) {
        public Float parseValue(String value) {
            return Float.parseFloat(value);
        }
    },
    DOUBLE("double", Double.class) {
        public Double parseValue(String value) {
            return Double.parseDouble(value);
        }
    },
    BOOLEAN("boolean", Boolean.class) {
        public Boolean parseValue(String value) {
            return Boolean.parseBoolean(value);
        }
    },
    STRING("String", String.class) {
        public String parseValue(String value) {
            return value;
        }
    };


    public abstract Object parseValue(String value);

    public static Class<?> getTypeByName(String typeName) {
        Types types = typesByName.get(typeName);
        if (types == null) {
            throw new IllegalArgumentException("unknown type");
        }
        return types.type;
    }

    public static String getSimpleName(Class<?> type) {
        Types typesFormatter = typesByClass.get(type);
        if (typesFormatter == null) {
            throw new IllegalArgumentException("unknown type");
        }
        return typesFormatter.typeName;
    }


    public static Object parse(String value, Class<?> type) {
        Types types = typesByClass.get(type);
        if (types == null) {
            throw new IllegalArgumentException("unknown type");
        }
        return types.parseValue(value);
    }

    private final String typeName;
    private final Class<?> type;

    private Types(String typeName, Class<?> type) {
        this.typeName = typeName;
        this.type = type;
    }

    private static final Map<String, Types> typesByName = new HashMap<>();
    private static final Map<Class<?>, Types> typesByClass = new HashMap<>();

    static {
        for (Types value : values()) {
            typesByName.put(value.typeName, value);
            typesByClass.put(value.type, value);
        }
    }
}
