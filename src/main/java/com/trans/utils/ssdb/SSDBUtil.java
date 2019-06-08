package com.trans.utils.ssdb;

import com.hyd.ssdb.SsdbClient;
import com.hyd.ssdb.util.KeyValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.lang.invoke.MethodHandles;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author dongyl
 */
@Component
public class SSDBUtil {

    @Resource(name = "singleServerSsdbClient")
    protected SsdbClient ssdbClient;
    private static final Logger logger = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    /**
     * get String
     *
     * @param key
     */
    public String get(String key) {
        return ssdbClient.get(key);
    }


    /**
     * mget String
     */
    public Map<String, String> mget(Collection<String> keySet) {
        String arr[] = new String[keySet.size() + 1];
        int m = 0;
        arr[m++] = "multi_get";
        for (String key : keySet) {
            arr[m++] = key;
        }
        List<KeyValue> keyValues = ssdbClient.sendRequest(arr).getKeyValues();
        HashMap<String, String> map = new HashMap<>(keyValues.size() + 1);
        for (int n = 0; n < keyValues.size(); n++) {
            KeyValue kv = keyValues.get(n);
            map.put(kv.getKey(), kv.getValue());
        }
        return map;
    }
}
