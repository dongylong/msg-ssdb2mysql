package com.trans;

import com.alibaba.fastjson.JSONObject;
import com.trans.bean.Entity;
import com.trans.mapper.EntityMapper;
import com.trans.repository.EntityRepository;
import com.trans.utils.ssdb.SSDBUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import javax.annotation.Resource;
import java.lang.invoke.MethodHandles;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author dongyl
 */
@SpringBootApplication
public class Application implements CommandLineRunner {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());

    @Resource
    private Environment environment;
    @Resource
    private EntityRepository entityRepository;
    @Resource
    private EntityMapper entityMapper;

    private static final String SSDB_KEY = "XXXXXXX_XX_";

    @Resource
    private SSDBUtil ssdbUtil;

    private static final int CPU_CORE = Runtime.getRuntime().availableProcessors();

    private static ExecutorService threadPool = new ThreadPoolExecutor(
            CPU_CORE,
            CPU_CORE,
            0L,
            TimeUnit.MICROSECONDS,
            new LinkedBlockingQueue<>(),
            new ThreadPoolExecutor.DiscardOldestPolicy());

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        LOGGER.info("spring.profiles.active:{}", environment.getProperty("spring.profiles.active"));
        long start = System.currentTimeMillis();
        updateBatch();
        LOGGER.info("msg-ssdb to mysql COST: {} s", (System.currentTimeMillis() - start) / 1000);
        LOGGER.info("ssdb2mysql success");
    }


    private void updateBatch() {
        List<Entity> msgList = entityMapper.findContentIsNull();
        int allMsgDBSize = msgList.size();
        LOGGER.info("allMsgDBSize : {}", allMsgDBSize);
        HashSet<String> keyList = new HashSet<>();
        for (Entity msg : msgList) {
            long msgId = msg.getId();
            String key = SSDB_KEY + msgId;
            keyList.add(key);
        }
        HashSet<String> ssdbKeys = new HashSet<>();
        Map<Long, String> ssdbMapLongKey = new HashMap<>(16);
        Map<String, String> ssdbMap = ssdbUtil.mget(keyList);
        int ssdbSize = ssdbMap.size();
        LOGGER.info("ssdbSize : {}", ssdbSize);
        Set<Map.Entry<String, String>> entries = ssdbMap.entrySet();
        for (Map.Entry<String, String> msgsEntry : entries) {
            String content = msgsEntry.getValue();
            Long key = Long.valueOf(msgsEntry.getKey().replace(SSDB_KEY, ""));
            ssdbKeys.add(msgsEntry.getKey());
            ssdbMapLongKey.put(key, content);
        }
        if (ssdbKeys.size() != allMsgDBSize) {
            ssdbKeys.removeAll(keyList);
            LOGGER.info("ssdb中数据未在数据库中的数量 : {} ：{}", ssdbKeys.size(), ssdbKeys.toString());
        }
        List<Entity> msgListUnbind = new ArrayList<>();
        msgList.forEach(msg -> {
            long msgId = msg.getId();
            if (ssdbMapLongKey.containsKey(msgId)) {
                String content = ssdbMapLongKey.get(msgId);
                if (isJson(content)) {
                    msg.setContent(content);
                } else {
                    LOGGER.error("非json数据。msgId:{}, content : {}", msgId, content);
                }
            } else {
                msgListUnbind.add(msg);
            }
        });
        if (!CollectionUtils.isEmpty(msgListUnbind)) {
            LOGGER.warn("msgListUnbind: {} , length : {}", msgListUnbind.toString(), msgListUnbind.size());
        }
        //需要保存的msg
        List<Entity> msgListSave = new ArrayList<>();
        for (Entity msg : msgList) {
            if (!StringUtils.isEmpty(msg.getContent())) {
                msgListSave.add(msg);
            }
        }
        int saveSize = msgListSave.size();
        LOGGER.info("saveSize : {} ", saveSize);
        int saveLimit = Integer.valueOf(environment.getProperty("save.limit").trim());
        LOGGER.info("saveLimit {}", saveLimit);
        List<Entity> msgListSaveBatch = new ArrayList<>();
        for (Entity msg : msgListSave) {
            msgListSaveBatch.add(msg);

            if (saveLimit == msgListSaveBatch.size()) {
                List<Entity> save = new ArrayList<>();
                save.addAll(msgListSaveBatch);
                msgListSaveBatch.clear();

                threadPool.execute(() -> {
                    update(save);
                });
            }
        }
        if (!CollectionUtils.isEmpty(msgListSaveBatch)) {
            update(msgListSaveBatch);
        }
    }

    private void update(List<Entity> save) {
        LOGGER.info("threadName: {}  test: {} , test.size(): {}", Thread.currentThread().getName(), save, save.size());

        try {
            int i = entityMapper.updateContent(save);
            if (i < 1) {
                LOGGER.error("更新失败. msgListSave: {} , msgListSave.size(): {}", save, save.size());
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error("更新失败. msgListSave: {} , msgListSave.size(): {}", save, save.size());
        }
    }

    public boolean isJson(String content) {

        try {
            JSONObject.parseObject(content);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private void updateSimple() {
        Entity msg = entityRepository.findAllById(11354);
        long msgId = msg.getId();
        String key = SSDB_KEY + msgId;
        String ssdbData = ssdbUtil.get(key);
        LOGGER.info("key : {} , ssdbData ; {} ", key, ssdbData);
        msg.setContent(ssdbData);
        List<Entity> list = new ArrayList<>();
        list.add(msg);
        int i = entityMapper.updateContent(list);
        LOGGER.info("i : {}", i);
    }
}
