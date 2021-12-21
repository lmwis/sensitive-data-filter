package com.lmwis.datachecker.scheduling;


import com.lmwis.datachecker.service.KeyboardRecordService;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * @Description: TODO
 * @Author: lmwis
 * @Data: 2021/12/21 3:24 下午
 * @Version: 1.0
 */
@Component
public class KeyboardRecordScheduling {

    KeyboardRecordService keyboardRecordService;

    public KeyboardRecordScheduling(KeyboardRecordService keyboardRecordService){
        this.keyboardRecordService = keyboardRecordService;
        new Thread(new KeyboardRecordSchedulingTask(keyboardRecordService)).run();
    }

    @Slf4j
    static class KeyboardRecordSchedulingTask implements Runnable{


        KeyboardRecordService keyboardRecordService;
        public KeyboardRecordSchedulingTask(KeyboardRecordService keyboardRecordService){
            this.keyboardRecordService = keyboardRecordService;
        }

        @Override
        public void run() {

            while(true){
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                int count = keyboardRecordService.saveTempRecord();
//                log.info("定时任务写入缓存，count="+count);
            }
        }

    }

}
