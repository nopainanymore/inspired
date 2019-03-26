package com.nopainanymore.inspired.blockingTask;

import com.nopainanymore.inspired.jedis.JedisClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * @author nopainanymore
 * @time 2019-03-24 16:33
 */
public class TaskManager {

    private static final Logger log = LoggerFactory.getLogger(TaskManager.class);

    private final static String TASK_REDIS_KEY = "task_key";

    private static volatile TaskManager taskManager;

    private JedisClient jedisClient;

    private Boolean tRunning = true;

    private static Queue<BaseTask> taskHolder;

    private TaskManager() {
    }

    public static void shutdown() {
        taskManager.stopRunner();
    }

    private void stopRunner() {
        tRunning = false;
    }

    public static TaskManager getTaskManager(JedisClient jedisClient) {
        if (taskManager == null) {
            synchronized (TaskManager.class) {
                if (taskManager == null) {
                    taskManager = new TaskManager();
                    taskManager.jedisClient = jedisClient;
                    taskHolder = new LinkedBlockingQueue<>(Integer.MAX_VALUE);
                    taskManager.startTask();
                }
            }
        }
        return taskManager;
    }


    public void addTask(List<? extends BaseTask> taskList) {
        taskHolder.addAll(taskList);
    }

    private void startTask() {
        Thread taskRunner = new Thread("taskRunnerThread") {
            @Override
            public void run() {
                while (tRunning) {
                    try {
                        sleep(5_000L);
                        checkTask();
                    } catch (Exception e) {
                        if (log.isDebugEnabled()) {
                            log.debug("TaskManager- run:{}", e);
                        }
                        log.info("TaskManager- run:{}", e.getMessage());
                    }
                }
            }
        };
        taskRunner.setDaemon(true);
        taskRunner.start();
    }

    private void checkTask() {
        String redisNum = jedisClient.get(TASK_REDIS_KEY);
        long currentTaskNumber;
        if (redisNum.equals("null")) {
            currentTaskNumber = 0L;
        } else {
            currentTaskNumber = Long.parseLong(redisNum);
        }
        long free = 300L - currentTaskNumber;
        if (taskHolder.size() < free) {
            free = (long) taskHolder.size();
        }
        if (free > 0) {
            jedisClient.incrBy(TASK_REDIS_KEY, free);
            for (int i = 0; i < free; i++) {
                BaseTask baseTask = taskHolder.poll();
                if (baseTask != null) {
                    baseTask.doTask();
                }
            }

        }
    }

}