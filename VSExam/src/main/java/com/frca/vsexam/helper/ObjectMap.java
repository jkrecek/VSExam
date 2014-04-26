package com.frca.vsexam.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectMap extends HashMap<String, Object> {
    @Override
    public boolean equals(Object o) {
        if (o instanceof ObjectMap) {
            boolean equals = true;
            ObjectMap otherMap = (ObjectMap)o;
            for (Map.Entry entry : entrySet()) {
                if (entry.getValue() != otherMap.get(entry.getKey()))
                    return false;
            }

            return true;
        }
        return false;
    }

    public boolean isInList(List<ObjectMap> list) {
        for (ObjectMap map : list) {
            if (equals(map))
                return true;
        }

        return false;
    }

}
