package com.frca.vsexam.helper;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ObjectMap extends HashMap<String, Object> {
    @Override
    public boolean equals(Object o) {
        if (o instanceof ObjectMap) {
            ObjectMap otherMap = (ObjectMap)o;
            for (Map.Entry<String, Object> entry : entrySet())
                if (!entry.getValue().equals(otherMap.get(entry.getKey())))
                    return false;

            return true;
        }
        return false;
    }

    public boolean isInList(List<ObjectMap> list) {
        for (ObjectMap map : list)
            if (equals(map))
                return true;

        return false;
    }

}
