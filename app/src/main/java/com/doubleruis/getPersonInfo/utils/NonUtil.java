package com.doubleruis.getPersonInfo.utils;

import java.util.Collection;
import java.util.Map;

/**
 * Created by dell
 * 2019/6/5
 */
public class NonUtil {
    public NonUtil() {
    }

    public static boolean isNon(Object object) {
        boolean isnon = false;
        if (object == null) {
            return true;
        } else {
            if (object instanceof String) {
                String str = (String)object;
                if (str.trim().length() == 0) {
                    isnon = true;
                }
            } else if (object instanceof Collection) {
                Collection collection = (Collection)object;
                if (collection.size() == 0) {
                    isnon = true;
                }
            } else if (object instanceof Map) {
                Map map = (Map)object;
                if (map.size() == 0) {
                    isnon = true;
                }
            }

            return isnon;
        }
    }

    public static boolean isNotNon(Object object) {
        return !isNon(object);
    }
}
